package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ZlSqsItemWrapperTest {

  @Test
  void getStore() {
    Assertions.assertTrue(ZlSqsItemWrapper.getStore(null).isEmpty());
  }

  @Test
  void getStoreBeneficiaryList() {
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreBeneficiaryList(
                ZlSqsItem.builder().store(ZlSqsItem.Store.builder().build()).build())
            .isEmpty());
    Assertions.assertFalse(
        ZlSqsItemWrapper.getStoreBeneficiaryList(
                ZlSqsItem.builder()
                    .store(ZlSqsItem.Store.builder().beneficiaryList(List.of()).build())
                    .build())
            .isPresent());
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreBeneficiaryList(
                ZlSqsItem.builder()
                    .store(
                        ZlSqsItem.Store.builder()
                            .beneficiaryList(List.of(ZlBeneficiary.builder().build()))
                            .build())
                    .build())
            .isPresent());
  }

  @Test
  void getStoreOrderList() {
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreOrderList(
                ZlSqsItem.builder().store(ZlSqsItem.Store.builder().build()).build())
            .isEmpty());
    Assertions.assertFalse(
        ZlSqsItemWrapper.getStoreOrderList(
                ZlSqsItem.builder()
                    .store(ZlSqsItem.Store.builder().orderList(List.of()).build())
                    .build())
            .isPresent());
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreOrderList(
                ZlSqsItem.builder()
                    .store(
                        ZlSqsItem.Store.builder()
                            .orderList(List.of(ZlOrder.builder().build()))
                            .build())
                    .build())
            .isPresent());
  }

  @Test
  void getStoreAgreementListTest() {
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreAgreementList(
                ZlSqsItem.builder().store(ZlSqsItem.Store.builder().build()).build())
            .isEmpty());
    Assertions.assertFalse(
        ZlSqsItemWrapper.getStoreAgreementList(
                ZlSqsItem.builder()
                    .store(ZlSqsItem.Store.builder().agreementList(List.of()).build())
                    .build())
            .isPresent());
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreAgreementList(
                ZlSqsItem.builder()
                    .store(
                        ZlSqsItem.Store.builder()
                            .agreementList(List.of(ZlAgreement.builder().build()))
                            .build())
                    .build())
            .isPresent());
  }

  @Test
  void getStoreAgreementFirstTest() {
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreAgreementFirst(
                ZlSqsItem.builder().store(ZlSqsItem.Store.builder().build()).build())
            .isEmpty());
    Assertions.assertFalse(
        ZlSqsItemWrapper.getStoreAgreementFirst(
                ZlSqsItem.builder()
                    .store(ZlSqsItem.Store.builder().agreementList(List.of()).build())
                    .build())
            .isPresent());
    Assertions.assertTrue(
        ZlSqsItemWrapper.getStoreAgreementFirst(
                ZlSqsItem.builder()
                    .store(
                        ZlSqsItem.Store.builder()
                            .agreementList(List.of(ZlAgreement.builder().build()))
                            .build())
                    .build())
            .isPresent());
  }

  @Test
  void getStore3BucketNameTest() {
    Assertions.assertNull(
        ZlSqsItemWrapper.getStoreS3BucketName(
            ZlSqsItem.builder().store(ZlSqsItem.Store.builder().build()).build()));

    Assertions.assertEquals(
        "bucket-name",
        ZlSqsItemWrapper.getStoreS3BucketName(
            ZlSqsItem.builder()
                .store(ZlSqsItem.Store.builder().s3BucketName("bucket-name").build())
                .build()));
  }

  @Test
  void setStoreS3BucketNameTest() {
    final ZlSqsItem item = ZlSqsItem.builder().build();

    ZlSqsItemWrapper.setStoreS3BucketName(item, "bucket-name");

    Assertions.assertEquals("bucket-name", ZlSqsItemWrapper.getStoreS3BucketName(item));
  }

  @Test
  void web3TransactionManagerTest() {
    final ZlSqsItem item = ZlSqsItem.builder().build();
    Assertions.assertTrue(ZlSqsItemWrapper.getWeb3TransactionManager(item).isEmpty());
    final ZlWeb3.TransactionManager transactionManager =
        ZlSqsItemWrapper.ensureWeb3TransactionManager(item);
    Assertions.assertNotNull(transactionManager);
    Assertions.assertEquals(
        transactionManager, ZlSqsItemWrapper.getWeb3TransactionManager(item).orElseThrow());

    ZlSqsItemWrapper.deleteWeb3TransactionManager(item);
    Assertions.assertTrue(ZlSqsItemWrapper.getWeb3TransactionManager(item).isEmpty());
  }

  @Test
  void web3TransactionTest() {
    final String id = "id";
    final ZlWeb3Transaction transaction = ZlWeb3Transaction.builder().id(id).build();
    Assertions.assertNull(ZlSqsItemWrapper.addWeb3Transaction(null, transaction));

    final ZlSqsItem item = ZlSqsItem.builder().build();
    Assertions.assertEquals(transaction, ZlSqsItemWrapper.addWeb3Transaction(item, transaction));
    Assertions.assertEquals(transaction, ZlSqsItemWrapper.getWeb3Transaction(item).orElseThrow());

    Assertions.assertDoesNotThrow(() -> ZlSqsItemWrapper.deleteWeb3Transaction(item));
    Assertions.assertNull(ZlSqsItemWrapper.getWeb3Transaction(item).orElse(null));
  }

  @Test
  void addTransactionToWeb3TransactionManagerTest() {
    final ZlSqsItem item = ZlSqsItem.builder().build();
    final ZlWeb3.TransactionManager transactionManager =
        ZlSqsItemWrapper.ensureWeb3TransactionManager(item);

    final String txHash = "0x0";
    final ZlWeb3.TransactionManager.Transaction transaction =
        ZlSqsItemWrapper.addTransactionToWeb3TransactionManager(item, txHash);
    Assertions.assertEquals(txHash, transaction.getTxHash());
    Assertions.assertEquals(
        ZlWeb3.TransactionManager.Transaction.Status.INITIAL, transaction.getStatus());

    final String txHash1 = "0x1";
    final ZlWeb3.TransactionManager.Transaction transaction1 =
        ZlSqsItemWrapper.addTransactionToWeb3TransactionManager(item, txHash1);
    Assertions.assertEquals(txHash1, transaction1.getTxHash());
    Assertions.assertEquals(
        ZlWeb3.TransactionManager.Transaction.Status.INITIAL, transaction1.getStatus());
    Assertions.assertNotEquals(transaction, transaction1);

    final ZlWeb3.TransactionManager.Transaction transaction3 =
        ZlSqsItemWrapper.addTransactionToWeb3TransactionManager(item, txHash);
    Assertions.assertEquals(transaction, transaction3);
  }

  @Test
  void initTest() {
    Assertions.assertDoesNotThrow(ZlSqsItemWrapper::new);
  }
}
