package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.ZlCertificateData;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/***
 * Created by Triphon Penakov 2022-12-14
 */
class ZlCertificateDataUtilsTest {
  @Test
  void setAgreementDistributionsTest() {
    final String input =
        "317d638a-7340-44eb-a171-b82f175b78dc:10:MWh,"
            + "00bd3b96-3bbe-4133-8abd-af46a1c88942:3000:KWh,"
            + "f487f316-2d3d-409f-b94f-1d2c039e32a2:4000:Wh";

    final ZlCertificateData certificateData =
        ZlCertificateData.builder().agreementDistributionsCsv(input).build();
    ZlCertificateDataUtils.setAgreementDistributions(certificateData);

    final List<ZlCertificateData.AgreementDistribution> agreementDistributions =
        certificateData.getAgreementDistributions();
    assertNotNull(agreementDistributions);
    assertEquals(3, agreementDistributions.size());
    assertEquals(
        List.of(
            ZlCertificateData.AgreementDistribution.builder()
                .agreementId("317d638a-7340-44eb-a171-b82f175b78dc")
                .energyAmount(BigDecimal.valueOf(10000000))
                .energyUnit(ConsumptionUnit.Wh)
                .build(),
            ZlCertificateData.AgreementDistribution.builder()
                .agreementId("00bd3b96-3bbe-4133-8abd-af46a1c88942")
                .energyAmount(BigDecimal.valueOf(3000000))
                .energyUnit(ConsumptionUnit.Wh)
                .build(),
            ZlCertificateData.AgreementDistribution.builder()
                .agreementId("f487f316-2d3d-409f-b94f-1d2c039e32a2")
                .energyAmount(BigDecimal.valueOf(4000))
                .energyUnit(ConsumptionUnit.Wh)
                .build()),
        agreementDistributions);
  }

  @Test
  void setAgreementDistributions_WhenNoAgreementDistribution_OkTest() {
    final ZlCertificateData certificateData = ZlCertificateData.builder().build();
    ZlCertificateDataUtils.setAgreementDistributions(certificateData);
    assertNull(certificateData.getAgreementDistributions());
    assertNull(certificateData.getAgreementDistributionsCsv());
  }

  @Test
  void setAgreementDistributions_WithAgreementDistributionList_OkTest() {
    final ZlCertificateData certificateData =
        ZlCertificateData.builder()
            .agreementDistributions(
                List.of(ZlCertificateData.AgreementDistribution.builder().build()))
            .build();
    ZlCertificateDataUtils.setAgreementDistributions(certificateData);
    assertFalse(CollectionUtils.isNullOrEmpty(certificateData.getAgreementDistributions()));
    assertNull(certificateData.getAgreementDistributionsCsv());
  }

  @Test
  void fixDateStringsTest() {
    final String redemptionDate = "2022-12-12";
    final ZlCertificateData certificateData =
        ZlCertificateData.builder().redemptionDate(redemptionDate).build();
    ZlCertificateDataUtils.fixDateStrings(certificateData);
    assertNotEquals(redemptionDate, certificateData.getRedemptionDate());
  }

  @Test
  void initTest() {
    assertNotNull(new ZlCertificateDataUtils());
  }
}
