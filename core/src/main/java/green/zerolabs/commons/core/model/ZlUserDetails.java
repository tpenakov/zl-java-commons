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
public class ZlUserDetails implements ZlInternal {
  private static final long serialVersionUID = 6532416022255144346L;

  public static final String FULL_TEXT_SEARCH_NAME_FIELD = "fullTextSearchInput";

  public enum ForgeAgreementLogic {
    SEARCH_BY_ID_ONLY,
    FULL_DETAILS_SEARCH
  }

  private String id;
  private String name;
  private String email;
  private String companyName;
  private String vat;
  private String address;
  private String state;
  private String zip;
  private String city;
  private String country;
  private String blockchainAddress;
  private String logo;
  private String metadataId;
  private String metadataType;
  private String fullTextSearchInput;
  private String inventoryId;

  @Builder.Default
  private ForgeAgreementLogic forgeAgreementLogic = ForgeAgreementLogic.SEARCH_BY_ID_ONLY;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
