package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.*;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.graphql.NonNull;

import java.util.List;

@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlProof implements ZlInternal {
  private static final long serialVersionUID = 7377449372329777588L;

  @NonNull String id;
  @NonNull ProofStatus proofStatus;
  String w2Url;
  String w2StorageUrl;
  String w2StorageName;
  String w2StoragePath;
  String w3StorageUrl;
  String transactionId;
  String requestDate;
  String generationDate;
  String error;
  ProofDetailsRs details;
  CertificateRs certificate;
  List<IndexedCertificateRs> indexedCertificates;
  List<CertificateRs> rootCertificates;
  BeneficiaryRs beneficiaryData;
  List<SellerRs> sellers;
  List<SupplierRs> suppliers;
  List<ZlGenerator> generators;
  AgreementRs agreement;
  ConsumptionEntityRs consumptionEntity;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();

}
