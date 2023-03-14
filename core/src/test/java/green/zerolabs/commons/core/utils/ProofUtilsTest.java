package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class ProofUtilsTest {

  private ConverterUtils converterUtils;

  @BeforeEach
  void beforeEach() {
    converterUtils = UnitTestUtils.of().getConverterUtils();
  }

  @Test
  void toProofDetailsRsTest() {
    final ProofRq proofRq = new ProofRq();

    final ConsumptionPeriodRq periodRq = new ConsumptionPeriodRq();
    periodRq.setStartDate("2021-01-01T00:00:00.000Z");
    periodRq.setEndDate("2022-01-01T00:00:00.000Z");
    proofRq.setConsumptionPeriod(periodRq);

    final ConsumptionLocationRq locationRq1 = new ConsumptionLocationRq();
    locationRq1.setCountry("country");
    locationRq1.setRegion("region");

    final ConsumptionLocationRq locationRq2 = new ConsumptionLocationRq();
    locationRq2.setCountry("country");

    final ConsumptionLocationRq locationRq3 = new ConsumptionLocationRq();
    locationRq3.setRegion("region");

    proofRq.setConsumptionLocations(List.of(locationRq1, locationRq2, locationRq3));

    proofRq.setConsumptionAmount(1L);

    final ProofDetailsRs proofDetailsRs =
        ProofUtils.toProofDetailsRs(proofRq, Optional.of("user-1"), converterUtils);

    Assertions.assertEquals("1", proofDetailsRs.getConsumptionAmount());
    Assertions.assertEquals("user-1", proofDetailsRs.getUserDetails().getId());

    Assertions.assertEquals(2, proofDetailsRs.getConsumptionLocations().size());
    Assertions.assertEquals("region", proofDetailsRs.getConsumptionLocations().get(0).getRegion());
    Assertions.assertEquals(
        "country", proofDetailsRs.getConsumptionLocations().get(0).getCountry());

    Assertions.assertNull(proofDetailsRs.getConsumptionLocations().get(1).getRegion());
    Assertions.assertEquals(
        "country", proofDetailsRs.getConsumptionLocations().get(1).getCountry());

    Assertions.assertEquals("2021-01-01T00:00:00.000Z", proofDetailsRs.getStartDate());
    Assertions.assertEquals("2022-01-01T00:00:00.000Z", proofDetailsRs.getEndDate());

    Assertions.assertEquals(
        "country", proofDetailsRs.getConsumptionLocations().get(0).getCountry());
    Assertions.assertEquals("region", proofDetailsRs.getConsumptionLocations().get(0).getRegion());
  }

  @Test
  void toProofDetailsRsWhenLocationsAreEmpty() {
    final ProofRq proofRq = new ProofRq();

    final ProofDetailsRs proofDetailsRs =
        ProofUtils.toProofDetailsRs(proofRq, Optional.of("user-1"), converterUtils);

    Assertions.assertNull(proofDetailsRs.getConsumptionLocations());
  }


  @Test
  void testInit() {
    Assertions.assertNotNull(new ProofUtils());
  }

  @Test
  void fixProofRsTest() {
    final String redemptionDate = "2022-12-12";
    final ProofRs proofRs = new ProofRs();
    final CertificateRs certificateRs = new CertificateRs();
    proofRs.setRootCertificates(List.of(certificateRs));
    final CertificateDataRs certificateData = new CertificateDataRs();
    certificateRs.setCertificateData(certificateData);
    certificateData.setRedemptionDate(redemptionDate);
    ProofUtils.fixProofRs(proofRs);
    Assertions.assertNotEquals(
        redemptionDate,
        proofRs.getRootCertificates().get(0).getCertificateData().getRedemptionDate());

    Assertions.assertDoesNotThrow(() -> ProofUtils.fixProofRs(null));
    Assertions.assertDoesNotThrow(() -> ProofUtils.fixProofRs(new ProofRs()));
  }
}
