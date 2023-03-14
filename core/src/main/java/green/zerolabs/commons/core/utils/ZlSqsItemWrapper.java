package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.*;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/*
 * Created by triphon 18.08.22 г.
 */
public class ZlSqsItemWrapper {
  public static Optional<ZlSqsItem.Store> getStore(final ZlSqsItem item) {
    return Optional.ofNullable(item).map(ZlSqsItem::getStore);
  }

  public static ZlSqsItem.Store ensureStore(final ZlSqsItem item) {
    return getStore(item)
        .orElseGet(
            () -> {
              item.setStore(ZlSqsItem.Store.builder().build());
              return item.getStore();
            });
  }

  public static Optional<List<ZlBeneficiary>> getStoreBeneficiaryList(final ZlSqsItem item) {
    return getStore(item)
        .map(ZlSqsItem.Store::getBeneficiaryList)
        .filter(collection -> !CollectionUtils.isNullOrEmpty(collection));
  }

  public static Optional<List<ZlOrder>> getStoreOrderList(final ZlSqsItem item) {
    return getStore(item)
        .map(ZlSqsItem.Store::getOrderList)
        .filter(collection -> !CollectionUtils.isNullOrEmpty(collection));
  }

  public static Optional<List<ZlAgreement>> getStoreAgreementList(final ZlSqsItem item) {
    return getStore(item)
        .map(ZlSqsItem.Store::getAgreementList)
        .filter(collection -> !CollectionUtils.isNullOrEmpty(collection));
  }

  public static Optional<ZlAgreement> getStoreAgreementFirst(final ZlSqsItem item) {
    return getStore(item).map(ZlSqsItem.Store::getAgreementList).stream()
        .flatMap(Collection::stream)
        .findFirst();
  }

  public static String getStoreS3BucketName(final ZlSqsItem item) {
    return getStore(item).map(ZlSqsItem.Store::getS3BucketName).orElse(null);
  }

  public static void setStoreS3BucketName(final ZlSqsItem item, final String name) {
    ensureStore(item).setS3BucketName(name);
  }

  public static Optional<ZlWeb3> getZlWeb3(final ZlSqsItem item) {
    return Optional.ofNullable(item).map(ZlSqsItem::getWeb3);
  }

  public static ZlWeb3 ensureZlWeb3(final ZlSqsItem item) {
    return getZlWeb3(item)
        .orElseGet(
            () -> {
              item.setWeb3(ZlWeb3.builder().build());
              return item.getWeb3();
            });
  }

  public static Optional<ZlWeb3.TransactionManager> getWeb3TransactionManager(
      final ZlSqsItem item) {
    return getZlWeb3(item).map(ZlWeb3::getTransactionManager);
  }

  public static ZlWeb3.TransactionManager ensureWeb3TransactionManager(final ZlSqsItem item) {
    return getWeb3TransactionManager(item)
        .orElseGet(
            () -> {
              final ZlWeb3 zlWeb3 = ensureZlWeb3(item);
              zlWeb3.setTransactionManager(ZlWeb3.TransactionManager.builder().build());
              return zlWeb3.getTransactionManager();
            });
  }

  public static ZlWeb3.TransactionManager.Transaction addTransactionToWeb3TransactionManager(
      final ZlSqsItem item, final String txHash) {
    final ZlWeb3.TransactionManager transactionManager = ensureWeb3TransactionManager(item);
    return transactionManager.getTransactions().stream()
        .filter(transaction -> StringUtils.equals(txHash, transaction.getTxHash()))
        .findFirst()
        .orElseGet(
            () -> {
              final ZlWeb3.TransactionManager.Transaction transaction =
                  ZlWeb3.TransactionManager.Transaction.builder().txHash(txHash).build();
              transactionManager.getTransactions().add(0, transaction);
              return transaction;
            });
  }

  public static void deleteWeb3TransactionManager(final ZlSqsItem item) {
    getZlWeb3(item).ifPresent(zlWeb3 -> zlWeb3.setTransactionManager(null));
  }

  public static ZlWeb3Transaction addWeb3Transaction(
      final ZlSqsItem item, final ZlWeb3Transaction transaction) {
    if (Objects.isNull(item)) {
      return null;
    }

    ensureZlWeb3(item).setWeb3Transaction(transaction);
    return transaction;
  }

  public static Optional<ZlWeb3Transaction> getWeb3Transaction(final ZlSqsItem item) {
    return getZlWeb3(item).map(ZlWeb3::getWeb3Transaction);
  }

  public static void deleteWeb3Transaction(final ZlSqsItem item) {
    getZlWeb3(item).ifPresent(zlWeb3 -> zlWeb3.setWeb3Transaction(null));
  }
}

