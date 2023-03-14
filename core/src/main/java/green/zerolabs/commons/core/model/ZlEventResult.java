package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlEventResult implements ZlInternal {
  private static final long serialVersionUID = 5672574058925482900L;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
