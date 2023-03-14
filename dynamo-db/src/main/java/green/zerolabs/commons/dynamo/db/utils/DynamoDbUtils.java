package green.zerolabs.commons.dynamo.db.utils;

import com.fasterxml.jackson.databind.JsonNode;
import green.zerolabs.commons.core.service.CryptoService;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.GraphQlWrapper;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import green.zerolabs.commons.dynamo.db.model.ZlDbPutItemPackage;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bk.aws.dynamo.util.JsonAttributeValueUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static green.zerolabs.commons.dynamo.db.model.Constants.STORAGE_ZLMVPDD_NAME;
import static green.zerolabs.commons.dynamo.db.model.Constants.TABLE_NAME;

/*
 * Created by Triphon Penakov 19.03.2022
 */
@Getter
@Slf4j
public class DynamoDbUtils {
  public static final String KEY_CONDITION_EXPRESSION =
      ZlDbItem.ID + "=:id and " + ZlDbItem.SORT + "=:sort";
  public static final String GSI_CONDITION_EXPRESSION =
      ZlDbItem.SORT + "=:id and " + ZlDbItem.GSI_SORT + "=:sort";
  public static final String GSI_NUMERIC_CONDITION_EXPRESSION =
      ZlDbItem.SORT + "=:id and " + ZlDbItem.GSI_NUMERIC_SORT + "=:sort";
  public static final String ID_VALUE = ":id";
  public static final String SORT_VALUE = ":sort";
  public static final int MAX_ITEMS_IN_TRANSACTION = 10;
  public static final String WILDCARD = "*";
  public static final String DELIMITER = "--";
  private final JsonUtils jsonUtils;
  private final ConverterUtils converterUtils;
  private final GraphQlWrapper graphQlWrapper;
  private final DynamoDbAsyncClient dynamoDB;
  private final CryptoService cryptoService;
  private final DynamoDbHelperUtils dynamoDbHelperUtils;
  private final Function<Object, JsonNode> objectToJsonNode;

  private final String tableName;

  public DynamoDbUtils(
      final JsonUtils jsonUtils,
      final ConverterUtils converterUtils,
      final GraphQlWrapper graphQlWrapper,
      final DynamoDbAsyncClient dynamoDB,
      final CryptoService cryptoService) {
    this.jsonUtils = jsonUtils;
    this.converterUtils = converterUtils;
    this.graphQlWrapper = graphQlWrapper;
    this.dynamoDB = dynamoDB;
    this.cryptoService = cryptoService;
    dynamoDbHelperUtils = new DynamoDbHelperUtils(jsonUtils, converterUtils);
    objectToJsonNode =
        Unchecked.function(
            zlDbItem ->
                getJsonUtils()
                    .getObjectMapper()
                    .readTree(getJsonUtils().toStringLazy(zlDbItem).toString()));
    tableName = Optional.ofNullable(System.getenv(STORAGE_ZLMVPDD_NAME)).orElse(TABLE_NAME);
  }

  public Multi<TransactWriteItemsResponse> write(final List<TransactWriteItem> requests) {
    // log.info("write: {}", requests);
    final Queue<List<TransactWriteItem>> batches =
        new ConcurrentLinkedQueue<>(splitToBatches(requests));

    final AtomicInteger numRetry = new AtomicInteger(0);
    return Uni.createFrom()
        .deferred(
            () ->
                Uni.createFrom()
                    .item(batches.poll())
                    .onSubscription()
                    .invoke(() -> numRetry.set(0))
                    .flatMap(
                        list -> {
                          final String token = UUID.randomUUID().toString();
                          return Uni.createFrom()
                              .deferred(
                                  () ->
                                      Uni.createFrom()
                                          .completionStage(
                                              () ->
                                                  getDynamoDB()
                                                      .transactWriteItems(
                                                          builder ->
                                                              builder
                                                                  .transactItems(list)
                                                                  .clientRequestToken(token))))
                              .onFailure()
                              .retry()
                              .until(
                                  throwable -> {
                                    if (throwable instanceof TransactionCanceledException
                                        && Optional.ofNullable(throwable.getMessage())
                                            .map(s -> s.contains("ConditionalCheckFailed"))
                                            .orElse(false)) {
                                      return false;
                                    }
                                    if (numRetry.incrementAndGet() > 5) {
                                      return false;
                                    }
                                    return true;
                                  })
                              .onFailure()
                              .invoke(throwable -> log.error("unable to store: {}", list))
                          // .onItem()
                          // .invoke(response -> log.debug("response: {}", response))
                          ;
                        }))
        .repeat()
        .whilst(response -> batches.peek() != null);
  }

  List<List<TransactWriteItem>> splitToBatches(final List<TransactWriteItem> input) {
    return getDynamoDbHelperUtils().splitToBatches(input);
  }

  public Uni<Map<String, List<TransactWriteItem>>> getTransactWriteItems(
      final List<ZlDbItem> items) {
    if (CollectionUtils.isNullOrEmpty(items)) {
      return Uni.createFrom().item(Map.of());
    }

    return getTransactWriteItemsByConditions(
        items.stream()
            .map(item -> ZlDbPutItemPackage.builder().item(item).build())
            .collect(Collectors.toList()));
  }

  public Uni<Map<String, List<TransactWriteItem>>> getTransactWriteItemsByConditions(
      final List<ZlDbPutItemPackage> itemsWithCondition) {

    return Multi.createFrom()
        .items(itemsWithCondition.stream().map(item -> item.getItem().getId()).distinct())
        .flatMap(
            id -> {
              //              log.info("findAllById: {}", id);
              return findAllById(id).map(list -> Tuple2.of(id, list)).convert().toPublisher();
            })
        .map(
            objects1 -> {
              final String id = objects1.getItem1();
              final List<ZlDbItem> items1 = objects1.getItem2();
              // log.info("id: {},, items1: {}", id, items1);
              final Stream<TransactWriteItem> itemsDeleteStream =
                  items1.stream()
                      .map(item -> Tuple2.of(item.getId(), item.getSort()))
                      .distinct()
                      .filter(
                          objects ->
                              itemsWithCondition.stream()
                                  .filter(
                                      zlDbItem ->
                                          StringUtils.equals(
                                              objects.getItem2(), zlDbItem.getItem().getSort()))
                                  .findFirst()
                                  .isEmpty())
                      .map(
                          objects ->
                              TransactWriteItem.builder()
                                  .delete(
                                      builder ->
                                          builder
                                              .tableName(getTableName())
                                              .key(
                                                  Map.of(
                                                      ZlDbItem.ID,
                                                      AttributeValue.builder()
                                                          .s(objects.getItem1())
                                                          .build(),
                                                      ZlDbItem.SORT,
                                                      AttributeValue.builder()
                                                          .s(objects.getItem2())
                                                          .build()))
                                              .build())
                                  .build());

              final Stream<TransactWriteItem> itemsPutStreamFirst =
                  splitAndTransform(
                      itemsWithCondition.stream()
                          .filter(
                              itemPackage -> StringUtils.equals(id, itemPackage.getItem().getId())),
                      zlDbPutItemPackage -> Boolean.TRUE.equals(zlDbPutItemPackage.getRunFirst()));

              final Stream<TransactWriteItem> itemsPutStreamLast =
                  splitAndTransform(
                      itemsWithCondition.stream()
                          .filter(
                              itemPackage -> StringUtils.equals(id, itemPackage.getItem().getId())),
                      zlDbPutItemPackage -> !Boolean.TRUE.equals(zlDbPutItemPackage.getRunFirst()));
              final List<TransactWriteItem> result =
                  Stream.of(itemsPutStreamFirst, itemsDeleteStream, itemsPutStreamLast)
                      .flatMap(transactWriteItemStream -> transactWriteItemStream)
                      .collect(Collectors.toList());
              return Tuple2.of(id, result);
            })
        .collect()
        .asMap(Tuple2::getItem1, Tuple2::getItem2);
  }

  public Stream<TransactWriteItem> splitAndTransform(
      final Stream<ZlDbPutItemPackage> input, final Predicate<ZlDbPutItemPackage> predicate) {
    return getDynamoDbHelperUtils().splitAndTransform(input, predicate);
  }

  public Map<String, AttributeValue> toStringAttributeValueMap(final ZlDbItem item) {
    return getDynamoDbHelperUtils().toStringAttributeValueMap(item);
  }

  public Optional<ZlDbItem> findDataItem(
      final List<ZlDbItem> items, final String id, final String dataItemSortKey) {
    return getDynamoDbHelperUtils().findDataItem(items, id, dataItemSortKey);
  }

  @SuppressWarnings("unchecked")
  public Uni<Map<String, Object>> findDataById(final String id, final String sort) {
    return findOne(id, sort)
        .onItem()
        .ifNotNull()
        .transform(ZlDbItem::getData)
        .onItem()
        .ifNotNull()
        .transform(data -> (Map<String, Object>) data);
  }

  public <T> Uni<T> findDataById(final String id, final String sort, final Class<T> clazz) {
    return findDataById(id, sort)
        .onItem()
        .ifNotNull()
        .transform(data -> getJsonUtils().fromMap(data, clazz));
  }

  public Uni<ZlDbItem> findOne(final String id, final String sort) {
    return Uni.createFrom()
        .completionStage(
            getDynamoDB()
                .getItem(
                    builder ->
                        builder
                            .tableName(getTableName())
                            .consistentRead(true)
                            .key(
                                Map.of(
                                    ZlDbItem.ID,
                                    AttributeValue.builder().s(id).build(),
                                    ZlDbItem.SORT,
                                    AttributeValue.builder().s(sort).build()))))
        .map(GetItemResponse::item)
        .map(JsonAttributeValueUtil::fromAttributeValue)
        .map(
            jsonNode ->
                getJsonUtils()
                    .readValue(jsonNode.toString(), ZlDbItem.class)
                    .filter(zlDbItem -> StringUtils.isNotBlank(zlDbItem.getId()))
                    .orElse(null));
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyBeginsWith(
      final String id,
      final String value,
      final Integer limit,
      final Map<String, AttributeValue> exclusiveStartKey) {
    return findManyByCondition(
        id,
        value,
        null,
        limit,
        toBeginsWithConditionExpression(ZlDbItem.ID, ZlDbItem.SORT),
        exclusiveStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>>
      findManyBeginsWithByIndex(
          final String id,
          final String value,
          final Integer limit,
          final Map<String, AttributeValue> exclusiveStartKey) {
    return findManyByCondition(
        id,
        value,
        ZlDbItem.GSI_NAME,
        limit,
        toBeginsWithConditionExpression(ZlDbItem.SORT, ZlDbItem.GSI_SORT),
        exclusiveStartKey);
  }

  private String toBeginsWithConditionExpression(
      final String indexKeyName, final String sortKeyName) {
    return MessageFormat.format(
        "{0}={1} and begins_with({2}, {3})", indexKeyName, ID_VALUE, sortKeyName, SORT_VALUE);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyGreaterThan(
      final String id,
      final String value,
      final Integer limit,
      final Map<String, AttributeValue> exclusiveStartKey) {
    return findManyByCondition(
        id,
        value,
        null,
        limit,
        toGreaterThanConditionExpression(ZlDbItem.ID, ZlDbItem.SORT),
        exclusiveStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>>
      findManyGreaterThanByIndex(
          final String id,
          final Long value,
          final Integer limit,
          final Map<String, AttributeValue> exclusiveStartKey) {
    return findManyByCondition(
        id,
        value,
        ZlDbItem.GSI_NUMERIC_NAME,
        limit,
        toGreaterThanConditionExpression(ZlDbItem.SORT, ZlDbItem.GSI_NUMERIC_SORT),
        exclusiveStartKey);
  }

  private String toGreaterThanConditionExpression(
      final String indexKeyName, final String sortKeyName) {
    return MessageFormat.format(
        "{0}={1} and {2}>{3}", indexKeyName, ID_VALUE, sortKeyName, SORT_VALUE);
  }

  private Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyByCondition(
      final String indexValue,
      final String sortValue,
      final String indexName,
      final Integer limit,
      final String conditionExpression,
      final Map<String, AttributeValue> exclusiveStartKey) {

    final QueryRequest request =
        QueryRequest.builder()
            .tableName(getTableName())
            .indexName(indexName)
            .keyConditionExpression(conditionExpression)
            .limit(limit)
            .consistentRead(StringUtils.isBlank(indexName))
            .exclusiveStartKey(exclusiveStartKey)
            .expressionAttributeValues(
                Map.of(
                    ID_VALUE,
                    AttributeValue.builder().s(indexValue).build(),
                    SORT_VALUE,
                    AttributeValue.builder().s(sortValue).build()))
            .build();

    return Uni.createFrom()
        .completionStage(getDynamoDB().query(request))
        .map(
            response ->
                Tuple2.of(response.items(), Optional.ofNullable(response.lastEvaluatedKey())))
        .map(this::toResultAndStartKey);
  }

  private Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyByCondition(
      final String indexValue,
      final Long sortValue,
      final String indexName,
      final Integer limit,
      final String conditionExpression,
      final Map<String, AttributeValue> exclusiveStartKey) {

    final QueryRequest request =
        QueryRequest.builder()
            .tableName(getTableName())
            .indexName(indexName)
            .keyConditionExpression(conditionExpression)
            .limit(limit)
            .consistentRead(StringUtils.isBlank(indexName))
            .exclusiveStartKey(exclusiveStartKey)
            .expressionAttributeValues(
                Map.of(
                    ID_VALUE,
                    AttributeValue.builder().s(indexValue).build(),
                    SORT_VALUE,
                    AttributeValue.builder().n(getConverterUtils().toString(sortValue)).build()))
            .build();

    return Uni.createFrom()
        .completionStage(getDynamoDB().query(request))
        .map(
            response ->
                Tuple2.of(response.items(), Optional.ofNullable(response.lastEvaluatedKey())))
        .map(this::toResultAndStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyByIndex(
      final String id,
      final String sort,
      final Integer limit,
      final Map<String, AttributeValue> exclusiveStartKey) {
    return findManyByIndex(
        ZlDbItem.GSI_NAME,
        ZlDbItem.GSI_SORT,
        id,
        AttributeValue.builder().s(sort).build(),
        limit,
        exclusiveStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyByIndex(
      final String id,
      final Long sort,
      final Integer limit,
      final Map<String, AttributeValue> exclusiveStartKey) {
    return findManyByIndex(
        ZlDbItem.GSI_NUMERIC_NAME,
        ZlDbItem.GSI_NUMERIC_SORT,
        id,
        AttributeValue.builder().n(getConverterUtils().toString(sort)).build(),
        limit,
        exclusiveStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyByIndex(
      final String indexName,
      final String sortFieldName,
      final String id,
      final AttributeValue sort,
      final Integer limit,
      final Map<String, AttributeValue> exclusiveStartKey) {
    return Uni.createFrom()
        .completionStage(
            getDynamoDB()
                .query(
                    builder ->
                        builder
                            .tableName(getTableName())
                            .indexName(indexName)
                            .keyConditionExpression(
                                ZlDbItem.SORT + "=:id and " + sortFieldName + "=:sort")
                            .limit(limit)
                            .exclusiveStartKey(exclusiveStartKey)
                            .expressionAttributeValues(
                                Map.of(
                                    ID_VALUE,
                                    AttributeValue.builder().s(id).build(),
                                    SORT_VALUE,
                                    sort))))
        .map(
            response ->
                Tuple2.of(response.items(), Optional.ofNullable(response.lastEvaluatedKey())))
        .map(this::toResultAndStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findMany(
      final String indexName,
      final Integer limit,
      final Boolean ascending,
      final Map<String, AttributeValue> exclusiveStartKey,
      final String keyConditionExpression,
      final String filterExpression,
      final Map<String, String> expressionAttributeNames,
      final Map<String, AttributeValue> expressionAttributeValues) {
    return Uni.createFrom()
        .completionStage(
            getDynamoDB()
                .query(
                    builder ->
                        builder
                            .tableName(getTableName())
                            .indexName(indexName)
                            .exclusiveStartKey(exclusiveStartKey)
                            .limit(limit)
                            .scanIndexForward(ascending) // true = ascending, false = descending
                            .keyConditionExpression(keyConditionExpression)
                            .filterExpression(filterExpression)
                            .expressionAttributeNames(expressionAttributeNames)
                            .expressionAttributeValues(expressionAttributeValues)))
        .map(
            response -> {
              if (response.scannedCount() == 0) {
                return null;
              }
              return Tuple2.of(response.items(), Optional.ofNullable(response.lastEvaluatedKey()));
            })
        .map(this::toResultAndStartKey);
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>> findManyById(
      final String id, final Integer limit, final Map<String, AttributeValue> exclusiveStartKey) {
    return Uni.createFrom()
        .completionStage(
            getDynamoDB()
                .query(
                    builder ->
                        builder
                            .tableName(getTableName())
                            .keyConditionExpression("id=:id")
                            .limit(limit)
                            .consistentRead(true)
                            .exclusiveStartKey(exclusiveStartKey)
                            .expressionAttributeValues(
                                Map.of(ID_VALUE, AttributeValue.builder().s(id).build()))))
        .map(
            response ->
                Tuple2.of(response.items(), Optional.ofNullable(response.lastEvaluatedKey())))
        .map(this::toResultAndStartKey)
        .replaceIfNullWith(Tuple2.of(List.of(), Optional.empty()));
  }

  public Uni<List<ZlDbItem>> findAllById(final String id) {

    final AtomicReference<Optional<Map<String, AttributeValue>>> startKey =
        new AtomicReference<>(Optional.empty());
    return Uni.createFrom()
        .deferred(() -> findManyById(id, 1000, startKey.get().orElse(null)))
        .map(
            objects -> {
              startKey.set(objects.getItem2());
              return objects.getItem1();
            })
        .repeat()
        .whilst(items -> startKey.get().filter(map -> !map.isEmpty()).isPresent())
        .collect()
        .asList()
        .map(lists -> lists.stream().flatMap(Collection::stream).collect(Collectors.toList()));
  }

  public Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>> toResultAndStartKey(
      final Tuple2<List<Map<String, AttributeValue>>, Optional<Map<String, AttributeValue>>>
          objects) {
    return getDynamoDbHelperUtils().toResultAndStartKey(objects);
  }

  public ZlDbItem createGsiItem(final String id, final String gsi, final String gsiSort) {
    return getDynamoDbHelperUtils().createGsiItem(id, gsi, gsiSort, null);
  }

  public ZlDbItem createGsiItem(final String id, final String gsi, final Long gsiSort) {
    return getDynamoDbHelperUtils().createGsiItem(id, gsi, gsiSort, null);
  }

  public ZlDbItem createGsiItem(
      final String id, final String gsi, final String gsiSort, final Object data) {
    return getDynamoDbHelperUtils().createGsiItem(id, gsi, gsiSort, data);
  }

  public ZlDbItem createGsiItem(
      final String id, final String gsi, final Long gsiSort, final Object data) {
    return getDynamoDbHelperUtils().createGsiItem(id, gsi, gsiSort, data);
  }

  public List<String> createSortKeys(final String key, final List<String>... keys) {
    return getDynamoDbHelperUtils().createSortKeys(key, keys);
  }

  public List<String> createSortKeys(final List<String>... keys) {
    return getDynamoDbHelperUtils().createSortKeys(Arrays.asList(keys));
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

  public boolean startsWithWildcard(final List<String> keys) {
    return getDynamoDbHelperUtils().startsWithWildcard(keys);
  }

  public String createSortKey(final String prefix, final String... keyValues) {
    return createSortKey(prefix, Arrays.stream(keyValues));
  }

  public String createSortKey(final String prefix, final Stream<String> keyValues) {
    return getDynamoDbHelperUtils().createSortKey(prefix, keyValues);
  }

  public Map<String, AttributeValue> generateAttributeValueMap(final String startToken) {
    return getDynamoDbHelperUtils().generateAttributeValueMap(startToken);
  }

  public String generateToken(final Map<String, AttributeValue> exclusiveStartKey) {
    return getDynamoDbHelperUtils().generateToken(exclusiveStartKey);
  }
}
