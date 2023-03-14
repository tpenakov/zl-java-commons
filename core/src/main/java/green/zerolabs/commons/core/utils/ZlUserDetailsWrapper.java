package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.ZlUserDetails;

import java.util.Optional;

/***
 * Created by Triphon Penakov 2022-12-09
 */
public class ZlUserDetailsWrapper {
  public static ZlUserDetails.ForgeAgreementLogic getForgeAgreementLogic(
          final ZlUserDetails userDetails) {
    return Optional.ofNullable(userDetails)
        .map(ZlUserDetails::getForgeAgreementLogic)
        .orElse(ZlUserDetails.ForgeAgreementLogic.SEARCH_BY_ID_ONLY);
  }
}
