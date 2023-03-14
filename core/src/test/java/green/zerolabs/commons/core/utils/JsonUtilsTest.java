package green.zerolabs.commons.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.model.ZlCertificateData;
import green.zerolabs.commons.core.model.ZlSqsItem;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.utils.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;

@Getter
@Setter
@Slf4j
class JsonUtilsTest {

  private JsonUtils jsonUtils;

  @BeforeEach
  void setUp() {
    jsonUtils = Mockito.spy(new JsonUtils(new ObjectMapper()));
  }

  @Test
  void toMapTest() {
    final Map<String, Object> result = jsonUtils.toMap(generateDummy());

    Assertions.assertEquals("parent", result.get("name"));
    Assertions.assertEquals(1, result.get("order"));

    final List<Map<String, Object>> childList = (List<Map<String, Object>>) result.get("childList");
    Assertions.assertEquals("child", childList.get(0).get("name"));
    Assertions.assertEquals(2, childList.get(0).get("order"));

    final List<Map<String, Object>> childChildList =
        (List<Map<String, Object>>) childList.get(0).get("childList");
    Assertions.assertEquals("child-child", childChildList.get(0).get("name"));
    Assertions.assertNull(childChildList.get(0).get("order"));
  }

  @Test
  void toMapNullValueTest() throws IOException {
    final Map<String, Object> map = new HashMap<>();
    map.put("key", null);
    Mockito.doReturn(map).when(getJsonUtils()).toMapInternal(any(), any());

    final Map<String, Object> result =
        jsonUtils.toMap(generateDummy(), jsonUtils.getObjectMapper(), Map.of());

    Assertions.assertTrue(CollectionUtils.isNullOrEmpty(result));
  }

  @Test
  void toMapStringInputTest() {
    final Map<String, Object> result = jsonUtils.toMap(getDummyAsString());

    Assertions.assertEquals("parent", result.get("name"));
    Assertions.assertEquals(1, result.get("order"));

    final List<Map<String, Object>> childList = (List<Map<String, Object>>) result.get("childList");
    Assertions.assertEquals("child", childList.get(0).get("name"));
    Assertions.assertEquals(2, childList.get(0).get("order"));

    final List<Map<String, Object>> childChildList =
        (List<Map<String, Object>>) childList.get(0).get("childList");
    Assertions.assertEquals("child-child", childChildList.get(0).get("name"));
    Assertions.assertNull(childChildList.get(0).get("order"));
  }

  @Test
  void toMapNullInputTest() {
    Assertions.assertThrows(RuntimeException.class, () -> jsonUtils.toMap(null));
  }

  @Test
  void toMapInvalidInputTest() {
    Assertions.assertThrows(RuntimeException.class, () -> jsonUtils.toMap("invalid"));
  }

  @Test
  void toMapValueChangeMapTest() {
    final Function<Object, Object> addSuffix = Unchecked.function(param -> param + "-new");

    final Map<String, Object> result = jsonUtils.toMap(generateDummy(), Map.of("name", addSuffix));

    Assertions.assertEquals("parent-new", result.get("name"));
    Assertions.assertEquals(1, result.get("order"));

    final List<Map<String, Object>> childList = (List<Map<String, Object>>) result.get("childList");
    Assertions.assertEquals("child", childList.get(0).get("name"));
    Assertions.assertEquals(2, childList.get(0).get("order"));

    final List<Map<String, Object>> childChildList =
        (List<Map<String, Object>>) childList.get(0).get("childList");
    Assertions.assertEquals("child-child", childChildList.get(0).get("name"));
    Assertions.assertNull(childChildList.get(0).get("order"));
  }

  @Test
  void toMap2InvalidInputTest() {
    Assertions.assertThrows(
        RuntimeException.class, () -> jsonUtils.toMap("invalid", new ObjectMapper()));
  }

  @Test
  void toMapDefinedKeyValueTypesTest() {
    final Map<String, Object> result =
        jsonUtils.toMap(getDummyAsString(), String.class, Object.class);

    Assertions.assertEquals("parent", result.get("name"));
    Assertions.assertEquals(1, result.get("order"));

    final List<Map<String, Object>> childList = (List<Map<String, Object>>) result.get("childList");
    Assertions.assertEquals("child", childList.get(0).get("name"));
    Assertions.assertEquals(2, childList.get(0).get("order"));

    final List<Map<String, Object>> childChildList =
        (List<Map<String, Object>>) childList.get(0).get("childList");
    Assertions.assertEquals("child-child", childChildList.get(0).get("name"));
    Assertions.assertNull(childChildList.get(0).get("order"));
  }

  @Test
  void toMapDefinedKeyValueTypesInvalidInputTest() {
    Assertions.assertThrows(
        RuntimeException.class,
        () -> jsonUtils.toMap(getDummyAsString(), String.class, Dummy.class));
  }

  @Test
  void deepCloneTest() {
    final Dummy input = generateDummy();

    final Dummy result = jsonUtils.deepClone(input, Dummy.class);

    Assertions.assertEquals("parent", result.getName());
    Assertions.assertEquals(1L, result.getOrder());

    final List<Dummy> childList = result.getChildList();
    Assertions.assertEquals("child", childList.get(0).getName());
    Assertions.assertEquals(2L, childList.get(0).getOrder());

    final List<Dummy> childChildList = childList.get(0).getChildList();
    Assertions.assertEquals("child-child", childChildList.get(0).getName());
    Assertions.assertNull(childChildList.get(0).getOrder());
  }

  @Test
  void deepCloneNullInputTest() {
    final Dummy result = jsonUtils.deepClone(null, Dummy.class);

    Assertions.assertNull(result);
  }

  @Test
  void deepCloneInvalidInputTest() {
    Assertions.assertThrows(
        RuntimeException.class, () -> jsonUtils.deepClone("invalid", Dummy.class));
  }

  @Test
  void readValueTest() {
    final Optional<Dummy> result = jsonUtils.readValue(getDummyAsString(), Dummy.class);

    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(generateDummy(), result.get());
  }

  @Test
  void readValueByteTest() {
    final Optional<Dummy> result = jsonUtils.readValue(getDummyAsString().getBytes(), Dummy.class);

    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(generateDummy(), result.get());
  }

  @Test
  void readValueInvalidByteTest() {
    final Optional<Dummy> result = jsonUtils.readValue("invalid".getBytes(), Dummy.class);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void readValueInputStreamTest() {
    final Optional<Dummy> result =
        jsonUtils.readValue(new ByteArrayInputStream(getDummyAsString().getBytes()), Dummy.class);

    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(generateDummy(), result.get());
  }

  @Test
  void readValueInvalidInputStreamTest() {
    final Optional<Dummy> result =
        jsonUtils.readValue(new ByteArrayInputStream("invalid".getBytes()), Dummy.class);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void readValueInvalidInputTest() {
    final Optional<Dummy> result = jsonUtils.readValue("invalid", Dummy.class);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void readValueNullInputTest() {
    final String input = null;
    final Optional<Dummy> result = jsonUtils.readValue(input, Dummy.class);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void readValueEmptyInputTest() {
    final Optional<Dummy> result = jsonUtils.readValue("", Dummy.class);

    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void toStringTest() {
    final String result = jsonUtils.toString(jsonUtils.toMap(generateDummy()));
    Assertions.assertNotNull(result);
  }

  @Test
  void toStringLazyTest() {
    final Object result = jsonUtils.toStringLazy(generateDummy());

    Assertions.assertEquals(getDummyAsString(), result.toString());
  }

  @Test
  void toStringLazyNullInputTest() {
    final Object result = jsonUtils.toStringLazy(null);

    Assertions.assertEquals("", result.toString());
  }

  @Test
  void toStringLazyNullMapperTest() {
    final Object result = jsonUtils.toStringLazy(generateDummy(), null);

    Assertions.assertEquals("", result.toString());
  }

  @Test
  void toStringLazyFailure() {
    final ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
    try {
      Mockito.doThrow(RuntimeException.class).when(objectMapper).writeValueAsString(any());
    } catch (final JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    final Object result = jsonUtils.toStringLazy(generateDummy(), objectMapper);

    Assertions.assertEquals("", result.toString());
  }

  @Test
  void convertValueTest() {
    final Dummy result =
        jsonUtils.convertValue(jsonUtils.toMap(generateDummy()), new TypeReference<>() {});

    Assertions.assertEquals(generateDummy(), result);
  }

  @Test
  void fromMapTest() {
    final Dummy result = jsonUtils.fromMap(jsonUtils.toMap(generateDummy()), Dummy.class);
    Assertions.assertEquals(result, generateDummy());
  }

  @Test
  void fromMapInvalidInputTest() {
    final Map<String, Object> invalidInput = Map.of("type", "invalid");
    Assertions.assertThrows(
        RuntimeException.class, () -> jsonUtils.fromMap(invalidInput, Dummy.class));
  }

  @Test
  void assertGetStringFunctions() {
    Assertions.assertNotNull(jsonUtils.getMAP_TO_STRING_FN());
    Assertions.assertNotNull(jsonUtils.getSTRING_TO_MAP_FN());
  }

  @Test
  void instantTest() {
    final ZlCertificateData zlCertificateData =
        UnitTestUtils.of()
            .readFromFile("json/sample-redemption-date-only.json", ZlCertificateData.class);
    Assertions.assertNotNull(zlCertificateData);
    Assertions.assertNotNull(zlCertificateData.getGenerationStart());
    Assertions.assertNotNull(zlCertificateData.getGenerationEnd());

    final String json = getJsonUtils().toStringLazy(zlCertificateData).toString();
    // log.info("zlRedemption: {}", json);
    Assertions.assertTrue(json.contains("T00:00:00Z"));
  }

  @Test
  void parseFromStringTest() {
    final String value = "{\"web3\":{}}";

    final Optional<ZlSqsItem> result = getJsonUtils().readValue(value, ZlSqsItem.class);
    Assertions.assertTrue(result.isPresent());
    final String jsonString = getJsonUtils().toStringLazy(result.orElseThrow()).toString();
    log.info("result: {}", jsonString);
  }

  private String getDummyAsString() {
    return "{\"name\":\"parent\",\"order\":1,\"childList\":[{\"name\":\"child\",\"order\":2,"
        + "\"childList\":[{\"name\":\"child-child\"}]}]}";
  }

  private Dummy generateDummy() {
    final Dummy dummy = new Dummy();
    dummy.setName("parent");
    dummy.setOrder(1L);

    final Dummy child1 = new Dummy();
    child1.setName("child");
    child1.setOrder(2L);

    final Dummy child1_1 = new Dummy();
    child1_1.setName("child-child");
    child1_1.setOrder(null);

    child1.setChildList(List.of(child1_1));
    dummy.setChildList(List.of(child1));

    return dummy;
  }

  @Getter
  @Setter
  @EqualsAndHashCode
  private static class Dummy {

    private String name;
    private Long order;
    private List<Dummy> childList;
    private DummyEnum type;
  }

  enum DummyEnum {
    DUMMY,
    SMART
  }
}
