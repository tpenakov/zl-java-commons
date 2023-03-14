package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRq;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CountryRegionUtilsTest {

  @Test
  void toCountryRegionIndexTest() {
    final ConsumptionLocationRq locationRq = new ConsumptionLocationRq();
    locationRq.setCountry("country");
    locationRq.setRegion("region");

    final String result = CountryRegionUtils.toCountryRegionIndex(locationRq);

    Assertions.assertEquals("country-region", result);
  }

  @Test
  void toCountryRegionIndexRsTest() {
    final String result = CountryRegionUtils.toCountryRegionIndex(new ConsumptionLocationRs());

    Assertions.assertNull(result);
  }

  @Test
  void newUtilsTest() {
    Assertions.assertNotNull(new CountryRegionUtils());
  }
}
