package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.W3ProductType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlCertificateData implements ZlInternal {
  private static final long serialVersionUID = -7620250365186628828L;

  private String id;
  private Long energyAmount;
  private ConsumptionUnit energyUnit;
  private String generatorId;
  private String sellerId;
  private String supplierId;
  private String certificateRegistry;
  private W3ProductType productType;
  private Instant generationStart;
  private Instant generationEnd;
  private String redemptionDate;
  private String label;
  private String orderId;
  private String agreements;
  private List<String> agreementIdList;
  // Example:
  // 317d638a-7340-44eb-a171-b82f175b78dc:10:MWh,00bd3b96-3bbe-4133-8abd-af46a1c88942:3000:KWh
  private String agreementDistributionsCsv;
  private List<AgreementDistribution> agreementDistributions;
  private String redemptionStatementId;
  private String inventoryId;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class AgreementDistribution {
    private String agreementId;
    private BigDecimal energyAmount;
    private ConsumptionUnit energyUnit;
  }
}
