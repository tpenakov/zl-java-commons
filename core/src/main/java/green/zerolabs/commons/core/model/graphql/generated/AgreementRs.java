package green.zerolabs.commons.core.model.graphql.generated;

import java.util.List;
import org.eclipse.microprofile.graphql.NonNull;

// Autogenerated. Do not modify. Will be overridden. Generated by green.zerolabs.gqgen.Generator$TypeGenerator
@io.quarkus.runtime.annotations.RegisterForReflection
@lombok.Data
@lombok.NoArgsConstructor
public class AgreementRs implements java.io.Serializable,IdOnly,ErrorResponse  {
    @NonNull String id;
    AgreementStatus agreementStatus;
    W3ProductType productType;
    List<EnergyType> energyTypes;
    List<ConsumptionLocationRs> consumptionLocations;
    String orderDate;
    String deliveryDate;
    String reportingStart;
    String reportingEnd;
    String energyAmount;
    String energyAmountDeliveredOnChain;
    ConsumptionUnit energyUnit;
    UserDetailsRs userDetails;
    BeneficiaryRs beneficiary;
    SellerRs seller;
    SupplierRs supplier;
    ConsumptionEntityRs consumptionEntity;
    OrderRs order;
    String label;
    String proofRequestId;
    String w3Address;
    String w3BuyerAddress;
    String w3SellerAddress;
    String w3DeployAgreementTxHash;
    String w3SignAgreementTxHash;
    List<String> w3FillAgreementTxHashList;
    List<String> w3ClaimAgreementTxHashList;
    String externalId;
    Boolean isError;
    List<String> errors;
}
