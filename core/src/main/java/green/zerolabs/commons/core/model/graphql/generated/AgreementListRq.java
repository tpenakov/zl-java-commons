package green.zerolabs.commons.core.model.graphql.generated;

// Autogenerated. Do not modify. Will be overridden. Generated by green.zerolabs.gqgen.Generator$TypeGenerator
@io.quarkus.runtime.annotations.RegisterForReflection
@lombok.Data
@lombok.NoArgsConstructor
public class AgreementListRq implements java.io.Serializable  {
    String beneficiaryId;
    String userId;
    String consumptionEntityId;
    String externalId;
    ConsumptionPeriodRq period;
    PaginationRq pagination;
}
