package green.zerolabs.commons.core.service.impl;

import green.zerolabs.commons.core.converter.DbToDtoConverter;
import green.zerolabs.commons.core.converter.db2dto.GenericDbToDtoConverter;
import green.zerolabs.commons.core.service.DbToDtoConverterService;
import green.zerolabs.commons.core.utils.JsonUtils;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/***
 * Created by Triphon Penakov 2023-02-22
 */
@SuppressWarnings("unchecked")
@Slf4j
public class DbToDtoConverterServiceImpl implements DbToDtoConverterService {

  private final Map<String, DbToDtoConverter<?, ? extends Serializable>> converterMap =
      new HashMap<>();

  private final JsonUtils jsonUtils;

  public DbToDtoConverterServiceImpl(final JsonUtils jsonUtils) {
    this.jsonUtils = jsonUtils;
  }

  @Override
  public void registerConverter(final DbToDtoConverter<?, ? extends Serializable> converter) {
    converterMap.put(
        generateConverterName(converter.getDbClass(), converter.getDtoClass()), converter);
  }

  @Override
  public <DB, DTO extends Serializable> Uni<DB> convertToDb(
      final DTO item, final Class<DB> dbClass, final DB previous) {
    if (Objects.isNull(item)) {
      return Uni.createFrom().nullItem();
    }
    final DbToDtoConverter<DB, DTO> converter =
        (DbToDtoConverter<DB, DTO>) getConverter(dbClass, item.getClass());

    return converter.convertToDb(item, previous);
  }

  @Override
  public <DB, DTO extends Serializable> Uni<DTO> convertToDto(
      final DB item, final Class<DTO> dtoClass) {
    if (Objects.isNull(item)) {
      return Uni.createFrom().nullItem();
    }

    final DbToDtoConverter<DB, DTO> converter =
        (DbToDtoConverter<DB, DTO>) getConverter(item.getClass(), dtoClass);

    return converter.convertToDto(item);
  }

  <DB, DTO extends Serializable> DbToDtoConverter<DB, DTO> getConverter(
      final Class<DB> dbClass, final Class<DTO> dtoClass) {

    final String key = generateConverterName(dbClass, dtoClass);

    return (DbToDtoConverter<DB, DTO>)
        converterMap.getOrDefault(key, new GenericDbToDtoConverter<>(jsonUtils, dbClass, dtoClass));
  }

  private String generateConverterName(final Class<?> dbClass, final Class<?> dtoClass) {
    return String.format("%s-%s", dbClass.getCanonicalName(), dtoClass.getCanonicalName());
  }
}
