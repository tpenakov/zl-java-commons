package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.ZlCertificate;
import green.zerolabs.commons.core.model.ZlCertificateData;
import green.zerolabs.commons.core.model.ZlOrder;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static green.zerolabs.commons.core.model.CoreConstants.EMPTY;

/*
 * Created by triphon 12.08.22 г.
 */
public class ZlCertificateWrapper {

  public static final String NULL_CERTIFICATE_STATUS = "null certificate status";

  public static Stream<ZlCertificateData> getZlCertificateDataStream(
      final ZlCertificate zlCertificate) {
    return getZlRootCertificateStream(zlCertificate)
        .map(ZlCertificate::getCertificateData)
        .filter(Objects::nonNull);
  }

  public static Stream<ZlCertificate> getZlRootCertificateStream(
      final ZlCertificate zlCertificate) {
    return Optional.ofNullable(zlCertificate)
        .map(ZlCertificate::getDataWithRootCertificates)
        .stream()
        .flatMap(Collection::stream)
        .map(ZlCertificate.DataWithRootCertificate::getRoot)
        .filter(Objects::nonNull);
  }

  public static String createSortKeyPrefix(final ZlCertificate certificate) {
    return createSortKeyPrefix(certificate, agreementPrefix(certificate));
  }

  public static String createSortKeyPrefix(
      final ZlCertificate certificate, final String agreementId) {
    final String name = certificate.getStatus().name();
    final String prefix = inventoryIdPrefix(certificate) + agreementId;
    return createSortKeyPrefix(prefix, name);
  }

  public static Stream<String> extractAgreementIdStream(
      final Collection<ZlCertificate> certificates) {
    return certificates.stream()
        .flatMap(
            zlCertificate ->
                Stream.concat(
                    Stream.of(zlCertificate),
                    Optional.ofNullable(zlCertificate.getDataWithRootCertificates()).stream()
                        .flatMap(Collection::stream)
                        .map(ZlCertificate.DataWithRootCertificate::getRoot)))
        .flatMap(
            zlCertificate ->
                extractAgreementDistributionIdList(zlCertificate)
                    .or(
                        () ->
                            Optional.ofNullable(zlCertificate.getCertificateData())
                                .map(ZlCertificateData::getAgreementIdList)
                                .filter(idList -> !CollectionUtils.isNullOrEmpty(idList)))
                    .or(
                        () ->
                            Optional.ofNullable(zlCertificate.getOrder())
                                .map(ZlOrder::getAgreementIdList)
                                .filter(idList -> !CollectionUtils.isNullOrEmpty(idList)))
                    .stream()
                    .flatMap(Collection::stream))
        .distinct();
  }

  static Optional<List<String>> extractAgreementDistributionIdList(
      final ZlCertificate zlCertificate) {

    final Optional<String> certificateAgreementId =
        Optional.ofNullable(zlCertificate.getIndexedData())
            .map(ZlCertificate.IndexedCertificate::getAgreementId)
            .filter(StringUtils::isNotBlank);

    return Optional.ofNullable(zlCertificate.getCertificateData())
        .map(ZlCertificateData::getAgreementDistributions)
        .filter(list -> !CollectionUtils.isNullOrEmpty(list))
        .map(
            agreementDistributions ->
                agreementDistributions.stream()
                    .map(ZlCertificateData.AgreementDistribution::getAgreementId)
                    .filter(StringUtils::isNotBlank)
                    .filter(
                        agreementId ->
                            certificateAgreementId.map(s -> s.equals(agreementId)).orElse(false))
                    .collect(Collectors.toList()));
  }

  public static boolean isOnChainStatus(final ZlCertificate certificate) {
    final ZlCertificate.Status certificateStatus =
        Optional.ofNullable(certificate)
            .map(ZlCertificate::getStatus)
            .orElseThrow(() -> new RuntimeException(NULL_CERTIFICATE_STATUS));

    final int ordinal = certificateStatus.ordinal();
    return ordinal >= ZlCertificate.Status.AGREEMENT_ACTIVE.ordinal()
        && ordinal <= ZlCertificate.Status.CLAIMED.ordinal();
  }

  static String inventoryIdPrefix(final ZlCertificate certificate) {
    final Optional<ZlOrder> order = Optional.ofNullable(certificate.getOrder());
    final Optional<ZlCertificateData> redemption =
        Optional.ofNullable(certificate.getCertificateData());
    return order
        .map(ZlOrder::getInventoryId)
        .filter(StringUtils::isNotBlank)
        .or(() -> redemption.map(ZlCertificateData::getInventoryId).filter(StringUtils::isNotBlank))
        .or(() -> Optional.ofNullable(certificate.getInventoryId()).filter(StringUtils::isNotBlank))
        .orElse(EMPTY);
  }

  static String agreementPrefix(final ZlCertificate certificate) {
    final Optional<ZlOrder> order = Optional.ofNullable(certificate.getOrder());
    final Optional<ZlCertificateData> redemption =
        Optional.ofNullable(certificate.getCertificateData());
    final Optional<String> orderId =
        order
            .filter(item -> !CollectionUtils.isNullOrEmpty(item.getAgreementIdList()))
            .map(ZlOrder::getId);
    final Optional<String> redemptionId =
        redemption
            .filter(item -> !CollectionUtils.isNullOrEmpty(item.getAgreementIdList()))
            .map(ZlCertificateData::getId);
    final Optional<String> agreementId =
        Optional.ofNullable(certificate.getIndexedData())
            .map(ZlCertificate.IndexedCertificate::getAgreementId);

    return agreementId.or(() -> redemptionId).or(() -> orderId).orElse(EMPTY);
  }

  public static String createSortKeyPrefix(final String id, final ZlCertificate.Status status) {
    return createSortKeyPrefix(id, status.name());
  }

  public static String createSortKeyPrefix(final String id, final String name) {
    return id.trim() + name.trim();
  }
}
