package green.zerolabs.commons.web3.j.utils;

import org.jetbrains.annotations.NotNull;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/***
 * Created by Triphon Penakov 2023-01-26
 */
public class TransactionReceiptUtils {
  public static final String DUMMY_TRANSACTION_HASH = "0x1";

  @NotNull
  public static TransactionReceipt createDummyTransactionReceipt() {
    final TransactionReceipt transactionReceipt = new TransactionReceipt();
    transactionReceipt.setTransactionHash(DUMMY_TRANSACTION_HASH);
    transactionReceipt.setTransactionIndex(DUMMY_TRANSACTION_HASH);
    transactionReceipt.setBlockNumber(DUMMY_TRANSACTION_HASH);
    transactionReceipt.setCumulativeGasUsed(DUMMY_TRANSACTION_HASH);
    transactionReceipt.setGasUsed(DUMMY_TRANSACTION_HASH);
    return transactionReceipt;
  }
}
