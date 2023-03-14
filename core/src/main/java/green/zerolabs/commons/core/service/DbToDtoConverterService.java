package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.converter.DbToDtoConverter;
import io.smallrye.mutiny.Uni;

import java.io.Serializable;

/***
 * Created by Triphon Penakov 2023-02-22
 */
public interface DbToDtoConverterService {
  void registerConverter(DbToDtoConverter<?, ? extends Serializable> converter);

  <DB, DTO extends Serializable> Uni<DB> convertToDb(DTO item, Class<DB> dbClass, DB previous);

  <DB, DTO extends Serializable> Uni<DTO> convertToDto(DB item, Class<DTO> dtoClass);
}
