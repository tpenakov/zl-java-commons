package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlBeneficiary implements ZlInternal {
  private static final long serialVersionUID = -3225440816610384515L;

  public static String FULL_TEXT_SEARCH_NAME_FIELD = "fullTextSearchInput";

  private String id;
  private String userId;
  private String externalId;
  private String name;
  private String email;
  private String address;
  private String description;
  private String blockchainAddressIndex;
  private String blockchainAddress;
  private String blockchainPk;
  private String goals;
  private String website;
  private String social;
  private String images;
  private List<String> imagesTransformed;
  private String fullTextSearchInput;
  private Boolean sendFundsIsDisabled;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
