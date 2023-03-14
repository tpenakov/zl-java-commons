package green.zerolabs.commons.dynamo.db.utils;

import com.fasterxml.jackson.databind.JsonNode;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.dynamo.db.exception.InvalidPaginationTokenException;
import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import green.zerolabs.commons.dynamo.db.model.ZlDbPutItemPackage;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bk.aws.dynamo.util.JsonAttributeValueUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static green.zerolabs.commons.dynamo.db.model.Constants.STORAGE_ZLMVPDD_NAME;
import static green.zerolabs.commons.dynamo.db.model.Constants.TABLE_NAME;
import static green.zerolabs.commons.dynamo.db.utils.DynamoDbUtils.*;
import static java.util.stream.Collectors.toMap;

/**
 * Please does not make this class public. Access it through DynamoDbUtils. The idea behind is to
 * unit test some functionality.
 */
@Getter
@Slf4j
class DynamoDbHelperUtils {
  private final ConverterUtils converterUtils;
  private final JsonUtils jsonUtils;
  private final Function<Object, JsonNode> objectToJsonNode;
  private final String tableName;

  public DynamoDbHelperUtils(final JsonUtils jsonUtils, final ConverterUtils converterUtils) {
    this.jsonUtils = jsonUtils;
    this.converterUtils = converterUtils;
    objectToJsonNode =
        Unchecked.<Object, JsonNode>function(
            zlDbItem ->
                getJsonUtils()
                    .getObjectMapper()
                    .readTree(getJsonUtils().toStringLazy(zlDbItem).toString()));
    tableName = Optional.ofNullable(System.getenv(STORAGE_ZLMVPDD_NAME)).orElse(TABLE_NAME);
  }

  List<List<TransactWriteItem>> splitToBatches(final List<TransactWriteItem> input) {
    final List<TransactWriteItem> forProcessing =
        input.stream().limit(MAX_ITEMS_IN_TRANSACTION).collect(Collectors.toList());
    final List<TransactWriteItem> nextForProcessing =
        input.stream().skip(MAX_ITEMS_IN_TRANSACTION).collect(Collectors.toList());
    if (CollectionUtils.isNullOrEmpty(nextForProcessing)) {
      return List.of(forProcessing);
    }
    if (nextForProcessing.size() < MAX_ITEMS_IN_TRANSACTION) {
      return List.of(forProcessing, nextForProcessing);
    }

    return Stream.concat(Stream.of(forProcessing), splitToBatches(nextForProcessing).stream())
        .collect(Collectors.toList());
  }

  public Stream<TransactWriteItem> splitAndTransform(
      final Stream<ZlDbPutItemPackage> input, final Predicate<ZlDbPutItemPackage> predicate) {
    return input
        .filter(predicate)
        .map(
            itemWithCondition ->
                toTransactWritePutItem(
                    itemWithCondition.getItem(),
                    itemWithCondition.getCondition(),
                    itemWithCondition.getConditionNames(),
                    itemWithCondition.getConditionValues()));
  }

  public TransactWriteItem toTransactWritePutItem(
      final ZlDbItem item,
      final String conditionExpression,
      final Map<String, String> expressionAttributeNames,
      final Map<String, AttributeValue> expressionAttributeValues) {
    final Map<String, AttributeValue> map = toStringAttributeValueMap(item);
    //    log.info(
    //        "conditionExpression:{}, expressionAttributeValues:{}",
    //        conditionExpression,
    //        expressionAttributeValues);

    return TransactWriteItem.builder()
        .put(
            builder ->
                builder
                    .tableName(getTableName())
                    .item(map)
                    .expressionAttributeNames(expressionAttributeNames)
                    .conditionExpression(conditionExpression)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build())
        .build();
  }

  public Map<String, AttributeValue> toStringAttributeValueMap(final ZlDbItem item) {
    final Map<String, AttributeValue> map = new HashMap<>();
    map.put(ZlDbItem.ID, AttributeValue.builder().s(item.getId()).build());
    map.put(ZlDbItem.SORT, AttributeValue.builder().s(item.getSort()).build());
    Optional.ofNullable(item.getGsiSort())
        .ifPresent(s -> map.put(ZlDbItem.GSI_SORT, AttributeValue.builder().s(s).build()));
    Optional.ofNullable(item.getGsiNumericSort())
        .ifPresent(
            o ->
                map.put(
                    ZlDbItem.GSI_NUMERIC_SORT,
                    AttributeValue.builder().n(getConverterUtils().toString(o)).build()));
    Optional.ofNullable(item.getData())
        .ifPresent(
            o ->
                map.put(
                    ZlDbItem.DATA,
                    JsonAttributeValueUtil.toAttributeValue(
                        getObjectToJsonNode().apply(item.getData()))));
    return map;
  }

  public Optional<ZlDbItem> findDataItem(
      final List<ZlDbItem> items, final String id, final String dataItemSortKey) {
    return items.stream()
        .filter(zlDbItem -> StringUtils.equals(id, zlDbItem.getId()))
        .filter(zlDbItem -> StringUtils.equals(dataItemSortKey, zlDbItem.getSort()))
        .findFirst();
  }

  public Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>> toResultAndStartKey(
      final Tuple2<List<Map<String, AttributeValue>>, Optional<Map<String, AttributeValue>>>
          objects) {
    if (objects == null) {
      return null;
    }
    return objects.mapItem1(
        list ->
            list.stream()
                .map(map -> JsonAttributeValueUtil.fromAttributeValue(map))
                .map(
                    jsonNode ->
                        getJsonUtils().readValue(jsonNode.toString(), ZlDbItem.class).orElse(null))
                .collect(Collectors.toList()));
  }

  public ZlDbItem createGsiItem(final String id, final String sort, final String gsiSort) {
    return createGsiItem(id, sort, gsiSort, null);
  }

  public ZlDbItem createGsiItem(final String id, final String sort, final Long gsiNumericSort) {
    return createGsiItem(id, sort, gsiNumericSort, null);
  }

  public ZlDbItem createGsiItem(
      final String id, final String sort, final String gsiSort, final Object data) {
    return ZlDbItem.builder().id(id).sort(sort).gsiSort(gsiSort).data(data).build();
  }

  public ZlDbItem createGsiItem(
      final String id, final String gsi, final Long gsiSort, final Object data) {
    return ZlDbItem.builder().id(id).sort(gsi).gsiNumericSort(gsiSort).data(data).build();
  }

  public List<String> createSortKeys(final String key, final List<String>... keys) {
    return createSortKeys(key, createSortKeys(Arrays.asList(keys)));
  }

  public List<String> createSortKeys(final List<String>... keys) {
    return createSortKeys(Arrays.asList(keys));
  }

  public List<String> createSortKeys(final List<List<String>> keys) {
    if (CollectionUtils.isNullOrEmpty(keys)) {
      return List.of();
    }

    final int keysSize = keys.size();
    if (keysSize == 1) {
      return keys.get(0);
    }

    if (keysSize == 2) {
      return createSortKeys(keys.get(0), keys.get(1));
    }

    return createSortKeys(
        keys.get(0), createSortKeys(keys.stream().skip(1).collect(Collectors.toList())));
  }

  public List<String> createSortKeys(final List<String> keys, final List<String> keys2) {
    return toWildcardStream(keys)
        .flatMap(s -> createSortKeys(s, keys2).stream())
        .collect(Collectors.toList());
  }

  public List<String> createSortKeys(final String key, final List<String> keys) {
    return toWildcardStream(keys)
        .distinct()
        .map(s -> key + DELIMITER + s)
        .collect(Collectors.toList());
  }

  public Stream<String> toWildcardStream(final List<String> keys) {
    return !startsWithWildcard(keys)
        ? Stream.concat(Stream.of(WILDCARD), keys.stream())
        : keys.stream();
  }

  public String createSortKey(final String prefix, final String... keyValues) {
    return createSortKey(prefix, Arrays.stream(keyValues));
  }

  public String createSortKey(final String prefix, final Stream<String> keyValues) {
    return Stream.concat(Stream.of(prefix), keyValues)
        .sequential()
        .map(s -> StringUtils.isNotBlank(s) ? s : WILDCARD)
        .collect(Collectors.joining(DELIMITER));
  }

  public boolean startsWithWildcard(final List<String> keys) {

    if (CollectionUtils.isNullOrEmpty(keys)) {
      return false;
    }

    return Optional.ofNullable(keys).stream().flatMap(List::stream).findFirst().stream()
        .filter(StringUtils::isNotBlank)
        .flatMap(
            s ->
                Arrays.stream(
                    green.zerolabs.commons.core.utils.apache.commons.StringUtils.split(
                        s, DELIMITER)))
        .map(String::trim)
        .filter(s -> !StringUtils.equals(WILDCARD, s))
        .findAny()
        .isEmpty();
  }

  String generateToken(final Map<String, AttributeValue> exclusiveStartKey) {
    if (CollectionUtils.isNullOrEmpty(exclusiveStartKey)) {
      return null;
    }

    final Map<String, AttributeValue.Builder> builderMap =
        exclusiveStartKey.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().toBuilder()));

    final String rs = jsonUtils.toStringLazy(builderMap).toString();

    return Base64.getEncoder().encodeToString(rs.getBytes());
  }

  Map<String, AttributeValue> generateAttributeValueMap(final String startToken) {
    try {
      final String json = new String(Base64.getDecoder().decode(startToken.getBytes()));
      return jsonUtils
          .toMap(json, String.class, AttributeValue.serializableBuilderClass())
          .entrySet()
          .stream()
          .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    } catch (final Exception e) {
      throw new InvalidPaginationTokenException(e, startToken);
    }
  }
}
