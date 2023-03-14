package green.zerolabs.commons.dynamo.db.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.model.ZlOrder;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.dynamo.db.model.ConditionalOperator;
import green.zerolabs.commons.dynamo.db.model.LogicalOperator;
import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Created by Triphon Penakov 2022-09-22
 */
@Getter(AccessLevel.PACKAGE)
@Slf4j
class DynamoDbFinderTest {
  public static final String VALUE_0 = ":value0";
  public static final String VALUE_1 = ":value1";
  public static final String ZERO = "0";
  public static final String ONE = "1";
  public static final String KEY = "key";
  DynamoDbFinder finder;
  DynamoDbFinder textFinder;
  DynamoDbFinder numericFinder;

  @BeforeEach
  void beforeEach() {
    final DynamoDbUtils dynamoDbUtils = Mockito.mock(DynamoDbUtils.class);

    final ConverterUtils converterUtils = new ConverterUtils();
    Mockito.when(dynamoDbUtils.getConverterUtils()).thenReturn(converterUtils);

    final JsonUtils jsonUtils = new JsonUtils(new ObjectMapper());
    Mockito.when(dynamoDbUtils.getJsonUtils()).thenReturn(jsonUtils);

    finder = Mockito.spy(new DynamoDbFinder(dynamoDbUtils));
    textFinder = Mockito.spy(new DynamoDbFinder(ZlDbItem.GSI_NAME, dynamoDbUtils));
    numericFinder = Mockito.spy(new DynamoDbFinder(ZlDbItem.GSI_NUMERIC_NAME, dynamoDbUtils));
  }

  @Test
  void findManyTest() {
    Mockito.when(
            getDynamoDbUtils()
                .findMany(
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Integer.class),
                    ArgumentMatchers.nullable(Boolean.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(Map.class)))
        .thenReturn(
            Uni.createFrom()
                .<Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>>>item(
                    Tuple2.of(
                        List.of(), Optional.of(Map.of("any", AttributeValue.builder().build())))));

    final String token = "token";
    Mockito.when(getDynamoDbUtils().generateToken(ArgumentMatchers.anyMap())).thenReturn(token);

    final Tuple2<List<ZlDbItem>, Optional<String>> objects =
        getFinder().findMany().await().atMost(Duration.ofSeconds(1));
    Assertions.assertNotNull(objects);
    Assertions.assertEquals(token, objects.getItem2().orElseThrow());
  }

  @Test
  void findManySoftLimitTest() {
    Mockito.when(
            getDynamoDbUtils()
                .findMany(
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Integer.class),
                    ArgumentMatchers.nullable(Boolean.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(Map.class)))
        .thenReturn(
            Uni.createFrom()
                .item(
                    Tuple2.of(
                        List.of(ZlDbItem.builder().build()),
                        Optional.of(Map.of("any", AttributeValue.builder().build())))))
        .thenReturn(
            Uni.createFrom()
                .item(
                    Tuple2.of(
                        List.of(ZlDbItem.builder().build(), ZlDbItem.builder().build()),
                        Optional.of(Map.of("any", AttributeValue.builder().build())))));

    final String token = "token";
    final String token1 = "token1";
    Mockito.when(getDynamoDbUtils().generateToken(ArgumentMatchers.anyMap()))
        .thenReturn(token)
        .thenReturn(token1);

    final Tuple2<List<ZlDbItem>, Optional<String>> objects =
        getFinder().withSoftLimit(2).findMany().await().atMost(Duration.ofSeconds(1));
    Assertions.assertNotNull(objects);
    Assertions.assertEquals(token1, objects.getItem2().orElseThrow());
    Assertions.assertEquals(3, objects.getItem1().size());
  }

  @Test
  void findDataTest() {
    final String id = "id0";
    Mockito.doReturn(
            Uni.createFrom()
                .item(
                    Tuple2.of(
                        List.of(ZlDbItem.builder().data(Map.of("id", id)).build()),
                        Optional.empty())))
        .when(getFinder())
        .findMany();
    Assertions.assertEquals(
        id,
        getFinder()
            .findData(ZlOrder.class)
            .await()
            .atMost(Duration.ofSeconds(1))
            .getItem1()
            .stream()
            .findAny()
            .orElseThrow()
            .getId());
  }

  @Test
  void findOneDataTest() {
    final String id = "id0";
    Mockito.doReturn(Uni.createFrom().item(ZlDbItem.builder().data(Map.of("id", id)).build()))
        .when(getFinder())
        .findOne(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    Assertions.assertEquals(
        id,
        getFinder()
            .findOne(id, "any", ZlOrder.class)
            .await()
            .atMost(Duration.ofSeconds(1))
            .getId());
  }

  @Test
  void findManyNullResultTest() {
    Mockito.when(
            getDynamoDbUtils()
                .findMany(
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Integer.class),
                    ArgumentMatchers.nullable(Boolean.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(Map.class)))
        .thenReturn(Uni.createFrom().nullItem());

    final Tuple2<List<ZlDbItem>, Optional<String>> objects =
        getFinder().withSoftLimit(200).findMany().await().atMost(Duration.ofSeconds(1));

    Assertions.assertNotNull(objects);
    Assertions.assertTrue(objects.getItem1().isEmpty());
    Assertions.assertTrue(objects.getItem2().isEmpty());
  }

  @Test
  void findManySecondSoftLimitIsNullTest() {
    Mockito.when(
            getDynamoDbUtils()
                .findMany(
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Integer.class),
                    ArgumentMatchers.nullable(Boolean.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(String.class),
                    ArgumentMatchers.nullable(Map.class),
                    ArgumentMatchers.nullable(Map.class)))
        .thenReturn(
            Uni.createFrom()
                .item(
                    Tuple2.of(
                        List.of(ZlDbItem.builder().build()),
                        Optional.of(Map.of("any", AttributeValue.builder().build())))))
        .thenReturn(Uni.createFrom().nullItem());

    final String token = "token";
    Mockito.when(getDynamoDbUtils().generateToken(ArgumentMatchers.anyMap())).thenReturn(token);

    final Tuple2<List<ZlDbItem>, Optional<String>> objects =
        getFinder().withSoftLimit(2).findMany().await().atMost(Duration.ofSeconds(1));
    Assertions.assertNotNull(objects);
    Assertions.assertEquals(1, objects.getItem1().size());
  }

  @Test
  void findOneTest() {
    Mockito.when(
            getDynamoDbUtils().findOne(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
        .thenReturn(Uni.createFrom().nullItem());
    Assertions.assertNull(getFinder().findOne("id", "sort").await().indefinitely());
  }

  @Test
  void toDynamoDbTokenTest() {
    final String any = "any";
    Mockito.when(getDynamoDbUtils().generateAttributeValueMap(ArgumentMatchers.anyString()))
        .thenReturn(Map.of(any, AttributeValue.builder().build()));
    Assertions.assertEquals(
        any,
        getFinder().withStartToken(any).toDynamoDbToken().keySet().stream()
            .findAny()
            .orElseThrow());
  }

  @Test
  void nextValueKeyTest() {
    Assertions.assertEquals(VALUE_0, getFinder().nextValueKey());
    Assertions.assertEquals(VALUE_1, getFinder().nextValueKey());
  }

  @Test
  void toAttributeValuesMapTextTest() {
    final Map<String, AttributeValue> map =
        getFinder()
            .toAttributeValuesMap(ZERO, ONE, "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");

    final List<Tuple2<String, AttributeValue>> list =
        map.entrySet().stream()
            .sequential()
            .map(entry -> Tuple2.of(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    Assertions.assertEquals(VALUE_0, list.get(0).getItem1());
    Assertions.assertEquals(AttributeValue.builder().s(ZERO).build(), list.get(0).getItem2());
    Assertions.assertEquals(VALUE_1, list.get(1).getItem1());
    Assertions.assertEquals(AttributeValue.builder().s(ONE).build(), list.get(1).getItem2());
  }

  @Test
  void toAttributeValuesMapNumericTest() {
    final Map<String, AttributeValue> map =
        getFinder().toAttributeValuesMap(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

    final List<Tuple2<String, AttributeValue>> list =
        map.entrySet().stream()
            .sequential()
            .map(entry -> Tuple2.of(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

    Assertions.assertEquals(VALUE_0, list.get(0).getItem1());
    Assertions.assertEquals(AttributeValue.builder().n(ZERO).build(), list.get(0).getItem2());
    Assertions.assertEquals(VALUE_1, list.get(1).getItem1());
    Assertions.assertEquals(AttributeValue.builder().n(ONE).build(), list.get(1).getItem2());
  }

  @Test
  void toConditionAndAttributesTest() {
    final Tuple2<String, Map<String, AttributeValue>> objects =
        getFinder().toConditionAndAttributes(KEY, ConditionalOperator.EQUAL, ONE);

    Assertions.assertEquals(KEY + "=" + VALUE_0, objects.getItem1());
    Assertions.assertEquals(VALUE_0, getFinder().getKeyByIndex(objects.getItem2(), 0));
  }

  @Test
  void withTextIndexTest() {
    Assertions.assertEquals(ZlDbItem.GSI_NAME, getTextFinder().getIndexName());
  }

  @Test
  void withNumericIndexTest() {
    Assertions.assertEquals(ZlDbItem.GSI_NUMERIC_NAME, getNumericFinder().getIndexName());
  }

  @Test
  void withTest() {
    final String startToken = "a";
    final int limit = 1;
    final boolean ascending = false;
    getFinder().withLimit(limit).withAscending(ascending).withStartToken(startToken);

    Assertions.assertEquals(startToken, getFinder().getStartToken());
    Assertions.assertEquals(limit, getFinder().getLimit());
    Assertions.assertEquals(ascending, getFinder().getAscending());
  }

  @Test
  void addIdKeyConditionTest() {
    getFinder().addKeyCondition("value");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addIdKeyConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("id=:value0", keyConditionExpression.toString());
  }

  @Test
  void addIdAndSortKeyConditionTest() {
    getFinder().addKeyCondition("value").addSortKeyEqualCondition("any");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addIdKeyConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("id=:value0 AND sort=:value1", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyConditionTest() {
    getTextFinder().addKeyCondition("value");
    final StringBuilder keyConditionExpression = getTextFinder().getKeyConditionExpression();
    log.warn("addIdKeyConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort=:value0", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyEqualConditionTest() {
    getFinder().addSortKeyEqualCondition("value");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addSortKeyEqualConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort=:value0", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyEqualConditionTest() {
    getFinder().addSortKeyEqualCondition(1L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addSortKeyEqualConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort=:value0", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyLessThanConditionTest() {
    getFinder().addSortKeyLessThanCondition("value");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addSortKeyLessThanConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort<:value0", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyLessThanConditionTest() {
    getFinder().addSortKeyLessThanCondition(1L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addSortKeyLessThanConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort<:value0", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyLessThanOrEqualConditionTest() {
    getFinder().addSortKeyLessThanOrEqualCondition("value");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyLessThanOrEqualConditionTest keyConditionExpression: {}",
        keyConditionExpression);
    Assertions.assertEquals("sort<=:value0", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyLessThanOrEqualConditionTest() {
    getFinder().addSortKeyLessThanOrEqualCondition(1L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyLessThanOrEqualConditionTest keyConditionExpression: {}",
        keyConditionExpression);
    Assertions.assertEquals("sort<=:value0", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyGreaterThanConditionTest() {
    getFinder().addSortKeyGreaterThanCondition("value");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyGreaterThanConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort>:value0", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyGreaterThanConditionTest() {
    getFinder().addSortKeyGreaterThanCondition(1L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyGreaterThanConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort>:value0", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyGreaterThanOrEqualConditionTest() {
    getFinder().addSortKeyGreaterThanOrEqualCondition("value");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyGreaterThanOrEqualConditionTest keyConditionExpression: {}",
        keyConditionExpression);
    Assertions.assertEquals("sort>=:value0", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyGreaterThanOrEqualConditionTest() {
    getFinder().addSortKeyGreaterThanOrEqualCondition(1L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyGreaterThanOrEqualConditionTest keyConditionExpression: {}",
        keyConditionExpression);
    Assertions.assertEquals("sort>=:value0", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyBetweenConditionTest() {
    getFinder().addSortKeyBetweenCondition("a", "b");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addSortKeyBetweenConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort between :value0 and :value1", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyBetweenConditionTest() {
    getFinder().addSortKeyBetweenCondition(1L, 2L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn("addSortKeyBetweenConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("sort between :value0 and :value1", keyConditionExpression.toString());
  }

  @Test
  void addSortKeyBeginsWithConditionTest() {
    getFinder().addSortKeyBeginsWithCondition("a");
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyBeginsWithConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("begins_with(sort, :value0)", keyConditionExpression.toString());
  }

  @Test
  void addNumberSortKeyBeginsWithConditionTest() {
    getFinder().addSortKeyBeginsWithCondition(1L);
    final StringBuilder keyConditionExpression = getFinder().getKeyConditionExpression();
    log.warn(
        "addSortKeyBeginsWithConditionTest keyConditionExpression: {}", keyConditionExpression);
    Assertions.assertEquals("begins_with(sort, :value0)", keyConditionExpression.toString());
  }

  @Test
  void addDataFilterEqualConditionTest() {
    getFinder().addDataFilterEqualCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterEqualConditionTest filterConditionExpression: {}", filterConditionExpression);
    Assertions.assertEquals("#data.path=:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterEqualConditionLongTest() {
    getFinder().addDataFilterEqualCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterEqualConditionTest filterConditionExpression: {}", filterConditionExpression);
    Assertions.assertEquals("#data.path=:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterNotEqualConditionTest() {
    getFinder().addDataFilterNotEqualCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterNotEqualConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path<>:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterNotEqualConditionLongTest() {
    getFinder().addDataFilterNotEqualCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterNotEqualConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path<>:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void multipleDataFilterConditionsTest() {
    getFinder()
        .addDataFilterEqualCondition("path", LogicalOperator.OR, "a")
        .addDataFilterEqualCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "multipleDataFilterConditionsTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "#data.path=:value0 OR #data.path=:value1", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterLessThanConditionTest() {
    getFinder().addDataFilterLessThanCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterLessThanConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path<:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterLessThanConditionLongTest() {
    getFinder().addDataFilterLessThanCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterLessThanConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path<:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterLessThanOrEqualConditionTest() {
    getFinder().addDataFilterLessThanOrEqualCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterLessThanOrEqualConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path<=:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterLessThanOrEqualConditionLongTest() {
    getFinder().addDataFilterLessThanOrEqualCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterLessThanOrEqualConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path<=:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterGreaterThanConditionTest() {
    getFinder().addDataFilterGreaterThanCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterGreaterThanConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path>:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterGreaterThanConditionLongTest() {
    getFinder().addDataFilterGreaterThanCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterGreaterThanConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path>:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterGreaterThanOrEqualConditionTest() {
    getFinder().addDataFilterGreaterThanOrEqualCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterGreaterThanOrEqualConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path>=:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterGreaterThanOrEqualConditionLongTest() {
    getFinder().addDataFilterGreaterThanOrEqualCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterGreaterThanOrEqualConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("#data.path>=:value0", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterBetweenConditionTest() {
    getFinder().addDataFilterBetweenCondition("path", LogicalOperator.OR, "a", "b");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterBetweenConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "#data.path between :value0 and :value1", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterBetweenConditionLongTest() {
    getFinder().addDataFilterBetweenCondition("path", LogicalOperator.OR, 1L, 2L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterBetweenConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "#data.path between :value0 and :value1", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterBeginsWithConditionTest() {
    getFinder().addDataFilterBeginsWithCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterBeginsWithConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "begins_with(#data.path, :value0)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterBeginsWithConditionLongTest() {
    getFinder().addDataFilterBeginsWithCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterBeginsWithConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "begins_with(#data.path, :value0)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterContainsConditionTest() {
    getFinder().addDataFilterContainsCondition("path", LogicalOperator.OR, "a");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterContainsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("contains(#data.path, :value0)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterContainsConditionLongTest() {
    getFinder().addDataFilterContainsCondition("path", LogicalOperator.OR, 1L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterContainsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("contains(#data.path, :value0)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterContainsConditionMapTest() {
    getFinder()
        .addDataFilterContainsCondition("path", LogicalOperator.OR, Map.of("key1", "value1"));
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterContainsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("contains(#data.path, :value0)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterInConditionTest() {
    getFinder().addDataFilterInCondition("path", LogicalOperator.OR, "a", "b");
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterInConditionTest filterConditionExpression: {}", filterConditionExpression);
    Assertions.assertEquals(
        "#data.path IN (:value0,:value1)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterInConditionLongTest() {
    getFinder().addDataFilterInCondition("path", LogicalOperator.OR, 1L, 2L);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterInConditionTest filterConditionExpression: {}", filterConditionExpression);
    Assertions.assertEquals(
        "#data.path IN (:value0,:value1)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterExistsConditionTest() {
    getFinder().addDataFilterExistsCondition("path", LogicalOperator.OR);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterExistsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals("attribute_exists(#data.path)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterNotExistsConditionTest() {
    getFinder().addDataFilterNotExistsCondition("path", LogicalOperator.OR);
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterNotExistsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "attribute_not_exists(#data.path)", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterGroupsConditionTest() {
    getFinder().addDataFilterBeginGroupCondition(LogicalOperator.AND);
    getFinder().addDataFilterNotExistsCondition("path", LogicalOperator.OR);
    getFinder().addDataFilterEndGroupCondition();
    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterGroupsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "(attribute_not_exists(#data.path))", filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void addDataFilterMultipleGroupsConditionTest() {
    getFinder()
        .addDataFilterBeginGroupCondition(LogicalOperator.AND)
        .addDataFilterNotExistsCondition("path", LogicalOperator.OR)
        .addDataFilterExistsCondition("path", LogicalOperator.AND)
        .addDataFilterEndGroupCondition()
        .addDataFilterBeginGroupCondition(LogicalOperator.AND)
        .addDataFilterEqualCondition("path", LogicalOperator.OR, "any")
        .addDataFilterBetweenCondition("path", LogicalOperator.AND, 1L, 3L)
        .addDataFilterEndGroupCondition()
        .addDataFilterBeginGroupCondition(LogicalOperator.OR)
        .addDataFilterLessThanCondition("path", LogicalOperator.OR, "any")
        .addDataFilterGreaterThanOrEqualCondition("path", LogicalOperator.AND, 1L)
        .addDataFilterContainsCondition("path", LogicalOperator.OR, 1L)
        .addDataFilterEndGroupCondition();

    final StringBuilder filterConditionExpression = getFinder().getFilterConditionExpression();
    log.warn(
        "addDataFilterMultipleGroupsConditionTest filterConditionExpression: {}",
        filterConditionExpression);
    Assertions.assertEquals(
        "(attribute_not_exists(#data.path) AND attribute_exists(#data.path)) "
            + "AND ( #data.path=:value0 AND #data.path between :value1 and :value2) "
            + "OR ( #data.path<:value3 AND #data.path>=:value4 OR contains(#data.path, :value5))",
        filterConditionExpression.toString());

    Assertions.assertFalse(
        CollectionUtils.isNullOrEmpty(getFinder().getExpressionAttributeNames()));
  }

  @Test
  void toCondition_fail_Test() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> getFinder().toCondition("key", ConditionalOperator.ERROR, Map.of()));
  }

  @Test
  void getKeyByIndexNullTest() {
    Assertions.assertNull(getFinder().getKeyByIndex(Map.of(), 0));
  }

  @Test
  void getIndexKeyName() {
    final String indexKeyName = getFinder().getIndexKeyName();
    Assertions.assertEquals(ZlDbItem.SORT, indexKeyName);
  }

  @Test
  void getTextIndexKeyName() {
    final String indexKeyName = getTextFinder().getIndexKeyName();
    Assertions.assertEquals(ZlDbItem.GSI_SORT, indexKeyName);
  }

  @Test
  void getNumericIndexKeyName() {
    final String indexKeyName = getNumericFinder().getIndexKeyName();
    Assertions.assertEquals(ZlDbItem.GSI_NUMERIC_SORT, indexKeyName);
  }

  private DynamoDbUtils getDynamoDbUtils() {
    return getFinder().getDynamoDbUtils();
  }
}


