package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.*;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Optional;

/***
 * Created by Triphon Penakov 2022-10-06
 */
@Slf4j
public class ProofRsWrapper {

  public static Optional<ProofDetailsRs> getProofDetailsRs(final ProofRs proofRs) {
    return Optional.ofNullable(proofRs).map(ProofRs::getDetails);
  }

  public static Optional<ProofUserDetails> getUserDetails(final ProofRs proofRs) {
    return getProofDetailsRs(proofRs).map(ProofDetailsRs::getUserDetails);
  }

  public static Optional<String> getUserId(final ProofRs proofRs) {
    return getUserDetails(proofRs).map(ProofUserDetails::getId).filter(StringUtils::isNotBlank);
  }

  public static Optional<BeneficiaryRs> getBeneficiaryRs(final ProofRs proofRs) {
    return Optional.ofNullable(proofRs).map(ProofRs::getBeneficiaryData);
  }

  public static Optional<String> getBeneficiaryId(final ProofRs proofRs) {
    return getBeneficiaryRs(proofRs).map(BeneficiaryRs::getId).filter(StringUtils::isNotBlank);
  }

  public static Optional<ConsumptionEntityRs> getConsumptionEntityRs(final ProofRs proofRs) {
    return Optional.ofNullable(proofRs).map(ProofRs::getConsumptionEntity);
  }

  public static Optional<String> getConsumptionEntityId(final ProofRs proofRs) {
    return getConsumptionEntityRs(proofRs)
        .map(ConsumptionEntityRs::getId)
        .filter(StringUtils::isNotBlank);
  }

  public static Optional<CertificateRs> getCertificateRs(final ProofRs proofRs) {
    return Optional.ofNullable(proofRs).map(ProofRs::getCertificate);
  }

  public static Optional<IndexedCertificateRs> getIndexedCertificateRs(final ProofRs proofRs) {
    return getCertificateRs(proofRs).map(CertificateRs::getIndexedData);
  }

  public static Optional<Long> getPeriodStartEpochMillis(
      final ProofRs proofRs, final ConverterUtils converterUtils) {
    return getIndexedCertificateRs(proofRs)
        .map(IndexedCertificateRs::getStartDate)
        .map(converterUtils::toEpochMilli);
  }

  public static Optional<Long> getPeriodEndEpochMillis(
      final ProofRs proofRs, final ConverterUtils converterUtils) {
    return getIndexedCertificateRs(proofRs)
        .map(IndexedCertificateRs::getEndDate)
        .map(converterUtils::toEpochMilli);
  }
}
