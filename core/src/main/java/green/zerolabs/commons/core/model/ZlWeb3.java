package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ProofRs;
import green.zerolabs.commons.core.model.graphql.generated.W3BatchRs;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * Created by triphon 25.08.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlWeb3 implements Serializable {
  private static final long serialVersionUID = -547260875013562978L;

  public enum State {
    INITIAL,
    CREATE_BATCHES,
    CERTIFICATE_REGISTRY,
    APPROVE_BENEFICIARY,
    AGREEMENT,
  }

  private State web3State;
  private ZlBlockchainProperties blockchainProperties;
  private Map<String, ZlBlockchainProperties> blockchainPropertiesByUser;
  private CreateBatches createBatches;
  private CertificateRegistry certificateRegistry;
  private ApproveBeneficiary approveBeneficiary;
  private Agreement agreement;
  private TransactionManager transactionManager;
  private ZlWeb3Transaction web3Transaction;

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class CreateBatches implements Serializable {
    private static final long serialVersionUID = 275142699584089772L;

    private ZlBatch w3Batch;
    private BigInteger gasLimit;
    private List<ZlCertificate> zlCertificates;
    private List<ZlCertificateData> zlCertificateDataList;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class CertificateRegistry implements Serializable {
    private static final long serialVersionUID = 8891332199647513409L;

    private String txHash;
    private W3BatchRs w3Batch;
    private BigInteger gasLimit;
    private ZlCertificate zlCertificate;
    private ProofRs proofRs;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class ApproveBeneficiary implements Serializable {
    private static final long serialVersionUID = 773973276942081373L;

    public enum Status {
      INITIAL,
      FUNDS_WILL_SEND,
      FUNDS_SENT,
      FUNDS_RECEIVED,
      APPROVE_REQUESTED,
      APPROVED
    }

    private String txHash;
    private String sendFundsTxHash;
    private String approveForAllTxHash;

    @Builder.Default private Status approveBeneficiaryW3Status = Status.INITIAL;
    private ZlBeneficiary beneficiary;
    private BigInteger gasLimit;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class Agreement implements Serializable {
    private static final long serialVersionUID = 3230278696851213921L;

    private String txHash;
    private ZlBeneficiary beneficiary;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class TransactionManager implements Serializable {
    private static final long serialVersionUID = 6557060951208234060L;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private BigInteger nonce;
    private Integer numAttempts;
    private Integer multiplyFactor;
    private String to;
    private String data;
    @Builder.Default private List<Transaction> transactions = new ArrayList<>();
    private BigInteger value;

    @Builder
    @Data
    @Jacksonized
    @RegisterForReflection
    public static class Transaction implements Serializable {
      private static final long serialVersionUID = 6472076689475954198L;

      public enum Status {
        INITIAL,
        MISSING,
        COMPLETED,
        ERROR
      }

      private String txHash;
      @Builder.Default private Status status = Status.INITIAL;
      private Object transactionReceipt;
      private Object error;
      private Throwable throwable;
      private String errorMessage;
    }
  }
}
