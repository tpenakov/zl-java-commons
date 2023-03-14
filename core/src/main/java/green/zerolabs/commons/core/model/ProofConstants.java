package green.zerolabs.commons.core.model;

/***
 * Created by Triphon Penakov 2023-02-16
 */
public class ProofConstants {
  public static final String GET_PROOF_BY_ID = "getProofById";
  public static final String GET_PROOF_TRANSACTIONS = "getProofTransactions";
  public static final String GET_PROOF_LIST = "getProofList";
  public static final String GET_PROOF_LIST_BY_ROOT_CERTIFICATE = "getProofListByRootCertificate";
  public static final String ADD_PROOF = "addProof";

  static final String PREFIX = "proof-";
  static final String DATA = PREFIX + "data";
  static final String VERSION = PREFIX + "version";
  static final Long CURRENT_PROOF_VERSION = 2L;
}
