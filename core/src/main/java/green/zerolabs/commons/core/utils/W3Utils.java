package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.*;
import green.zerolabs.commons.core.model.graphql.generated.W3BatchRs;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * Created by triphon 15.07.22 г.
 */
public class W3Utils {

  public static Optional<ZlBatch> getZlBatch(final ZlSqsItem input) {
    return getZlBatch(getCreateBatches(input));
  }

  public static Optional<ZlBatch> getZlBatch(final Optional<ZlWeb3.CreateBatches> createBatches) {
    return createBatches.map(ZlWeb3.CreateBatches::getW3Batch);
  }

  public static Optional<ZlWeb3.CreateBatches> getCreateBatches(final ZlSqsItem input) {
    return getWeb3(input).map(ZlWeb3::getCreateBatches);
  }

  public static Optional<ZlWeb3.CertificateRegistry> getCertificateRegistry(final ZlSqsItem input) {
    return getWeb3(input).map(ZlWeb3::getCertificateRegistry);
  }

  public static Optional<ZlWeb3.ApproveBeneficiary> getApproveBeneficiary(final ZlSqsItem input) {
    return getWeb3(input).map(ZlWeb3::getApproveBeneficiary);
  }

  public static Optional<ZlWeb3.Agreement> getAgreement(final ZlSqsItem input) {
    return getWeb3(input).map(ZlWeb3::getAgreement);
  }

  public static Optional<ZlBeneficiary> getZlAgreementBeneficiary(final ZlSqsItem input) {
    return getAgreement(input).map(ZlWeb3.Agreement::getBeneficiary);
  }

  public static Optional<ZlBeneficiary> getZlBeneficiaryForApproval(final ZlSqsItem input) {
    return getApproveBeneficiary(input).map(ZlWeb3.ApproveBeneficiary::getBeneficiary);
  }

  public static Optional<ZlWeb3> getWeb3(final ZlSqsItem input) {
    return Optional.ofNullable(input).map(ZlSqsItem::getWeb3);
  }

  public static Boolean isSetRedemptionStatement(final ZlSqsItem input) {
    return isSetRedemptionStatement(getZlBatch(getCreateBatches(input)).orElse(null));
  }

  public static Boolean checkW3State(final ZlSqsItem input, final ZlWeb3.State state) {
    return getWeb3(input)
        .map(zlWeb3 -> zlWeb3.getWeb3State())
        .orElse(ZlWeb3.State.INITIAL)
        .equals(Optional.ofNullable(state).orElse(ZlWeb3.State.INITIAL));
  }

  public static Boolean isSetRedemptionStatement(final W3BatchRs w3BatchRs) {
    return Optional.ofNullable(w3BatchRs)
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getZlId()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getUserId()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getW3Id()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getRedemptionStatement()))
        .map(W3Utils::certificateIdsAreEmpty)
        .orElse(false);
  }

  public static boolean certificateIdsAreNotEmpty(final W3BatchRs w3BatchRs1) {
    return !certificateIdsAreEmpty(w3BatchRs1);
  }

  public static boolean certificateIdsAreEmpty(final W3BatchRs w3BatchRs1) {
    return Optional.ofNullable(w3BatchRs1.getCertificateIds()).stream()
        .flatMap(Collection::stream)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList())
        .isEmpty();
  }

  public static Optional<Map<String, ZlBlockchainProperties>> getBlockchainPropertiesByUser(
      final ZlSqsItem item) {
    return getWeb3(item).map(ZlWeb3::getBlockchainPropertiesByUser);
  }

  public static Optional<ZlBlockchainProperties> getBlockchainProperties(final ZlSqsItem item) {
    return getWeb3(item).map(ZlWeb3::getBlockchainProperties);
  }

  public static Boolean isMintCertificates(final ZlSqsItem input) {
    final Optional<ZlBatch> w3BatchRs = getZlBatch(input);
    final Optional<ZlWeb3.CreateBatches> createBatches = getCreateBatches(input);
    return w3BatchRs
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getZlId()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getUserId()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getW3Id()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getRedemptionStatement()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getTxHash()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getRedemptionStatementTxHash()))
        .filter(W3Utils::certificateIdsAreNotEmpty)
        .map(w3BatchRs1 -> hasZlCertificates(createBatches))
        .orElse(false);
  }

  static boolean hasZlCertificates(final Optional<ZlWeb3.CreateBatches> createBatches) {
    return !CollectionUtils.isNullOrEmpty(
        createBatches.map(ZlWeb3.CreateBatches::getZlCertificates).orElse(null));
  }

  public static Boolean isBatchMintedOnChain(final W3BatchRs w3BatchRs2) {
    final Optional<W3BatchRs> w3BatchRs = Optional.ofNullable(w3BatchRs2);
    return w3BatchRs
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getZlId()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getUserId()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getW3Id()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getRedemptionStatement()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getTxHash()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getRedemptionStatementTxHash()))
        .filter(w3BatchRs1 -> StringUtils.isNotBlank(w3BatchRs1.getMintCertificatesTxHash()))
        .filter(W3Utils::certificateIdsAreNotEmpty)
        .map(unused -> true)
        .orElse(false);
  }

  public static byte[] random32Bytes() {
    final ByteBuffer bb = ByteBuffer.wrap(new byte[32]);
    bb.put(uuidToBytes(UUID.randomUUID()));
    bb.put(uuidToBytes(UUID.randomUUID()));
    return bb.array();
  }

  public static byte[] uuidToBytes(final UUID uuid) {
    final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());

    return bb.array();
  }

}

