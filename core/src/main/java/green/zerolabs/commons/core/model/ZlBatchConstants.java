package green.zerolabs.commons.core.model;

/***
 * Created by Triphon Penakov 2023-02-16
 */
public class ZlBatchConstants {
  public static final String CREATE_W3BATCH = "createW3Batch";
  public static final String GET_W3BATCH = "getW3Batch";
  public static final String GET_W3BATCH_LIST = "getW3BatchList";
  public static final String ADD_W3BATCH = "storeW3Batch";
  public static final String UPDATE_W3BATCH = "updateW3Batch";
  static final String W3BATCH_PREFIX = "w3Batch-";
  static final String W3BATCH_DATA = W3BATCH_PREFIX + "data";
  static final String W3BATCH_VERSION = W3BATCH_PREFIX + "version";
  static final Long CURRENT_W3BATCH_VERSION = 1L;
}
