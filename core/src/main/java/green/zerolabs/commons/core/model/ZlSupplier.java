package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlSupplier implements ZlInternal {
  private static final long serialVersionUID = -4578808477011373836L;

  private String id;
  private String name;
  private String address;
  private String contact;
  private String website;
  private String social;
  private String blockchainAddress;
  private String inventoryId;

  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
