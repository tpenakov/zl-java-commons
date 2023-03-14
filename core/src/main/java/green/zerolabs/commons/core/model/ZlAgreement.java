package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRs;
import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import green.zerolabs.commons.core.model.graphql.generated.W3ProductType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/*
 * Created by Triphon Penakov 2022-08-31
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlAgreement implements ZlInternal {
  private static final long serialVersionUID = 9046178025318216083L;

  public static final String W3_ADDRESS = "w3Address";
  public static final String W3_BUYER_ADDRESS = "w3BuyerAddress";
  public static final String W3_SELLER_ADDRESS = "w3SellerAddress";

  public enum Status {
    INITIAL,
    ORDER_ADDED,
    DEPLOY_ON_WEB3_REQUESTED,
    DEPLOYED_ON_WEB3,
    SIGN_ON_WEB3_REQUESTED,
    SIGNED_ON_WEB3,
    COMPLETED,
    ERROR,
  }

  private String id;
  @Builder.Default private Status agreementStatus = Status.INITIAL;
  private W3ProductType productType;
  private String energySources;
  List<EnergyType> energyTypes;
  private String countryRegionList;
  private List<ConsumptionLocationRs> consumptionLocations;
  private Instant orderDate;
  private Instant deliveryDate;
  private Instant reportingStart;
  private Instant reportingEnd;
  private BigDecimal energyAmount;
  private String energyUnit;
  private String userId;
  private String beneficiaryId;
  private String sellerId;
  private String supplierId;
  private String consumptionEntityId;
  private String orderId;
  private String label;
  private String proofRequestId;
  private String w3Address;
  private String w3DeployAgreementTxHash;
  private String w3SignAgreementTxHash;
  private List<String> w3FillAgreementTxHashList;
  private List<String> w3ClaimAgreementTxHashList;
  private String w3BuyerAddress;
  private String w3SellerAddress;
  private String inventoryId;
  private String metadata;
  private String externalId;
  private List<String> errors;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
