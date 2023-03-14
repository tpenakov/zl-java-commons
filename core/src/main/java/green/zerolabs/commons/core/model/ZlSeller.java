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
public class ZlSeller implements ZlInternal {
  private static final long serialVersionUID = -8195824852338651845L;

  private String id;
  private String name;
  private String address;
  private String contact;
  private String website;
  private String social;
  private String blockchainAddress;
  private String inventoryId;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
