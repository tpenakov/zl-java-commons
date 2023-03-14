package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ProofRs;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

import static green.zerolabs.commons.core.model.ZlAgreementConstants.*;
import static green.zerolabs.commons.core.model.ZlBatchConstants.*;
import static green.zerolabs.commons.core.model.ZlBlockchainPropertiesConstants.*;
import static green.zerolabs.commons.core.model.ZlCertificateConstants.*;

/***
 * Created by Triphon Penakov 2023-02-16
 */
@SuppressWarnings({"unused"})
public class CoreModelConstants {

  public static String getPrefix(final Class<?> clazz) {
    return ensureMap().get(clazz).getPrefix();
  }

  public static String getDataField(final Class<?> clazz) {
    return ensureMap().get(clazz).getDataField();
  }

  public static String getVersionField(final Class<?> clazz) {
    return ensureMap().get(clazz).getVersionField();
  }

  public static Long getCurrentVersion(final Class<?> clazz) {
    return ensureMap().get(clazz).getCurrentVersion();
  }

  public static final String DATA = "data";

  private static Map<Class<?>, Commons> commonsMap;

  private static Map<Class<?>, Commons> ensureMap() {
    if (Objects.isNull(commonsMap)) {
      commonsMap = initMap();
    }
    return commonsMap;
  }

  private static Map<Class<?>, Commons> initMap() {
    return Map.ofEntries(
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlAgreement.class,
            Commons.builder()
                .clazz(ZlAgreement.class)
                .prefix(AGREEMENT_PREFIX)
                .dataField(AGREEMENT_DATA)
                .versionField(AGREEMENT_VERSION)
                .currentVersion(AGREEMENT_CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlBlockchainProperties.class,
            Commons.builder()
                .clazz(ZlBlockchainProperties.class)
                .prefix(W3BLOCKCHAIN_PROPERTIES_PREFIX)
                .dataField(null)
                .versionField(W3BLOCKCHAIN_PROPERTIES_VERSION)
                .currentVersion(CURRENT_W3BLOCKCHAIN_PROPERTIES_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlBatch.class,
            Commons.builder()
                .clazz(ZlBatch.class)
                .prefix(W3BATCH_PREFIX)
                .dataField(W3BATCH_DATA)
                .versionField(W3BATCH_VERSION)
                .currentVersion(CURRENT_W3BATCH_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlBeneficiary.class,
            Commons.builder()
                .clazz(ZlBeneficiary.class)
                .prefix(ZlBeneficiaryConstants.PREFIX)
                .dataField(ZlBeneficiaryConstants.DATA)
                .versionField(ZlBeneficiaryConstants.VERSION)
                .currentVersion(ZlBeneficiaryConstants.CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlCertificate.class,
            Commons.builder()
                .clazz(ZlCertificate.class)
                .prefix(ZL_CERTIFICATE_PREFIX)
                .dataField(ZL_CERTIFICATE_DATA)
                .versionField(ZL_CERTIFICATE_VERSION)
                .currentVersion(ZL_CERTIFICATE_CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlCertificateData.class,
            Commons.builder()
                .clazz(ZlCertificateData.class)
                .prefix(ZlCertificateDataConstants.PREFIX)
                .dataField(ZlCertificateDataConstants.DATA)
                .versionField(ZlCertificateDataConstants.VERSION)
                .currentVersion(ZlCertificateDataConstants.CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlGenerator.class,
            Commons.builder()
                .clazz(ZlGenerator.class)
                .prefix(ZlGeneratorConstants.PREFIX)
                .dataField(ZlGeneratorConstants.DATA)
                .versionField(ZlGeneratorConstants.VERSION)
                .currentVersion(ZlGeneratorConstants.CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ProofRs.class,
            Commons.builder()
                .clazz(ProofRs.class)
                .prefix(ProofConstants.PREFIX)
                .dataField(ProofConstants.DATA)
                .versionField(ProofConstants.VERSION)
                .currentVersion(ProofConstants.CURRENT_PROOF_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlOrder.class,
            Commons.builder()
                .clazz(ZlOrder.class)
                .prefix(ZlOrderConstants.PREFIX)
                .dataField(ZlOrderConstants.DATA)
                .versionField(ZlOrderConstants.VERSION)
                .currentVersion(ZlOrderConstants.CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlSeller.class,
            Commons.builder()
                .clazz(ZlSeller.class)
                .prefix(ZlSellerConstants.PREFIX)
                .dataField(ZlSellerConstants.DATA)
                .versionField(ZlSellerConstants.VERSION)
                .currentVersion(ZlSellerConstants.CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlSupplier.class,
            Commons.builder()
                .clazz(ZlSupplier.class)
                .prefix(ZlSupplierConstants.PREFIX)
                .dataField(ZlSupplierConstants.DATA)
                .versionField(ZlSupplierConstants.VERSION)
                .currentVersion(ZlSupplierConstants.CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlOffer.class,
            Commons.builder()
                .clazz(ZlOffer.class)
                .prefix(ZlOfferConstants.OFFER_PREFIX)
                .dataField(ZlOfferConstants.OFFER_DATA)
                .versionField(ZlOfferConstants.OFFER_VERSION)
                .currentVersion(ZlOfferConstants.OFFER_CURRENT_VERSION)
                .build()),
        new AbstractMap.SimpleEntry<Class<?>, Commons>(
            ZlUserDetails.class,
            Commons.builder()
                .clazz(ZlUserDetails.class)
                .prefix(ZlUserDetailsConstants.PREFIX)
                .dataField(ZlUserDetailsConstants.DATA)
                .versionField(ZlUserDetailsConstants.VERSION)
                .currentVersion(ZlUserDetailsConstants.CURRENT_VERSION)
                .build()));
  }

  @Data
  @Builder
  @RegisterForReflection
  public static class Commons {
    private final Class<?> clazz;
    private final String prefix;
    private final String dataField;
    private final String versionField;
    private final Long currentVersion;
  }
}
