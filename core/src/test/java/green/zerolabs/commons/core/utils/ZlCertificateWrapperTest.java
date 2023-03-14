package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.ZlCertificate;
import green.zerolabs.commons.core.model.ZlCertificateData;
import green.zerolabs.commons.core.model.ZlOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Created by triphon 12.08.22 г.
 */
class ZlCertificateWrapperTest {

  @Test
  void getZlRedemptionEmptyStreamTest() {
    Assertions.assertEquals(0, ZlCertificateWrapper.getZlCertificateDataStream(null).count());
  }

  @Test
  void getZlRedemptionStreamTest() {
    Assertions.assertEquals(
        1,
        ZlCertificateWrapper.getZlCertificateDataStream(
                ZlCertificate.builder()
                    .dataWithRootCertificates(
                        List.of(
                            ZlCertificate.DataWithRootCertificate.builder()
                                .root(
                                    ZlCertificate.builder()
                                        .certificateData(ZlCertificateData.builder().build())
                                        .build())
                                .build()))
                    .build())
            .count());
  }

  @Test
  void getZlRootCertificateEmptyStreamTest() {
    Assertions.assertEquals(0, ZlCertificateWrapper.getZlRootCertificateStream(null).count());
    Assertions.assertEquals(
        0,
        ZlCertificateWrapper.getZlRootCertificateStream(
                ZlCertificate.builder()
                    .dataWithRootCertificates(
                        List.of(ZlCertificate.DataWithRootCertificate.builder().build()))
                    .build())
            .count());
  }

  @Test
  void getZlRootCertificateStreamTest() {
    Assertions.assertEquals(
        1,
        ZlCertificateWrapper.getZlRootCertificateStream(
                ZlCertificate.builder()
                    .dataWithRootCertificates(
                        List.of(
                            ZlCertificate.DataWithRootCertificate.builder()
                                .root(ZlCertificate.builder().build())
                                .build()))
                    .build())
            .count());
  }

  @Test
  void coveragesTest() {
    Assertions.assertNotNull(new ZlCertificateWrapper());
  }

  @Test
  void extractAgreementIdStreamTest() {
    final ZlCertificate.Status status = ZlCertificate.Status.ACTIVE;
    final ZlCertificate certificate = ZlCertificate.builder().status(status).build();

    final String orderId = "id";
    final String agreementIdOrder = "agreement-id-order";
    final List<String> agreementIdList = List.of(agreementIdOrder);
    final ZlOrder order = ZlOrder.builder().id(orderId).agreementIdList(agreementIdList).build();
    certificate.setOrder(order);
    Assertions.assertEquals(
        agreementIdList,
        ZlCertificateWrapper.extractAgreementIdStream(List.of(certificate))
            .collect(Collectors.toList()));

    final String agreementIdRedemption = "agreement-id-redemption";
    final List<String> agreementIdList1 = List.of(agreementIdRedemption);
    final String redemptionId = "redemptionId";
    final ZlCertificateData redemption =
        ZlCertificateData.builder().id(redemptionId).agreementIdList(agreementIdList1).build();
    certificate.setCertificateData(redemption);
    Assertions.assertEquals(
        agreementIdList1,
        ZlCertificateWrapper.extractAgreementIdStream(List.of(certificate))
            .collect(Collectors.toList()));

    order.setAgreementIdList(List.of());
    redemption.setAgreementIdList(List.of());
    Assertions.assertEquals(
        0, ZlCertificateWrapper.extractAgreementIdStream(List.of(certificate)).count());
  }

  @Test
  void extractAgreementDistributionIdList_WhenEmptyAgreementDistribution_OkTest() {
    final ZlCertificate zlCertificate =
        ZlCertificate.builder()
            .certificateData(ZlCertificateData.builder().agreementDistributions(List.of()).build())
            .build();

    Assertions.assertTrue(
        ZlCertificateWrapper.extractAgreementDistributionIdList(zlCertificate).isEmpty());
  }

  @Test
  void extractAgreementDistributionIdList_WhenAgreementDistributionWithNoId_OkTest() {
    final ZlCertificate zlCertificate =
        ZlCertificate.builder()
            .certificateData(
                ZlCertificateData.builder()
                    .agreementDistributions(
                        List.of(ZlCertificateData.AgreementDistribution.builder().build()))
                    .build())
            .build();

    Assertions.assertTrue(
        ZlCertificateWrapper.extractAgreementDistributionIdList(zlCertificate).stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList())
            .isEmpty());
  }

  @Test
  void extractAgreementIdStream_WhenAgreementDistributionWithId_OkTest() {
    final String agreementId = "agreementId";
    final String agreementId2 = "agreementId2";
    final String agreementId3 = "agreementId3";
    final ZlCertificate zlCertificate =
        ZlCertificate.builder()
            .certificateData(
                ZlCertificateData.builder()
                    .agreementDistributions(
                        List.of(
                            ZlCertificateData.AgreementDistribution.builder()
                                .agreementId(agreementId)
                                .build()))
                    .agreementIdList(List.of(agreementId2))
                    .build())
            .order(ZlOrder.builder().agreementIdList(List.of(agreementId3)).build())
            .indexedData(
                ZlCertificate.IndexedCertificate.builder().agreementId(agreementId).build())
            .build();

    final List<String> idList =
        ZlCertificateWrapper.extractAgreementIdStream(List.of(zlCertificate))
            .collect(Collectors.toList());
    Assertions.assertFalse(CollectionUtils.isNullOrEmpty(idList));
    Assertions.assertEquals(1, idList.size());
    Assertions.assertEquals(agreementId, idList.get(0));
    Assertions.assertFalse(idList.contains(agreementId2));
    Assertions.assertFalse(idList.contains(agreementId3));
  }

  @Test
  void isOnChainStatusTest() {
    final RuntimeException exception =
        Assertions.assertThrows(
            RuntimeException.class, () -> ZlCertificateWrapper.isOnChainStatus(null));
    Assertions.assertEquals(ZlCertificateWrapper.NULL_CERTIFICATE_STATUS, exception.getMessage());

    final ZlCertificate certificate = ZlCertificate.builder().build();

    certificate.setStatus(ZlCertificate.Status.CREATED);
    Assertions.assertFalse(ZlCertificateWrapper.isOnChainStatus(certificate));
    certificate.setStatus(ZlCertificate.Status.CANCELLED);
    Assertions.assertFalse(ZlCertificateWrapper.isOnChainStatus(certificate));
    certificate.setStatus(ZlCertificate.Status.ERROR);
    Assertions.assertFalse(ZlCertificateWrapper.isOnChainStatus(certificate));

    certificate.setStatus(ZlCertificate.Status.AGREEMENT_ACTIVE);
    Assertions.assertTrue(ZlCertificateWrapper.isOnChainStatus(certificate));
    certificate.setStatus(ZlCertificate.Status.PARENT);
    Assertions.assertTrue(ZlCertificateWrapper.isOnChainStatus(certificate));
    certificate.setStatus(ZlCertificate.Status.CLAIMED);
    Assertions.assertTrue(ZlCertificateWrapper.isOnChainStatus(certificate));
  }

  @Test
  void createSortKeyPrefixTest() {
    final ZlCertificate.Status status = ZlCertificate.Status.ACTIVE;
    final ZlCertificate certificate = ZlCertificate.builder().status(status).build();
    Assertions.assertEquals(status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));

    final String orderId = "id";
    final List<String> agreementIdList = List.of("agreement1Id");
    final ZlOrder order = ZlOrder.builder().id(orderId).agreementIdList(agreementIdList).build();
    certificate.setOrder(order);
    Assertions.assertEquals(
        orderId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));
    Assertions.assertEquals(
        orderId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(orderId, status));

    final String redemptionId = "redemptionId";
    final ZlCertificateData redemption =
        ZlCertificateData.builder().id(redemptionId).agreementIdList(agreementIdList).build();
    certificate.setCertificateData(redemption);
    Assertions.assertEquals(
        redemptionId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));

    order.setAgreementIdList(null);
    redemption.setAgreementIdList(null);
    Assertions.assertEquals(status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));
  }

  @Test
  void createSortKeyPrefix_RedemptionWithMultipleAgreements_OkTest() {
    final ZlCertificate.Status status = ZlCertificate.Status.AGREEMENT_ACTIVE;
    final ZlCertificate certificate = ZlCertificate.builder().status(status).build();

    final String orderId = "id";
    final List<String> agreementIdList = List.of("agreement1Id", "agreement2Id");
    final ZlOrder order = ZlOrder.builder().id(orderId).agreementIdList(agreementIdList).build();
    certificate.setOrder(order);
    Assertions.assertEquals(
        orderId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));
    Assertions.assertEquals(
        orderId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(orderId, status));

    final String redemptionId = "redemptionId";
    final ZlCertificateData redemption =
        ZlCertificateData.builder().id(redemptionId).agreementIdList(agreementIdList).build();
    certificate.setCertificateData(redemption);
    Assertions.assertEquals(
        redemptionId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));
  }

  @Test
  void createSortKeyWithInventoryPrefixTest() {
    final String inventoryId = "inventoryId";
    final ZlCertificate.Status status = ZlCertificate.Status.ACTIVE;
    final ZlCertificate certificate = ZlCertificate.builder().status(status).build();
    Assertions.assertEquals(status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));

    final String orderId = "id";
    final List<String> agreementIdList = List.of("agreement1Id");
    final ZlOrder order =
        ZlOrder.builder()
            .id(orderId)
            .agreementIdList(agreementIdList)
            .inventoryId(inventoryId)
            .build();
    certificate.setOrder(order);
    Assertions.assertEquals(
        inventoryId + orderId + status.name(),
        ZlCertificateWrapper.createSortKeyPrefix(certificate));
    order.setInventoryId(null);

    final String redemptionId = "redemptionId";
    final ZlCertificateData redemption =
        ZlCertificateData.builder()
            .id(redemptionId)
            .agreementIdList(agreementIdList)
            .inventoryId(inventoryId)
            .build();
    certificate.setCertificateData(redemption);
    Assertions.assertEquals(
        inventoryId + redemptionId + status.name(),
        ZlCertificateWrapper.createSortKeyPrefix(certificate));
    redemption.setInventoryId(null);

    order.setAgreementIdList(null);
    redemption.setAgreementIdList(null);
    certificate.setInventoryId(inventoryId);
    Assertions.assertEquals(
        inventoryId + status.name(), ZlCertificateWrapper.createSortKeyPrefix(certificate));
  }
}
