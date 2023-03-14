package green.zerolabs.commons.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.model.CoreConstants;
import green.zerolabs.commons.core.model.GraphQlRequest;
import green.zerolabs.commons.core.model.ZlAgreement;
import green.zerolabs.commons.core.model.ZlOrder;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.ProofRq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static green.zerolabs.commons.core.model.ProofConstants.ADD_PROOF;

class ZlAgreementWrapperTest {

  public static final String ERROR = "error";

  private ConverterUtils converterUtils;

  @BeforeEach
  void beforeEach() {
    converterUtils = UnitTestUtils.of().getConverterUtils();
  }

  @Test
  void initTest() {
    Assertions.assertNotNull(new ZlAgreementWrapper());
  }

  @Test
  void fillAgreementTest() {

    final ZlAgreement agreement = ZlAgreement.builder().build();

    final ZlOrder order =
        ZlOrder.builder()
            .id("id")
            .productType("IREC")
            .energySources("energySources")
            .countryRegionList("countryRegionList")
            .deliveryDate(Instant.now())
            .reportingStart(Instant.now())
            .reportingEnd(Instant.now())
            .sellerId("sellerId")
            .supplierId("supplierId")
            .label("label")
            .build();

    ZlAgreementWrapper.fillAgreement(agreement, order);

    Assertions.assertEquals(order.getId(), agreement.getOrderId());
    Assertions.assertEquals(order.getProductType(), agreement.getProductType().name());
    Assertions.assertEquals(order.getEnergySources(), agreement.getEnergySources());
    Assertions.assertEquals(order.getCountryRegionList(), agreement.getCountryRegionList());
    Assertions.assertEquals(order.getDeliveryDate(), agreement.getOrderDate());
    Assertions.assertEquals(order.getReportingStart(), agreement.getReportingStart());
    Assertions.assertEquals(order.getReportingEnd(), agreement.getReportingEnd());
    Assertions.assertEquals(order.getSellerId(), agreement.getSellerId());
    Assertions.assertEquals(order.getSupplierId(), agreement.getSupplierId());
    Assertions.assertEquals(order.getLabel(), agreement.getLabel());
  }

  @Test
  void fillAgreement_BlankProductTypeTest() {

    final ZlAgreement agreement = ZlAgreement.builder().build();

    final ZlOrder order =
        ZlOrder.builder()
            .id("id")
            .productType("")
            .energySources("energySources")
            .countryRegionList("countryRegionList")
            .deliveryDate(Instant.now())
            .reportingStart(Instant.now())
            .reportingEnd(Instant.now())
            .sellerId("sellerId")
            .supplierId("supplierId")
            .label("label")
            .build();

    ZlAgreementWrapper.fillAgreement(agreement, order);

    Assertions.assertEquals(order.getId(), agreement.getOrderId());
    Assertions.assertNull(agreement.getProductType());
    Assertions.assertEquals(order.getEnergySources(), agreement.getEnergySources());
    Assertions.assertEquals(order.getCountryRegionList(), agreement.getCountryRegionList());
    Assertions.assertEquals(order.getDeliveryDate(), agreement.getOrderDate());
    Assertions.assertEquals(order.getReportingStart(), agreement.getReportingStart());
    Assertions.assertEquals(order.getReportingEnd(), agreement.getReportingEnd());
    Assertions.assertEquals(order.getSellerId(), agreement.getSellerId());
    Assertions.assertEquals(order.getSupplierId(), agreement.getSupplierId());
    Assertions.assertEquals(order.getLabel(), agreement.getLabel());
  }

  @Test
  void fillProofRqTest() {
    final String metadata = "Metadata";
    final ZlAgreement agreement =
        ZlAgreement.builder()
            .reportingStart(Instant.MIN)
            .reportingEnd(Instant.now())
            .energyAmount(BigDecimal.ONE)
            .beneficiaryId("beneficiary-id")
            .countryRegionList("country-region|")
            .metadata(metadata)
            .build();

    final ProofRq proofRq = new ProofRq();

    ZlAgreementWrapper.fillProofRq(proofRq, agreement);

    Assertions.assertEquals(
        "-1000000000-01-01T00:00:00Z", proofRq.getConsumptionPeriod().getStartDate());
    Assertions.assertNotNull(proofRq.getConsumptionPeriod().getEndDate());
    Assertions.assertEquals(1L, proofRq.getConsumptionAmount());
    Assertions.assertEquals(ConsumptionUnit.Wh, proofRq.getConsumptionUnit());
    Assertions.assertEquals("beneficiary-id", proofRq.getBeneficiaryId());
    Assertions.assertEquals(1, proofRq.getConsumptionLocations().size());
    Assertions.assertEquals("country", proofRq.getConsumptionLocations().get(0).getCountry());
    Assertions.assertEquals("region", proofRq.getConsumptionLocations().get(0).getRegion());
    Assertions.assertEquals(metadata, proofRq.getMetadata());
  }

  @Test
  void fillProofRqBlankCountryTest() {
    final ZlAgreement agreement =
        ZlAgreement.builder()
            .reportingStart(Instant.MIN)
            .reportingEnd(Instant.now())
            .energyAmount(BigDecimal.ONE)
            .beneficiaryId("beneficiary-id")
            .countryRegionList("-region|")
            .build();

    final ProofRq proofRq = new ProofRq();

    ZlAgreementWrapper.fillProofRq(proofRq, agreement);

    Assertions.assertEquals(
        "-1000000000-01-01T00:00:00Z", proofRq.getConsumptionPeriod().getStartDate());
    Assertions.assertNotNull(proofRq.getConsumptionPeriod().getEndDate());
    Assertions.assertEquals(1L, proofRq.getConsumptionAmount());
    Assertions.assertEquals(ConsumptionUnit.Wh, proofRq.getConsumptionUnit());
    Assertions.assertEquals("beneficiary-id", proofRq.getBeneficiaryId());
    Assertions.assertTrue(CollectionUtils.isNullOrEmpty(proofRq.getConsumptionLocations()));
  }

  @Test
  void ensureError_Ok_Test() {
    final ZlAgreement agreement = ZlAgreement.builder().build();
    ZlAgreementWrapper.ensureError(agreement, ERROR);
    final List<String> errors = agreement.getErrors();
    Assertions.assertFalse(CollectionUtils.isNullOrEmpty(errors));
    Assertions.assertEquals(ZlAgreement.Status.ERROR, agreement.getAgreementStatus());
    Assertions.assertEquals(1, errors.size());
    Assertions.assertEquals(ERROR, errors.get(0));
  }

  @Test
  void isError_Positive_Test() {
    final ZlAgreement agreement =
        ZlAgreement.builder().agreementStatus(ZlAgreement.Status.ERROR).build();
    Assertions.assertTrue(ZlAgreementWrapper.isError(agreement));
  }

  @Test
  void isError_Negative_Test() {
    final ZlAgreement agreement =
        ZlAgreement.builder().agreementStatus(ZlAgreement.Status.INITIAL).build();
    Assertions.assertFalse(ZlAgreementWrapper.isError(agreement));
  }

  @Test
  void isError_NullStatus_NegativeTest() {
    final ZlAgreement agreement = ZlAgreement.builder().build();
    Assertions.assertFalse(ZlAgreementWrapper.isError(agreement));
  }

  @Test
  void toAddProofGraphQlRequestTest() {
    final String sellerId = "seller-id";
    final String userId = "user-id";
    final String agreementId = "agreement-id";
    final ZlAgreement agreement =
        ZlAgreement.builder().id(agreementId).sellerId(sellerId).userId(userId).build();
    final GraphQlRequest graphQlRequest =
        ZlAgreementWrapper.toAddProofGraphQlRequest(agreement, new JsonUtils(new ObjectMapper()));

    Assertions.assertEquals(CoreConstants.MUTATION, graphQlRequest.getTypeName());
    Assertions.assertEquals(ADD_PROOF, graphQlRequest.getFieldName());
    Assertions.assertEquals(userId, graphQlRequest.getIdentity().getUsername());
    Assertions.assertEquals(
        agreementId,
        ((Map<String, Object>) graphQlRequest.getArguments().get("body")).get("agreementId"));
  }

  @Test
  void toConsumptionLocations_EmptyList_Test() {
    Assertions.assertTrue(
        CollectionUtils.isNullOrEmpty(
            ZlAgreementWrapper.toConsumptionLocations(ZlAgreement.builder().build())));
  }
}
