package green.zerolabs.commons.dynamo.db.utils;

import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.dynamo.db.model.ConditionalOperator;
import green.zerolabs.commons.dynamo.db.model.LogicalOperator;
import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bk.aws.dynamo.util.JsonAttributeValueUtil;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

/***
 * Some documentation:
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.html
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.OperatorsAndFunctions.html
 *
 * <p>Created by Triphon Penakov 2022-09-22
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
@SuppressWarnings("unused")
public class DynamoDbFinder {

  public static final String SPACE = " ";
  public static final String COMMA = ",";

  public static final Map<String, String> EXPRESSION_ATTRIBUTE_NAMES_MAP =
      Map.of(ZlDbItem.DATA_KEY, ZlDbItem.DATA);
  public static final String START_BRACKET = "(";
  public static final String END_BRACKET = ")";

  private final String indexName;

  @Setter(AccessLevel.PROTECTED)
  private Integer limit = 10;

  @Setter(AccessLevel.PROTECTED)
  private Integer softLimit;

  @Setter(AccessLevel.PROTECTED)
  private Boolean ascending = true;

  @Setter(AccessLevel.PROTECTED)
  private String startToken;

  private final DynamoDbUtils dynamoDbUtils;

  @Getter @Setter private Map<String, String> expressionAttributeNames = null;

  @Setter(AccessLevel.PROTECTED)
  private boolean bracketJustOpened = false;

  private final Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
  private final StringBuilder keyConditionExpression = new StringBuilder();
  private final StringBuilder filterConditionExpression = new StringBuilder();
  private final AtomicInteger valuesIndex = new AtomicInteger(0);

  public DynamoDbFinder(final DynamoDbUtils dynamoDbUtils) {
    this(null, dynamoDbUtils);
  }

  public DynamoDbFinder(final String indexName, final DynamoDbUtils dynamoDbUtils) {
    this.indexName = indexName;
    this.dynamoDbUtils = dynamoDbUtils;
  }

  public Uni<Tuple2<List<ZlDbItem>, Optional<String>>> findMany() {
    return Optional.ofNullable(getSoftLimit())
        .map(
            atLeast -> {
              setLimit(atLeast > getLimit() ? atLeast : getLimit());
              final List<ZlDbItem> result = new ArrayList<>();
              final AtomicReference<String> token = new AtomicReference<>(getStartToken());

              return Uni.createFrom()
                  .deferred(() -> findNext(token.get()))
                  .onItem()
                  .ifNull()
                  .switchTo(
                      () -> {
                        token.set(null);
                        return Uni.createFrom().item(Tuple2.of(List.of(), Optional.empty()));
                      })
                  .onItem()
                  .ifNotNull()
                  .transform(
                      objects ->
                          objects
                              .mapItem1(
                                  zlDbItems -> {
                                    result.addAll(zlDbItems);
                                    return result;
                                  })
                              .mapItem2(
                                  s -> {
                                    token.set(s.orElse(null));
                                    return s;
                                  }))
                  .repeat()
                  .whilst(
                      objects ->
                          objects.getItem2().isPresent() && atLeast > objects.getItem1().size())
                  .collect()
                  .asList()
                  .onItem()
                  .ifNotNull()
                  .transform(unused -> Tuple2.of(result, Optional.ofNullable(token.get())));
            })
        .orElseGet(() -> findManyInternal());
  }

  public <T> Uni<Tuple2<List<T>, Optional<String>>> findData(final Class<T> clazz) {
    return findMany().onItem().ifNotNull().transform(objects -> extractData(clazz, objects));
  }

  <T> Tuple2<List<T>, Optional<String>> extractData(
      final Class<T> clazz, final Tuple2<List<ZlDbItem>, Optional<String>> objects) {
    return objects.mapItem1(
        zlDbItems ->
            zlDbItems.stream()
                .map(ZlDbItem::getData)
                .map(o -> getJsonUtils().fromMap((Map<String, Object>) o, clazz))
                .collect(Collectors.toList()));
  }

  protected Uni<Tuple2<List<ZlDbItem>, Optional<String>>> findManyInternal() {
    final Map<String, AttributeValue> exclusiveStartKey = toDynamoDbToken();
    final String conditionExpression = toDynamoDbConditionExpression();
    final String filterExpression = toDynamoDbFilterExpression();
    log.debug(
        "findMany: indexName={}|limit={}|ascending={}|exclusiveStartKey={}|conditionExpression={}|filterExpression={},"
            + "expressionAttributeNames={}|expressionAttributeValues={} ",
        getIndexName(),
        getLimit(),
        getAscending(),
        exclusiveStartKey,
        conditionExpression,
        filterExpression,
        getExpressionAttributeNames(),
        getExpressionAttributeValues());
    return getDynamoDbUtils()
        .findMany(
            getIndexName(),
            getLimit(),
            getAscending(),
            exclusiveStartKey,
            conditionExpression,
            filterExpression,
            getExpressionAttributeNames(),
            getExpressionAttributeValues())
        .onItem()
        .ifNotNull()
        .transform(
            objects ->
                objects.mapItem2(
                    optional ->
                        optional
                            .map(map -> getDynamoDbUtils().generateToken(map))
                            .filter(StringUtils::isNotBlank)));
  }

  protected Uni<Tuple2<List<ZlDbItem>, Optional<String>>> findNext(final String nextToken) {
    return withStartToken(nextToken).findManyInternal();
  }

  public Uni<ZlDbItem> findOne(final String id, final String sort) {
    return getDynamoDbUtils().findOne(id, sort);
  }

  public <T> Uni<T> findOne(final String id, final String sort, final Class<T> clazz) {
    return findOne(id, sort)
        .onItem()
        .ifNotNull()
        .transform(zlDbItem -> zlDbItem.getData())
        .onItem()
        .ifNotNull()
        .transform(o -> getJsonUtils().fromMap((Map<String, Object>) o, clazz));
  }

  public DynamoDbFinder withLimit(final Integer limit) {
    setLimit(limit);
    return this;
  }

  public DynamoDbFinder withSoftLimit(final Integer limit) {
    setSoftLimit(limit);
    return this;
  }

  public DynamoDbFinder withAscending(final Boolean ascending) {
    setAscending(ascending);
    return this;
  }

  public DynamoDbFinder withStartToken(final String startToken) {
    setStartToken(startToken);
    return this;
  }

  public DynamoDbFinder addKeyCondition(final String value) {
    final String key = StringUtils.isBlank(getIndexName()) ? ZlDbItem.ID : ZlDbItem.SORT;
    fillKeyExpression(toConditionAndAttributes(key, ConditionalOperator.EQUAL, value));
    return this;
  }

  public DynamoDbFinder addSortKeyEqualCondition(final String value) {
    return addSortKeyCondition(ConditionalOperator.EQUAL, value);
  }

  public DynamoDbFinder addSortKeyEqualCondition(final Long value) {
    return addSortKeyCondition(ConditionalOperator.EQUAL, value);
  }

  public DynamoDbFinder addSortKeyLessThanCondition(final String value) {
    return addSortKeyCondition(ConditionalOperator.LESS, value);
  }

  public DynamoDbFinder addSortKeyLessThanCondition(final Long value) {
    return addSortKeyCondition(ConditionalOperator.LESS, value);
  }

  public DynamoDbFinder addSortKeyLessThanOrEqualCondition(final String value) {
    return addSortKeyCondition(ConditionalOperator.LESS_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addSortKeyLessThanOrEqualCondition(final Long value) {
    return addSortKeyCondition(ConditionalOperator.LESS_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addSortKeyGreaterThanCondition(final String value) {
    return addSortKeyCondition(ConditionalOperator.GREATER, value);
  }

  public DynamoDbFinder addSortKeyGreaterThanCondition(final Long value) {
    return addSortKeyCondition(ConditionalOperator.GREATER, value);
  }

  public DynamoDbFinder addSortKeyGreaterThanOrEqualCondition(final String value) {
    return addSortKeyCondition(ConditionalOperator.GREATER_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addSortKeyGreaterThanOrEqualCondition(final Long value) {
    return addSortKeyCondition(ConditionalOperator.GREATER_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addSortKeyBetweenCondition(final String... values) {
    return addSortKeyCondition(ConditionalOperator.BETWEEN, values);
  }

  public DynamoDbFinder addSortKeyBetweenCondition(final Long... values) {
    return addSortKeyCondition(ConditionalOperator.BETWEEN, values);
  }

  public DynamoDbFinder addSortKeyBeginsWithCondition(final String value) {
    return addSortKeyCondition(ConditionalOperator.BEGINS_WITH, value);
  }

  public DynamoDbFinder addSortKeyBeginsWithCondition(final Long value) {
    return addSortKeyCondition(ConditionalOperator.BEGINS_WITH, value);
  }

  public DynamoDbFinder addDataFilterEqualCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.EQUAL, value);
  }

  public DynamoDbFinder addDataFilterEqualCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.EQUAL, value);
  }

  public DynamoDbFinder addDataFilterNotEqualCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.NOT_EQUAL, value);
  }

  public DynamoDbFinder addDataFilterNotEqualCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.NOT_EQUAL, value);
  }

  public DynamoDbFinder addDataFilterLessThanCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.LESS, value);
  }

  public DynamoDbFinder addDataFilterLessThanCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.LESS, value);
  }

  public DynamoDbFinder addDataFilterLessThanOrEqualCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(
        path, logicalOperator, ConditionalOperator.LESS_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addDataFilterLessThanOrEqualCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(
        path, logicalOperator, ConditionalOperator.LESS_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addDataFilterGreaterThanCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.GREATER, value);
  }

  public DynamoDbFinder addDataFilterGreaterThanCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.GREATER, value);
  }

  public DynamoDbFinder addDataFilterGreaterThanOrEqualCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(
        path, logicalOperator, ConditionalOperator.GREATER_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addDataFilterGreaterThanOrEqualCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(
        path, logicalOperator, ConditionalOperator.GREATER_THAN_OR_EQUAL, value);
  }

  public DynamoDbFinder addDataFilterBetweenCondition(
      final String path, final LogicalOperator logicalOperator, final String... values) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.BETWEEN, values);
  }

  public DynamoDbFinder addDataFilterBetweenCondition(
      final String path, final LogicalOperator logicalOperator, final Long... values) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.BETWEEN, values);
  }

  public DynamoDbFinder addDataFilterBeginsWithCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.BEGINS_WITH, value);
  }

  public DynamoDbFinder addDataFilterBeginsWithCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.BEGINS_WITH, value);
  }

  public DynamoDbFinder addDataFilterContainsCondition(
      final String path, final LogicalOperator logicalOperator, final String value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.CONTAINS, value);
  }

  public DynamoDbFinder addDataFilterContainsCondition(
      final String path, final LogicalOperator logicalOperator, final Long value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.CONTAINS, value);
  }

  public DynamoDbFinder addDataFilterContainsCondition(
      final String path, final LogicalOperator logicalOperator, final Map value) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.CONTAINS, value);
  }

  public DynamoDbFinder addDataFilterInCondition(
      final String path, final LogicalOperator logicalOperator, final String... values) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.IN, values);
  }

  public DynamoDbFinder addDataFilterInCondition(
      final String path, final LogicalOperator logicalOperator, final Long... values) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.IN, values);
  }

  public DynamoDbFinder addDataFilterExistsCondition(
      final String path, final LogicalOperator logicalOperator) {
    return addDataFilterExpression(path, logicalOperator, ConditionalOperator.EXISTS, (Long) null);
  }

  public DynamoDbFinder addDataFilterNotExistsCondition(
      final String path, final LogicalOperator logicalOperator) {
    return addDataFilterExpression(
        path, logicalOperator, ConditionalOperator.NOT_EXISTS, (String) null);
  }

  public DynamoDbFinder addDataFilterBeginGroupCondition(final LogicalOperator logicalOperator) {
    return addDataFilterExpression(
        null, logicalOperator, ConditionalOperator.BEGIN_GROUP, (String) null);
  }

  public DynamoDbFinder addDataFilterEndGroupCondition() {
    return addDataFilterExpression(
        null, LogicalOperator.AND, ConditionalOperator.END_GROUP, (String) null);
  }

  DynamoDbFinder addDataFilterExpression(
      final String path,
      final LogicalOperator logicalOperator,
      final ConditionalOperator conditionalOperator,
      final String... values) {
    fillFilterExpression(
        logicalOperator,
        toConditionAndAttributes(ZlDbItem.DATA_KEY + "." + path, conditionalOperator, values));
    return this;
  }

  DynamoDbFinder addDataFilterExpression(
      final String path,
      final LogicalOperator logicalOperator,
      final ConditionalOperator conditionalOperator,
      final Long... values) {
    fillFilterExpression(
        logicalOperator,
        toConditionAndAttributes(ZlDbItem.DATA_KEY + "." + path, conditionalOperator, values));
    return this;
  }

  DynamoDbFinder addDataFilterExpression(
      final String path,
      final LogicalOperator logicalOperator,
      final ConditionalOperator conditionalOperator,
      final Map... values) {
    fillFilterExpression(
        logicalOperator,
        toConditionAndAttributes(ZlDbItem.DATA_KEY + "." + path, conditionalOperator, values));
    return this;
  }

  void fillFilterExpression(
      final LogicalOperator logicalOperator,
      final Tuple2<String, Map<String, AttributeValue>> objects) {
    if (Objects.isNull(getExpressionAttributeNames())) {
      setExpressionAttributeNames(EXPRESSION_ATTRIBUTE_NAMES_MAP);
    }
    getExpressionAttributeValues().putAll(objects.getItem2());

    final String expression = objects.getItem1();
    if (getFilterConditionExpression().length() <= 1
        || StringUtils.equals(END_BRACKET, expression)) {
      getFilterConditionExpression().append(expression);
      setBracketJustOpened(false);
      return;
    }
    getFilterConditionExpression().append(SPACE);
    if (isBracketJustOpened()) {
      if (StringUtils.equals(START_BRACKET, expression)) {
        getFilterConditionExpression().append(logicalOperator);
        getFilterConditionExpression().append(SPACE);
      } else {
        setBracketJustOpened(false);
      }
    } else {
      getFilterConditionExpression().append(logicalOperator);
      getFilterConditionExpression().append(SPACE);
    }

    getFilterConditionExpression().append(expression);
  }

  DynamoDbFinder addSortKeyCondition(
      final ConditionalOperator conditionalOperator, final String... values) {
    final String key = getIndexKeyName();
    fillKeyExpression(toConditionAndAttributes(key, conditionalOperator, values));
    return this;
  }

  DynamoDbFinder addSortKeyCondition(
      final ConditionalOperator conditionalOperator, final Long... values) {
    final String key = getIndexKeyName();
    fillKeyExpression(toConditionAndAttributes(key, conditionalOperator, values));
    return this;
  }

  String getIndexKeyName() {
    final String key;
    if (StringUtils.equals(ZlDbItem.GSI_NAME, getIndexName())) {
      key = ZlDbItem.GSI_SORT;
    } else if (StringUtils.equals(ZlDbItem.GSI_NUMERIC_NAME, getIndexName())) {
      key = ZlDbItem.GSI_NUMERIC_SORT;
    } else {
      key = ZlDbItem.SORT;
    }
    return key;
  }

  void fillKeyExpression(final Tuple2<String, Map<String, AttributeValue>> objects) {
    getExpressionAttributeValues().putAll(objects.getItem2());
    if (getKeyConditionExpression().length() <= 1) {
      getKeyConditionExpression().append(objects.getItem1());
      return;
    }
    getKeyConditionExpression().append(SPACE);
    getKeyConditionExpression().append(LogicalOperator.AND.name());
    getKeyConditionExpression().append(SPACE);
    getKeyConditionExpression().append(objects.getItem1());
  }

  Tuple2<String, Map<String, AttributeValue>> toConditionAndAttributes(
      final String key, final ConditionalOperator conditionalOperator, final String... values) {
    final String condition;
    final Map<String, AttributeValue> attributeValueMap = toAttributeValuesMap(values);
    condition = toCondition(key, conditionalOperator, attributeValueMap);
    return Tuple2.of(condition, attributeValueMap);
  }

  Tuple2<String, Map<String, AttributeValue>> toConditionAndAttributes(
      final String key, final ConditionalOperator conditionalOperator, final Long... values) {
    final String condition;
    final Map<String, AttributeValue> attributeValueMap = toAttributeValuesMap(values);
    condition = toCondition(key, conditionalOperator, attributeValueMap);
    return Tuple2.of(condition, attributeValueMap);
  }

  Tuple2<String, Map<String, AttributeValue>> toConditionAndAttributes(
      final String key, final ConditionalOperator conditionalOperator, final Map... values) {
    final String condition;
    final Map<String, AttributeValue> attributeValueMap = toAttributeValuesMap(values);
    condition = toCondition(key, conditionalOperator, attributeValueMap);
    return Tuple2.of(condition, attributeValueMap);
  }

  String toCondition(
      final String key,
      final ConditionalOperator conditionalOperator,
      final Map<String, AttributeValue> map) {
    final String condition;
    switch (conditionalOperator) {
      case EQUAL:
        condition = MessageFormat.format("{0}={1}", key, getKeyByIndex(map, 0));
        break;
      case NOT_EQUAL:
        condition = MessageFormat.format("{0}<>{1}", key, getKeyByIndex(map, 0));
        break;
      case GREATER:
        condition = MessageFormat.format("{0}>{1}", key, getKeyByIndex(map, 0));
        break;
      case GREATER_THAN_OR_EQUAL:
        condition = MessageFormat.format("{0}>={1}", key, getKeyByIndex(map, 0));
        break;
      case LESS:
        condition = MessageFormat.format("{0}<{1}", key, getKeyByIndex(map, 0));
        break;
      case LESS_THAN_OR_EQUAL:
        condition = MessageFormat.format("{0}<={1}", key, getKeyByIndex(map, 0));
        break;
      case BETWEEN:
        condition =
            MessageFormat.format(
                "{0} between {1} and {2}", key, getKeyByIndex(map, 0), getKeyByIndex(map, 1));
        break;
      case CONTAINS:
        condition = MessageFormat.format("contains({0}, {1})", key, getKeyByIndex(map, 0));
        break;
      case BEGINS_WITH:
        condition = MessageFormat.format("begins_with({0}, {1})", key, getKeyByIndex(map, 0));
        break;
      case IN:
        condition =
            MessageFormat.format(
                "{0} IN ({1})",
                key, map.keySet().stream().sequential().collect(Collectors.joining(COMMA)));
        break;
      case NOT_EXISTS:
        condition = MessageFormat.format("attribute_not_exists({0})", key);
        break;
      case EXISTS:
        condition = MessageFormat.format("attribute_exists({0})", key);
        break;
      case BEGIN_GROUP:
        setBracketJustOpened(true);
        condition = START_BRACKET;
        break;
      case END_GROUP:
        condition = END_BRACKET;
        break;
      default:
        throw new RuntimeException("not supported conditional operator: " + conditionalOperator);
    }
    return condition;
  }

  String getKeyByIndex(final Map<String, AttributeValue> map, final long index) {
    if (CollectionUtils.isNullOrEmpty(map)) {
      return null;
    }
    return map.keySet().stream().skip(index).findFirst().orElse(null);
  }

  Map<String, AttributeValue> toAttributeValuesMap(final String... values) {
    return toAttributeValuesMap(
        Arrays.stream(values)
            .filter(StringUtils::isNotBlank)
            .map(o -> AttributeValue.builder().s(o).build()));
  }

  Map<String, AttributeValue> toAttributeValuesMap(final Long... values) {
    return toAttributeValuesMap(
        Arrays.stream(values)
            .filter(Objects::nonNull)
            .map(o -> AttributeValue.builder().n(getConverterUtils().toString(o)).build()));
  }

  Map<String, AttributeValue> toAttributeValuesMap(final Map... values) {
    return toAttributeValuesMap(
        Arrays.stream(values)
            .filter(Objects::nonNull)
            .map(
                o ->
                    JsonAttributeValueUtil.toAttributeValue(
                        getJsonUtils().toStringLazy(o).toString(),
                        getJsonUtils().getObjectMapper())));
  }

  private Map<String, AttributeValue> toAttributeValuesMap(final Stream<AttributeValue> values) {
    final Map<String, AttributeValue> result = new TreeMap<>();

    values.forEach(o -> result.put(nextValueKey(), o));

    return result;
  }

  String nextValueKey() {
    return MessageFormat.format(":value{0,number,#}", getValuesIndex().getAndIncrement());
  }

  String toDynamoDbFilterExpression() {
    return Optional.of(getFilterConditionExpression().toString())
        .filter(StringUtils::isNotBlank)
        .orElse(null);
  }

  String toDynamoDbConditionExpression() {
    return Optional.of(getKeyConditionExpression().toString())
        .filter(StringUtils::isNotBlank)
        .orElse(null);
  }

  Map<String, AttributeValue> toDynamoDbToken() {
    return Optional.ofNullable(getStartToken())
        .map(s -> getDynamoDbUtils().generateAttributeValueMap(s))
        .orElse(null);
  }

  private ConverterUtils getConverterUtils() {
    return getDynamoDbUtils().getConverterUtils();
  }

  private JsonUtils getJsonUtils() {
    return getDynamoDbUtils().getJsonUtils();
  }
}
