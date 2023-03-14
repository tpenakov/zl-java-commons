package green.zerolabs.commons.core.model;

import static green.zerolabs.commons.core.model.CoreModelConstants.DATA;

/***
 * Created by Triphon Penakov 2023-02-16
 */
@SuppressWarnings({"unused", "raw"})
public class ZlAgreementConstants {

  public static final String W3_HASH_MISSING =
      CoreModelConstants.getPrefix(ZlAgreement.class) + "w3-hash-missing";
  public static final String GRAPHQL_REQUEST_GET_AGREEMENT_LIST = "getAgreementList";

  static final String AGREEMENT_PREFIX = "agreement-";
  static final String AGREEMENT_DATA = AGREEMENT_PREFIX + DATA;
  static final String AGREEMENT_VERSION = AGREEMENT_PREFIX + "version";
  static final Long AGREEMENT_CURRENT_VERSION = 2L;
}
