package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlInternal;
import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Uni;

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

/***
 * Created by Triphon Penakov 2023-02-22
 */
public class GenericInternalDbToDtoConverter<DB extends ZlInternal, DTO extends Serializable>
    extends GenericDbToDtoConverter<DB, DTO> {
  public GenericInternalDbToDtoConverter(
      final DeepClone cloneable, final Class<DB> dbClass, final Class<DTO> dtoClass) {
    super(cloneable, dbClass, dtoClass);
  }

  @Override
  public Uni<DB> convertToDb(final DTO item, final DB previous) {
    return super.convertToDb(item, previous)
        .onItem()
        .ifNotNull()
        .invoke(
            converted ->
                Optional.ofNullable(previous)
                    .ifPresent(prev -> converted.setInternal(prev.getInternal())))
        .onItem()
        .ifNotNull()
        .invoke(
            converted ->
                Optional.ofNullable(converted.getInternal())
                    .ifPresent(zlInternalData -> zlInternalData.setUpdatedDate(Instant.now())));
  }
}
