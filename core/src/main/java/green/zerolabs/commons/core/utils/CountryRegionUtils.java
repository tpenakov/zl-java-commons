package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRq;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRs;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CountryRegionUtils {

  public static String toCountryRegionIndex(final ConsumptionLocationRq consumptionLocation) {
    return toCountryRegionIndex(consumptionLocation.getCountry(), consumptionLocation.getRegion());
  }

  public static String toCountryRegionIndex(final ConsumptionLocationRs consumptionLocation) {
    return toCountryRegionIndex(consumptionLocation.getCountry(), consumptionLocation.getRegion());
  }

  public static String toCountryRegionIndex(final String... values) {
    final String result =
        Arrays.stream(values).filter(StringUtils::isNotBlank).collect(Collectors.joining("-"));
    return StringUtils.isNotBlank(result) ? result : null;
  }
}
