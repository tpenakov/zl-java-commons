package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.converter.DbToDtoConverter;
import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/***
 * Created by Triphon Penakov 2023-02-22
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class BaseDbToDtoConverter<DB, DTO extends Serializable>
    implements DbToDtoConverter<DB, DTO> {
  private final DeepClone cloneable;

  protected Uni<DB> convertToDbSimple(final DTO item, final DB previous) {
    return Uni.createFrom().item(getCloneable().deepClone(item, getDbClass()));
  }

  protected Uni<DTO> convertToDtoSimple(final DB item) {
    return Uni.createFrom().item(getCloneable().deepClone(item, getDtoClass()));
  }
}
