package green.zerolabs.commons.dynamo.db.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import green.zerolabs.commons.dynamo.db.exception.InvalidPaginationTokenException;
import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import green.zerolabs.commons.dynamo.db.model.ZlDbPutItemPackage;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static green.zerolabs.commons.dynamo.db.utils.DynamoDbUtils.MAX_ITEMS_IN_TRANSACTION;

/*
 * Created by triphon 27.04.22 г.
 */
@Getter
@Slf4j
class DynamoDbHelperUtilsTest {

  UnitTestUtils unitTestUtils;
  DynamoDbUtils dynamoDbUtils;
  DynamoDbHelperUtils dynamoDbHelperUtils;

  @BeforeEach
  void beforeEach() {
    unitTestUtils = UnitTestUtils.of();
    dynamoDbHelperUtils = unitTestUtils.getAllUtils().getDynamoDbUtils().getDynamoDbHelperUtils();
    dynamoDbUtils = unitTestUtils.getAllUtils().getDynamoDbUtils();
  }

  @Test
  void createSortKeysTest() {
    final List<String> list0 = List.of("00", "01", "02", "03", "04", "05", "06");
    final List<String> list1 = List.of("10", "11", "12", "13", "14", "15");
    final List<String> list2 = List.of("20", "21", "22", "23", "24");
    final List<String> list3 = List.of("30", "31", "32", "33");
    final List<String> list4 = List.of("40", "41", "42", DynamoDbUtils.WILDCARD);
    Assertions.assertEquals(list0, getDynamoDbHelperUtils().createSortKeys(list0));

    final List<String> keys = getDynamoDbHelperUtils().createSortKeys(List.of("99"), list0);
    log.info("keys size={}: \n{}", keys.size(), printKeys(keys));
    keys.forEach(
        s -> Assertions.assertEquals(2, StringUtils.split(s, DynamoDbUtils.DELIMITER).length));

    final List<String> keys1 = getDynamoDbHelperUtils().createSortKeys(list2, list3, list4);
    log.info("keys1 size={}: \n{}", keys1.size(), printKeys(keys1));
    keys1.forEach(
        s -> Assertions.assertEquals(3, StringUtils.split(s, DynamoDbUtils.DELIMITER).length));

    final List<String> keys2 =
        getDynamoDbHelperUtils().createSortKeys(list0, list1, list2, list3, list4);
    log.info("keys2 size={}: \n{}", keys2.size(), printKeys(keys2));
    keys2.forEach(
        s -> Assertions.assertEquals(5, StringUtils.split(s, DynamoDbUtils.DELIMITER).length));

    final List<String> keys3 = getDynamoDbHelperUtils().createSortKeys("99", list3, list4);
    log.info("keys3 size={}: \n{}", keys3.size(), printKeys(keys3));
    keys3.forEach(
        s -> {
          Assertions.assertEquals(3, StringUtils.split(s, DynamoDbUtils.DELIMITER).length);
          Assertions.assertFalse(getDynamoDbHelperUtils().startsWithWildcard(List.of(s)));
        });

    Assertions.assertEquals(0, getDynamoDbHelperUtils().createSortKeys(List.of()).size());
  }

  @Test
  void startWithWildcardEmptyListTest() {
    Assertions.assertFalse(getDynamoDbHelperUtils().startsWithWildcard(List.of()));
  }

  @Test
  void splitAndTransformTest() {
    final List<ZlDbItem> items =
        getUnitTestUtils()
            .readJson(
                "/input/dynamodb/dynamodb-utils-test-getTransactWriteItems-input-ZlDbItems.json",
                new TypeReference<>() {});

    final Stream<ZlDbPutItemPackage> stream =
        items.stream().map(item -> ZlDbPutItemPackage.builder().item(item).runFirst(true).build());
    final List<TransactWriteItem> transactWriteItems =
        getDynamoDbHelperUtils()
            .splitAndTransform(
                stream, zlDbPutItemPackage -> Boolean.TRUE.equals(zlDbPutItemPackage.getRunFirst()))
            .collect(Collectors.toList());

    Assertions.assertEquals(1, transactWriteItems.size());
    Assertions.assertNotNull(transactWriteItems.get(0).put());
    Assertions.assertNull(transactWriteItems.get(0).put().item().get("gsiSort"));
  }

  @Test
  void splitAndTransformTest_WithGsiSort() {
    final List<ZlDbItem> items =
        getUnitTestUtils()
            .readJson(
                "/input/dynamodb/dynamodb-utils-test-getTransactWriteItems-input-ZlDbItems-with-gsiSort.json",
                new TypeReference<>() {});

    final Stream<ZlDbPutItemPackage> stream =
        items.stream().map(item -> ZlDbPutItemPackage.builder().item(item).runFirst(true).build());
    final List<TransactWriteItem> transactWriteItems =
        getDynamoDbHelperUtils()
            .splitAndTransform(
                stream, zlDbPutItemPackage -> Boolean.TRUE.equals(zlDbPutItemPackage.getRunFirst()))
            .collect(Collectors.toList());

    Assertions.assertEquals(1, transactWriteItems.size());
    Assertions.assertNotNull(transactWriteItems.get(0).put());
    Assertions.assertNotNull(transactWriteItems.get(0).put().item().get("gsiSort"));
  }

  @Test
  void splitAndTransformTest_WithGsiNumericSort() {
    final List<ZlDbItem> items =
        getUnitTestUtils()
            .readJson(
                "/input/dynamodb/dynamodb-utils-test-getTransactWriteItems-input-ZlDbItems-with-gsiNumericSort.json",
                new TypeReference<>() {});

    final Stream<ZlDbPutItemPackage> stream =
        items.stream().map(item -> ZlDbPutItemPackage.builder().item(item).runFirst(true).build());
    final List<TransactWriteItem> transactWriteItems =
        getDynamoDbHelperUtils()
            .splitAndTransform(
                stream, zlDbPutItemPackage -> Boolean.TRUE.equals(zlDbPutItemPackage.getRunFirst()))
            .collect(Collectors.toList());

    Assertions.assertEquals(1, transactWriteItems.size());
    Assertions.assertNotNull(transactWriteItems.get(0).put());
    Assertions.assertNotNull(transactWriteItems.get(0).put().item().get("gsiNumericSort"));
  }

  @Test
  void toResultAndStartKeyTest() {
    final Tuple2<List<Map<String, AttributeValue>>, Optional<Map<String, AttributeValue>>> input =
        Tuple2.of(
            List.of(Map.of("x", AttributeValue.builder().s("y").build())),
            Optional.of(Map.of("x", AttributeValue.builder().s("y").build())));

    final Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>> objects =
        getDynamoDbHelperUtils().toResultAndStartKey(input);

    Assertions.assertNotNull(objects);
    Assertions.assertEquals(1, objects.getItem1().size());
    Assertions.assertNotNull(objects.getItem2().get());
  }

  @Test
  void toResultAndStartKeyTest_InputIsNull() {
    final Tuple2<List<ZlDbItem>, Optional<Map<String, AttributeValue>>> objects =
        getDynamoDbHelperUtils().toResultAndStartKey(null);

    Assertions.assertNull(objects);
  }

  @Test
  void findDataTest() {
    final List<ZlDbItem> items =
        getUnitTestUtils()
            .readJson(
                "/input/dynamodb/dynamodb-utils-test-getTransactWriteItems-input-ZlDbItems.json",
                new TypeReference<>() {});
    final String id = "58a12228-bb4c-406e-88f9-aa775e8062e5";
    final String sortKey = "certificate-data";
    final ZlDbItem item = getDynamoDbHelperUtils().findDataItem(items, id, sortKey).get();

    Assertions.assertEquals(id, item.getId());
    Assertions.assertEquals(sortKey, item.getSort());
  }

  @Test
  void splitToBatchesTest_WhenInputIsEmpty() {
    final List<List<TransactWriteItem>> lists = getDynamoDbHelperUtils().splitToBatches(List.of());
    Assertions.assertEquals(1, lists.size());
    Assertions.assertEquals(0, lists.get(0).size());
  }

  @Test
  void splitToBatchesTest_WhenInputSizeIsMoreThanTransactionLimit() {
    final List<TransactWriteItem> list =
        IntStream.range(0, MAX_ITEMS_IN_TRANSACTION + 1)
            .mapToObj(operand -> TransactWriteItem.builder().build())
            .collect(Collectors.toList());

    final List<List<TransactWriteItem>> lists = getDynamoDbHelperUtils().splitToBatches(list);
    Assertions.assertEquals(2, lists.size());
    Assertions.assertEquals(10, lists.get(0).size());
    Assertions.assertEquals(1, lists.get(1).size());
  }

  @Test
  void splitToBatchesTest_WhenInputSizeIsDoubleTheSizeOfTransactionLimit() {
    final List<TransactWriteItem> list =
        IntStream.range(0, MAX_ITEMS_IN_TRANSACTION * 2)
            .mapToObj(operand -> TransactWriteItem.builder().build())
            .collect(Collectors.toList());

    final List<List<TransactWriteItem>> lists = getDynamoDbHelperUtils().splitToBatches(list);
    Assertions.assertEquals(2, lists.size());
    Assertions.assertEquals(10, lists.get(0).size());
    Assertions.assertEquals(10, lists.get(1).size());
  }

  @Test
  void startsWithWildcardTest() {
    Assertions.assertTrue(getDynamoDbHelperUtils().startsWithWildcard(List.of("*--*--*--*")));
    Assertions.assertFalse(getDynamoDbHelperUtils().startsWithWildcard(List.of("*--1--*--*")));
  }

  @Test
  void createGsiItemTest() {
    final String id = "id";
    final String sort = "sort";
    final String gsiSort = "gsiSort";
    final ZlDbItem item = getDynamoDbHelperUtils().createGsiItem(id, sort, gsiSort);
    Assertions.assertEquals(id, item.getId());
    Assertions.assertEquals(sort, item.getSort());
    Assertions.assertEquals(gsiSort, item.getGsiSort());
  }

  @Test
  void createGsiItemTest_WithGsiNumericSort() {
    final String id = "id";
    final String sort = "sort";
    final Long gsiNumericSort = 1L;
    final ZlDbItem item = getDynamoDbHelperUtils().createGsiItem(id, sort, gsiNumericSort);
    Assertions.assertEquals(id, item.getId());
    Assertions.assertEquals(sort, item.getSort());
    Assertions.assertEquals(gsiNumericSort, item.getGsiNumericSort());
  }

  @Test
  void createSortKeyTest() {
    final List<String> arrayList = new ArrayList<>();
    arrayList.add("1");
    arrayList.add(null);
    arrayList.add("");
    arrayList.add("2");

    final String result =
        dynamoDbUtils.getDynamoDbHelperUtils().createSortKey("prefix", arrayList.stream());

    Assertions.assertEquals("prefix--1--*--*--2", result);
  }

  @Test
  void createSortKeyTest_whenThereIsNoKeys() {
    final String result = dynamoDbUtils.getDynamoDbHelperUtils().createSortKey("prefix");

    Assertions.assertEquals("prefix", result);
  }

  @Test
  void createTokenTest() {
    final Map<String, AttributeValue> attributeValueMap =
        Map.of("id", AttributeValue.builder().s("1").build());

    final String result = getDynamoDbHelperUtils().generateToken(attributeValueMap);

    Assertions.assertEquals("eyJpZCI6eyJzIjoiMSJ9fQ==", result);
  }

  @Test
  void createTokenTest_whenInputIsEmpty() {
    final Map<String, AttributeValue> attributeValueMap =
        Map.of("id", AttributeValue.builder().s("1").build());

    final String result = getDynamoDbHelperUtils().generateToken(Map.of());

    Assertions.assertNull(result);
  }

  @Test
  void convertToAttributeValueMapTest() {
    final Map<String, AttributeValue> attributeValueMap =
        Map.of("id", AttributeValue.builder().s("1").build());

    final Map<String, AttributeValue> result =
        getDynamoDbHelperUtils().generateAttributeValueMap("eyJpZCI6eyJzIjoiMSJ9fQ==");

    Assertions.assertEquals(attributeValueMap, result);
  }

  @Test
  void convertToAttributeValueMap_failure_Test() {
    final Map<String, AttributeValue> attributeValueMap =
        Map.of("id", AttributeValue.builder().s("1").build());

    Assertions.assertThrows(
        InvalidPaginationTokenException.class,
        () -> getDynamoDbHelperUtils().generateAttributeValueMap("a"));
  }

  private String printKeys(final List<String> keys) {
    return keys.stream().reduce((s, s2) -> s + StringUtils.LF + s2).orElse(StringUtils.EMPTY);
  }
}
