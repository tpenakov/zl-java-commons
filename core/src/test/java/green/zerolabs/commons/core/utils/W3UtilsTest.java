package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.*;
import green.zerolabs.commons.core.model.graphql.generated.W3BatchRs;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Created by triphon 17.07.22 г.
 */
@Getter(AccessLevel.PROTECTED)
class W3UtilsTest {

  UnitTestUtils unitTestUtils;

  @BeforeEach
  void beforeEach() {
    unitTestUtils = UnitTestUtils.of();
  }

  public static final String SQS_RQ =
      "{\n"
          + "    \"Records\": [\n"
          + "        {\n"
          + "            \"messageId\": \"7bb2ae62-a4d9-4291-a9a5-5682fb2acbae\",\n"
          + "            \"receiptHandle\": \"AQEBv/Ta19sxstyKHilpJEpoegpVRBUOXCdM8Y0XicwVpsj3IEm3tan07qEstWOYwgISOLs8ri1qlQL6EdN+oaczD/P+sMaod1OoaCSfzd2Xxlh6vZ9exwJI2QOFU9ZQylBaBNRIiCi2RagzMi0BxoYGbVOkTc9/m6I5Uy7GEP4FIC6mbLdMgFa9d2FpJ7rUs3U/A+/V2/VxnYJu5xmnb2/i+gbYA3afbcNynNGJ/G/0DZIK7Ar0+lg9YV5AVxQWSbz1xhxx9ixcojRqkdRj6oNdOtB7KE3nGwILxLWaWTa3SZP8cROqxOwzaeLmPtf6fA3t7shnTlcVfq7POoa69Z3vVxOogKN3K1+auVrSqMfO/F1M/Zgqro7EC3JAPt+vuftqrz/PAH7CZlAo0rA6W2n+oQ==\",\n"
          + "            \"body\": \"{\\\"web3\\\":{\\\"blockchainProperties\\\":{\\\"userId\\\":\\\"7e6fa7ca-e2de-4178-8c0e-db5f763a22d3\\\",\\\"netId\\\":73799,\\\"registry\\\":\\\"0xa4804e76E3b21017F062cd400b3dF6767a318ECa\\\",\\\"topic\\\":1,\\\"batches\\\":\\\"0x4eab544a98c1655086090437af1d799ca09aa436\\\",\\\"rpcNode\\\":\\\"https://volta-rpc.energyweb.org\\\",\\\"platformOperatorPrivateKey\\\":\\\"X6jLmsWrb2igcFwA2VG0s24Hlu0zo3vObjrGRnaCzUauhcrtI0nSrNGy/oQjIruos46XlXMOht4I85SAa4/9bwjbXSKDk3uaHMZm4ZcIlXI=\\\",\\\"rpcNodeFallback\\\":\\\"https://volta-rpc.energyweb.org\\\"},\\\"createBatches\\\":{\\\"w3Batch\\\":{\\\"w3Id\\\":\\\"64\\\",\\\"zlId\\\":\\\"00977121-f927-4e3b-9b33-4d96aca98a0e\\\",\\\"userId\\\":\\\"7e6fa7ca-e2de-4178-8c0e-db5f763a22d3\\\",\\\"txHash\\\":\\\"0x80b641e5eaa39d749df4764d652a9625fdce166cc49436fd32b363f90cb90b53\\\",\\\"certificateIds\\\":[\\\"\\\"],\\\"redemptionStatement\\\":\\\"10Vk003\\\",\\\"storagePointer\\\":\\\"https://google.com\\\"}}}}\",\n"
          + "            \"attributes\": {\n"
          + "                \"ApproximateReceiveCount\": \"1\",\n"
          + "                \"SentTimestamp\": \"1657981080954\",\n"
          + "                \"SenderId\": \"AROATAEUKUCM6PYMH6OYB:StorageFn-dev\",\n"
          + "                \"ApproximateFirstReceiveTimestamp\": \"1657981080962\"\n"
          + "            },\n"
          + "            \"messageAttributes\": {},\n"
          + "            \"md5OfBody\": \"2a50468e6fd460b0ca3c58e3ab6f3c63\",\n"
          + "            \"eventSource\": \"aws:sqs\",\n"
          + "            \"eventSourceARN\": \"arn:aws:sqs:eu-west-1:206469505177:sqs-queue-zlmvp-w3-dev\",\n"
          + "            \"awsRegion\": \"eu-west-1\"\n"
          + "        }\n"
          + "    ]\n"
          + "}\n";

  @Test
  void isSetRedemptionStatementTest() {

    final String sqsRq = SQS_RQ;
    final SqsEventMessage sqsEventMessage =
        getJsonUtils().readValue(sqsRq, SqsEventMessage.class).get();

    final ZlSqsItem zlSqsItem =
        getJsonUtils()
            .readValue(sqsEventMessage.getRecords().get(0).getBody(), ZlSqsItem.class)
            .get();

    Assertions.assertTrue(W3Utils.isSetRedemptionStatement(zlSqsItem));
    Assertions.assertFalse(W3Utils.isSetRedemptionStatement(ZlSqsItem.builder().build()));

    final W3BatchRs w3BatchRs = W3Utils.getZlBatch(zlSqsItem).get();
    Assertions.assertFalse(W3Utils.certificateIdsAreNotEmpty(w3BatchRs));
  }

  @Test
  void isMintCertificatesTest() {

    final String sqsRq = SQS_RQ;
    final SqsEventMessage sqsEventMessage =
        getJsonUtils().readValue(sqsRq, SqsEventMessage.class).get();

    final ZlSqsItem zlSqsItem =
        getJsonUtils()
            .readValue(sqsEventMessage.getRecords().get(0).getBody(), ZlSqsItem.class)
            .get();
    W3Utils.getZlBatch(zlSqsItem)
        .ifPresent(
            w3BatchRs -> {
              w3BatchRs.setRedemptionStatementTxHash("any_hash");
              w3BatchRs.setCertificateIds(List.of("12345"));
            });
    W3Utils.getCreateBatches(zlSqsItem)
        .ifPresent(
            createBatches ->
                createBatches.setZlCertificates(List.of(ZlCertificate.builder().build())));

    Assertions.assertTrue(W3Utils.isMintCertificates(zlSqsItem));
    Assertions.assertFalse(W3Utils.isMintCertificates(ZlSqsItem.builder().build()));
  }

  @Test
  void isBatchMintedOnChainTest() {
    final SqsEventMessage sqsEventMessage =
        getJsonUtils().readValue(SQS_RQ, SqsEventMessage.class).get();

    final ZlSqsItem zlSqsItem =
        getJsonUtils()
            .readValue(sqsEventMessage.getRecords().get(0).getBody(), ZlSqsItem.class)
            .get();
    final Optional<ZlBatch> w3BatchRsOpt = W3Utils.getZlBatch(zlSqsItem);
    w3BatchRsOpt.ifPresent(
        w3BatchRs -> {
          w3BatchRs.setRedemptionStatementTxHash("redemption_hash");
        });

    Assertions.assertFalse(W3Utils.isBatchMintedOnChain(w3BatchRsOpt.get()));
    w3BatchRsOpt.ifPresent(
        w3BatchRs -> {
          w3BatchRs.setMintCertificatesTxHash("mint_hash");
        });

    Assertions.assertFalse(W3Utils.isBatchMintedOnChain(w3BatchRsOpt.get()));
    w3BatchRsOpt.ifPresent(
        w3BatchRs -> {
          w3BatchRs.setCertificateIds(List.of("12345"));
        });

    Assertions.assertTrue(W3Utils.isBatchMintedOnChain(w3BatchRsOpt.get()));
  }

  @Test
  void getCertificateRegistryTest() {
    Assertions.assertNotNull(
        W3Utils.getCertificateRegistry(
            ZlSqsItem.builder()
                .web3(
                    ZlWeb3.builder()
                        .certificateRegistry(ZlWeb3.CertificateRegistry.builder().build())
                        .build())
                .build()));
  }

  @Test
  void getBlockchainPropertiesByUserTest() {
    Assertions.assertNotNull(
        W3Utils.getBlockchainPropertiesByUser(
            ZlSqsItem.builder()
                .web3(ZlWeb3.builder().blockchainPropertiesByUser(Map.of()).build())
                .build()));
  }

  @Test
  void getBlockchainPropertiesTest() {
    Assertions.assertNotNull(
        W3Utils.getBlockchainProperties(
            ZlSqsItem.builder()
                .web3(
                    ZlWeb3.builder()
                        .blockchainProperties(ZlBlockchainProperties.builder().build())
                        .build())
                .build()));
  }

  @Test
  void hasZlCertificatesTest() {
    Assertions.assertFalse(W3Utils.hasZlCertificates(Optional.empty()));
    Assertions.assertFalse(
        W3Utils.hasZlCertificates(Optional.of(ZlWeb3.CreateBatches.builder().build())));
    Assertions.assertTrue(
        W3Utils.hasZlCertificates(
            Optional.of(
                ZlWeb3.CreateBatches.builder()
                    .zlCertificates(List.of(ZlCertificate.builder().build()))
                    .build())));
  }

  @Test
  void getBeneficiaryTest() {
    Assertions.assertTrue(
        W3Utils.getZlBeneficiaryForApproval(
                ZlSqsItem.builder()
                    .web3(
                        ZlWeb3.builder()
                            .approveBeneficiary(
                                ZlWeb3.ApproveBeneficiary.builder()
                                    .beneficiary(ZlBeneficiary.builder().build())
                                    .build())
                            .build())
                    .build())
            .isPresent());
  }

  @Test
  void getAgreementTest() {
    Assertions.assertTrue(
        W3Utils.getAgreement(
                ZlSqsItem.builder()
                    .web3(ZlWeb3.builder().agreement(ZlWeb3.Agreement.builder().build()).build())
                    .build())
            .isPresent());
  }

  @Test
  void getZlAgreementBeneficiaryTest() {
    Assertions.assertTrue(
        W3Utils.getZlAgreementBeneficiary(
                ZlSqsItem.builder()
                    .web3(
                        ZlWeb3.builder()
                            .agreement(
                                ZlWeb3.Agreement.builder()
                                    .beneficiary(ZlBeneficiary.builder().build())
                                    .build())
                            .build())
                    .build())
            .isPresent());
  }

  @Test
  void coveragesTest() {
    new W3Utils();
  }

  @Test
  void random32BytesTest() {
    Assertions.assertEquals(32, W3Utils.random32Bytes().length);
  }

  @Test
  void checkWeb3StateTest() {
    Assertions.assertTrue(W3Utils.checkW3State(ZlSqsItem.builder().build(), null));
    Assertions.assertTrue(W3Utils.checkW3State(ZlSqsItem.builder().build(), ZlWeb3.State.INITIAL));
    Assertions.assertFalse(
        W3Utils.checkW3State(ZlSqsItem.builder().build(), ZlWeb3.State.AGREEMENT));
    Assertions.assertTrue(
        W3Utils.checkW3State(
            ZlSqsItem.builder()
                .web3(ZlWeb3.builder().web3State(ZlWeb3.State.INITIAL).build())
                .build(),
            ZlWeb3.State.INITIAL));
  }

  private JsonUtils getJsonUtils() {
    return getUnitTestUtils().getJsonUtils();
  }
}
