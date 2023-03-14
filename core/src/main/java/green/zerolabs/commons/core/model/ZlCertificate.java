package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRs;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import green.zerolabs.commons.core.model.graphql.generated.W3ProductType;
import green.zerolabs.commons.core.model.web3j.Web3jClaimSingleEventResponse;
import green.zerolabs.commons.core.model.web3j.Web3jTransferSingleEventResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlCertificate implements ZlInternal {
  private static final long serialVersionUID = 1580013655420912167L;

  public static final String STATUS_KEY = "#status";
  public static final String STATUS = "status";

  public enum Status {
    NONE,
    // on chain -> setRedemptionStatement
    CREATED,
    // on chain -> mint
    AGREEMENT_ACTIVE,
    // on chain -> mint
    ACTIVE,
    // certificate is split and used for parent
    PARENT,
    // returned from the forge
    SOLD,
    // safeTransferFrom from smart contracts
    TRANSFERRED,
    // safeTransferAndClaimFrom (for beneficiary) or agreementFactory.claimAgeement (for agreements)
    // from smart contracts
    CLAIMED,
    CANCELLED,
    ERROR,
  }

  private String id;
  private Status status;
  private List<String> parents;

  private ZlOrder order;
  private ZlCertificateData certificateData;
  private ZlAgreement agreement;
  private ZlGenerator generator;
  private IndexedCertificate indexedData;
  private List<DataWithRootCertificate> dataWithRootCertificates;
  private String w3Id;
  private Instant w3MintDateTime;
  private ZlBatch w3Batch;
  private Map<String, Web3jTransferSingleEventResponse> transferSingleEventResponseMap;
  private Map<String, Web3jClaimSingleEventResponse> claimSingleEventResponseMap;
  private String inventoryId;
  private String chainId;
  private Boolean isAgreementDistributionLeftOver;

  // internal fields
  @Builder.Default
  private ZlInternalData internal =
      ZlInternalData.builder().status(ZlInternalData.Status.OK).build();

  @Builder
  @Data
  @Jacksonized
  public static class IndexedCertificate implements Serializable {
    private static final long serialVersionUID = -1301317675853550124L;

    private String agreementId;
    private Long energyAmount;
    private ConsumptionUnit energyUnit;
    private Long startDate;
    private Long endDate;
    private List<W3ProductType> productTypes;
    private List<EnergyType> energySources;
    private List<String> countryRegionIndexes;
    private List<ConsumptionLocationRs> countryRegionList;
  }

  @Builder
  @Data
  @Jacksonized
  public static class DataWithRootCertificate implements Serializable {
    private static final long serialVersionUID = -4796826333346339914L;

    private IndexedCertificate data;
    private ZlCertificate root;

  }
}
