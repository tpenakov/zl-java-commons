package green.zerolabs.commons.core.converter;

import io.smallrye.mutiny.Uni;

import java.io.Serializable;

/***
 * Created by Triphon Penakov 2023-02-22
 */
public interface DbToDtoConverter<DB, DTO extends Serializable> {

  Uni<DB> convertToDb(DTO item, DB previous);

  Uni<DTO> convertToDto(DB item);

  Class<DTO> getDtoClass();

  Class<DB> getDbClass();
}
