package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ProofStatus;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/***
 * Created by Triphon Penakov 2022-10-06
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlProofIndexed {
  public static final String START_DATE = "startDate";
  public static final String END_DATE = "endDate";

  private ProofStatus proofStatus;
  private String userId;
  private String beneficiaryId;
  private String consumptionEntityId;
  private Long startDate;
  private Long endDate;
}
