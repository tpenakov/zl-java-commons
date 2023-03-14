package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ProofRsWrapperTest {

  @Test
  void getProofDetailsRsTest() {
    final Optional<ProofDetailsRs> result = ProofRsWrapper.getProofDetailsRs(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getUserDetailsTest() {
    final Optional<ProofUserDetails> result = ProofRsWrapper.getUserDetails(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getUserIdTest() {
    final Optional<String> result = ProofRsWrapper.getUserId(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals("user-id", result.get());
  }

  @Test
  void getBeneficiaryRsTest() {
    final Optional<BeneficiaryRs> result = ProofRsWrapper.getBeneficiaryRs(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getBeneficiaryIdTest() {
    final Optional<String> result = ProofRsWrapper.getBeneficiaryId(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals("beneficiary-id", result.get());
  }

  @Test
  void getConsumptionRsTest() {
    final Optional<ConsumptionEntityRs> result =
        ProofRsWrapper.getConsumptionEntityRs(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getConsumptionIdTest() {
    final Optional<String> result = ProofRsWrapper.getConsumptionEntityId(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals("consumption-id", result.get());
  }

  @Test
  void getCertificateTest() {
    final Optional<CertificateRs> result = ProofRsWrapper.getCertificateRs(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getIndexedCertificateTest() {
    final Optional<IndexedCertificateRs> result =
        ProofRsWrapper.getIndexedCertificateRs(generateProofRs());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getPeriodStartTest() {
    final Optional<Long> result =
        ProofRsWrapper.getPeriodStartEpochMillis(generateProofRs(), new ConverterUtils());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void getPeriodEndTest() {
    final Optional<Long> result =
        ProofRsWrapper.getPeriodEndEpochMillis(generateProofRs(), new ConverterUtils());

    Assertions.assertTrue(result.isPresent());
    Assertions.assertNotNull(result.get());
  }

  @Test
  void initTest() {
    Assertions.assertNotNull(new ProofRsWrapper());
  }

  private ProofRs generateProofRs() {
    final ProofRs proofRs = new ProofRs();

    final ProofDetailsRs proofDetailsRs = new ProofDetailsRs();
    final ProofUserDetails proofUserDetails = new ProofUserDetails();
    proofUserDetails.setId("user-id");
    proofDetailsRs.setUserDetails(proofUserDetails);

    proofRs.setDetails(proofDetailsRs);

    final BeneficiaryRs beneficiaryRs = new BeneficiaryRs();
    beneficiaryRs.setId("beneficiary-id");
    proofRs.setBeneficiaryData(beneficiaryRs);

    final ConsumptionEntityRs consumptionEntityRs = new ConsumptionEntityRs();
    consumptionEntityRs.setId("consumption-id");
    proofRs.setConsumptionEntity(consumptionEntityRs);

    final IndexedCertificateRs indexedCertificateRs = new IndexedCertificateRs();
    indexedCertificateRs.setStartDate("2020-01-01T00:00:00.000Z");
    indexedCertificateRs.setEndDate("2022-01-01T00:00:00.000Z");

    final CertificateRs certificateRs = new CertificateRs();
    certificateRs.setIndexedData(indexedCertificateRs);
    proofRs.setCertificate(certificateRs);

    return proofRs;
  }
}
