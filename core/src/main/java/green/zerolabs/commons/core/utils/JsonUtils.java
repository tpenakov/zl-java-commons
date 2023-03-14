package green.zerolabs.commons.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import green.zerolabs.commons.core.converter.InstantDeserializer;
import green.zerolabs.commons.core.converter.InstantSerializer;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Some of the code is copied from here:
 *
 * <p>https://github.com/tpenakov/otaibe-commons-quarkus/blob/master/otaibe-commons-quarkus-core/src/main/java/org/otaibe/commons/quarkus/core/utils/JsonUtils.java
 *
 * <p>Created by triphon 27.02.22 г.
 */
@Getter
@Slf4j
@SuppressWarnings("unchecked")
public class JsonUtils implements DeepClone {

  public static final String EMPTY = "";
  private final Function<Map<String, Object>, String> MAP_TO_STRING_FN =
      Unchecked.<Map<String, Object>, String>function(
          input -> getObjectMapper().writeValueAsString(input));

  private final Function<String, Map<String, Object>> STRING_TO_MAP_FN =
      Unchecked.<String, Map<String, Object>>function(
          input -> getObjectMapper().readValue(input, Map.class));

  private final ObjectMapper objectMapper;

  public JsonUtils(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    objectMapper.registerModule(new JavaTimeModule());

    registerCustomConverters(objectMapper);
  }

  public static void registerCustomConverters(final ObjectMapper objectMapper) {
    final SimpleModule module = new SimpleModule();
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    objectMapper.registerModule(module);
  }

  public String toString(final Map<String, Object> input) {
    return MAP_TO_STRING_FN.apply(input);
  }

  public Map<String, Object> toMap(final String input) {
    return STRING_TO_MAP_FN.apply(input);
  }

  public <K, V> Map<K, V> toMap(
      final String input, final Class<K> keyType, final Class<V> valueType) {
    final TypeFactory typeFactory = getObjectMapper().getTypeFactory();
    final MapType mapType = typeFactory.constructMapType(Map.class, keyType, valueType);

    try {
      return getObjectMapper().readValue(input, mapType);
    } catch (final Exception e) {
      log.error("unable to transform to outputClass", e);
      throw new RuntimeException(e);
    }
  }

  public <T> T fromMap(final Map input, final Class<T> outputClass) {
    return fromMap(input, getObjectMapper(), outputClass);
  }

  public <T> T fromMap(
      final Map input, final ObjectMapper objectMapper, final Class<T> outputClass) {
    try {
      final T value = objectMapper.readValue(objectMapper.writeValueAsBytes(input), outputClass);
      return value;
    } catch (final Exception e) {
      log.error("unable to transform to outputClass", e);
      throw new RuntimeException(e);
    }
  }

  public Map<String, Object> toMap(final Object input) {
    return toMap(input, getObjectMapper());
  }

  public Map<String, Object> toMap(final Object input, final ObjectMapper objectMapper) {
    return toMap(input, objectMapper, new HashMap<>());
  }

  public Map<String, Object> toMap(
      final Object input, final Map<String, Function<Object, Object>> valueChangeMap) {
    return toMap(input, getObjectMapper(), valueChangeMap);
  }

  public Map<String, Object> toMap(
      final Object input,
      final ObjectMapper objectMapper,
      final Map<String, Function<Object, Object>> valueChangeMap) {
    try {
      final Map<String, Object> map = toMapInternal(input, objectMapper);
      return map.keySet().stream()
          .filter(Objects::nonNull)
          .filter(s -> map.get(s) != null)
          .filter(s -> StringUtils.isNotBlank(map.get(s).toString()))
          .collect(
              Collectors.toMap(
                  o -> o,
                  o -> {
                    final Object val = map.get(o);
                    if (valueChangeMap.containsKey(o)) {
                      return valueChangeMap.get(o).apply(val);
                    }
                    return val;
                  }));
    } catch (final Exception e) {
      log.error("unable to transform to Map", e);
      throw new RuntimeException(e);
    }
  }

  Map toMapInternal(final Object input, final ObjectMapper objectMapper) throws IOException {
    return objectMapper.readValue(objectMapper.writeValueAsBytes(input), Map.class);
  }

  @Override
  public <T> T deepClone(final Object input, final Class<T> resultType) {
    return deepClone(input, getObjectMapper(), resultType);
  }

  public <T> T deepClone(
      final Object input, final ObjectMapper objectMapper, final Class<T> resultType) {
    if (input == null) {
      return null;
    }
    try {
      return objectMapper.readValue(objectMapper.writeValueAsBytes(input), resultType);
    } catch (final Exception e) {
      log.error("unable to serialize", e);
      throw new RuntimeException(e);
    }
  }

  public Object toStringLazy(final Object input) {
    return toStringLazy(input, getObjectMapper());
  }

  public Object toStringLazy(final Object input, final ObjectMapper objectMapper) {
    return new ToStringLazy(input, objectMapper);
  }

  public <T> Optional<T> readValue(final String value, final Class<T> clazz) {
    return readValue(value, clazz, getObjectMapper());
  }

  public <T> Optional<T> readValue(final byte[] value, final Class<T> clazz) {
    return readValue(value, clazz, getObjectMapper());
  }

  public <T> Optional<T> readValue(final InputStream value, final Class<T> clazz) {
    return readValue(value, clazz, getObjectMapper());
  }

  public <T> Optional<T> readValue(
      final String value, final Class<T> clazz, final ObjectMapper objectMapper1) {
    return Optional.ofNullable(objectMapper1)
        .map(
            objectMapper -> {
              try {
                return objectMapper.readValue(value, clazz);
              } catch (final Exception e) {
                log.error("unable to deserialize", e);
              }
              return null;
            });
  }

  public <T> Optional<T> readValue(
      final byte[] value, final Class<T> clazz, final ObjectMapper objectMapper1) {
    return Optional.ofNullable(objectMapper1)
        .map(
            objectMapper -> {
              try {
                return objectMapper.readValue(value, clazz);
              } catch (final Exception e) {
                log.error("unable to deserialize", e);
              }
              return null;
            });
  }

  public <T> Optional<T> readValue(
      final InputStream value, final Class<T> clazz, final ObjectMapper objectMapper1) {
    return Optional.ofNullable(objectMapper1)
        .map(
            objectMapper -> {
              try {
                return objectMapper.readValue(value, clazz);
              } catch (final Exception e) {
                log.error("unable to deserialize", e);
              }
              return null;
            });
  }

  public <T> T convertValue(final Object data, final TypeReference<T> dataClass) {
    return convertValue(data, dataClass, getObjectMapper());
  }

  public <T> T convertValue(
      final Object data, final TypeReference<T> dataClass, final ObjectMapper objectMapper1) {
    return Unchecked.unchecked(() -> objectMapper1.convertValue(data, dataClass))
        .toSupplier()
        .get();
  }

  @AllArgsConstructor
  private static class ToStringLazy {
    private Object input;
    private ObjectMapper objectMapper;

    @Override
    public String toString() {
      if (objectMapper == null || input == null) {
        return EMPTY;
      }
      try {
        final String value = objectMapper.writeValueAsString(input);
        return value;
      } catch (final Exception e) {
        log.error("unable to serialize to json", e);
        return EMPTY;
      }
    }
  }
}
