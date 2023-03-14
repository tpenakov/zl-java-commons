package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.ZlUserDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/***
 * Created by Triphon Penakov 2022-12-09
 */
class ZlUserDetailsWrapperTest {

  @Test
  void getForgeAgreementLogicNullTest() {
    assertEquals(
        ZlUserDetails.ForgeAgreementLogic.SEARCH_BY_ID_ONLY,
        ZlUserDetailsWrapper.getForgeAgreementLogic(null));
  }

  @Test
  void getForgeAgreementLogicTest() {
    assertEquals(
        ZlUserDetails.ForgeAgreementLogic.FULL_DETAILS_SEARCH,
        ZlUserDetailsWrapper.getForgeAgreementLogic(
            ZlUserDetails.builder()
                .forgeAgreementLogic(ZlUserDetails.ForgeAgreementLogic.FULL_DETAILS_SEARCH)
                .build()));
  }

  @Test
  void initTest() {
    Assertions.assertNotNull(new ZlUserDetailsWrapper());
  }
}
