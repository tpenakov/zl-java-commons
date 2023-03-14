package green.zerolabs.commons.core.model.graphql.generated;

import java.util.List;
import org.eclipse.microprofile.graphql.NonNull;

// Autogenerated. Do not modify. Will be overridden. Generated by green.zerolabs.gqgen.Generator$TypeGenerator
@io.quarkus.runtime.annotations.RegisterForReflection
@lombok.Data
@lombok.NoArgsConstructor
public class ProofTransactionsRs implements java.io.Serializable  {
    @NonNull String id;
    @NonNull ProofStatus proofStatus;
    @NonNull String totalConsumptionAmount;
    @NonNull ConsumptionUnit totalConsumptionUnit;
    String totalConsumptionStartDate;
    String totalConsumptionEndDate;
    String requestDate;
    String generationDate;
    String accountName;
    String consumptionEntity;
    String userId;
    List<ProofTransactionCertificateDetails> certificates;
}
