package green.zerolabs.commons.core.model;

/*
 * Created by triphon 17.06.22 г.
 */
@SuppressWarnings("unused")
public class CoreConstants {
  public static final String CSV_ARRAY_SPLIT = "|";
  public static final String COMMA = ",";
  public static final String EMPTY = "";
  public static final String SLASH = "/";
  public static final String AGREEMENT_DISTRIBUTION_SPLIT = ":";

  public static final String SOME_SECRET_VALUE = "some_secret_value";

  public static final String QUERY = "Query";
  public static final String MUTATION = "Mutation";

  public static final String BODY = "body";
  public static final String DATA = "data";
  public static final String VERSION = "version";
  public static final String INVENTORY = "inventory";
  public static final String BENEFICIARY_ID = "beneficiaryId";

  public static final String SQS_SSM_PROCESSOR = "sqsSsmProcessor";
  public static final String SQS_S3_PROCESSOR = "sqsS3Processor";
  public static final String SQS_W3_PROCESSOR = "sqsW3Processor";
  public static final String SQS_DB_PROCESSOR = "sqsDbProcessor";
  public static final String SQS_DB_WITH_DUPLICATION_PROCESSOR = "sqsDbWithDuplicationProcessor";
  public static final String SQS_W3_RETRY_PROCESSOR = "sqsW3RetryProcessor";
  public static final String SQS_IPFS_RETRY_PROCESSOR = "sqsIpfsRetryProcessor";
}
