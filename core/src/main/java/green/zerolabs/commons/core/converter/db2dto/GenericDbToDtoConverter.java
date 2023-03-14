package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.Serializable;

/***
 * Created by Triphon Penakov 2023-02-22
 */
@Getter(AccessLevel.PROTECTED)
public class GenericDbToDtoConverter<DB, DTO extends Serializable>
    extends BaseDbToDtoConverter<DB, DTO> {

  @Getter protected final Class<DB> dbClass;
  @Getter protected final Class<DTO> dtoClass;

  public GenericDbToDtoConverter(
      final DeepClone cloneable, final Class<DB> dbClass, final Class<DTO> dtoClass) {
    super(cloneable);
    this.dbClass = dbClass;
    this.dtoClass = dtoClass;
  }

  @Override
  public Uni<DB> convertToDb(final DTO item, final DB previous) {
    return convertToDbSimple(item, previous);
  }

  @Override
  public Uni<DTO> convertToDto(final DB item) {
    return convertToDtoSimple(item);
  }
}
