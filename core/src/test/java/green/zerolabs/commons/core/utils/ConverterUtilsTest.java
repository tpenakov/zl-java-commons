package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.CoreConstants;
import green.zerolabs.commons.core.model.ZlCertificateData;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import io.smallrye.mutiny.tuples.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class ConverterUtilsTest {

  public static final String LONG_NUMBER_AS_STRING =
      "1234567890123456789012345678901234567890123456789012345678901234567890";
  private static final Integer INTEGER_NUMBER = 999999;
  private static final Long LONG_NUMBER = 999999L;

  final byte ZERO = 0;

  private ConverterUtils converterUtils;

  @BeforeEach
  void beforeEach() {
    converterUtils = new ConverterUtils();
  }

  @Test
  void testToString() {
    final BigInteger bigInteger = new BigInteger(LONG_NUMBER_AS_STRING);

    final String result = converterUtils.toString(bigInteger);
    Assertions.assertEquals(LONG_NUMBER_AS_STRING, result);
    Assertions.assertNotEquals(
        LONG_NUMBER_AS_STRING, converterUtils.toString(bigInteger.longValue()));
  }

  @Test
  void testIntegerToString() {
    final String result = converterUtils.toString(INTEGER_NUMBER);
    Assertions.assertEquals("999999", result);
  }

  @Test
  void uuidToBytes32Test() {
    final UUID uuid = UUID.randomUUID();
    final byte[] bytes32 = ConverterUtils.uuidToBytes32(uuid);
    Assertions.assertEquals(32, bytes32.length);
    for (int i = 0; i < 16; i++) {
      final byte b = bytes32[i];
      Assertions.assertEquals(ZERO, b);
    }
  }

  @Test
  void epochMilliToStringTest() {
    final String result = converterUtils.epochMilliToString(LONG_NUMBER);
    Assertions.assertEquals("1970-01-01T00:16:39.999Z", result);
  }

  @Test
  void epochStringToMilliTest() {
    final Long result = converterUtils.epochStringToMilli("1970-01-01T00:16:39.999Z");
    Assertions.assertEquals(LONG_NUMBER, result);
  }

  @Test
  void toEpochMilliTest() {
    final Long result = converterUtils.toEpochMilli("1970-01-01T00:16:39.999Z");
    Assertions.assertEquals(LONG_NUMBER, result);
  }

  @Test
  void toEpochMilliLongInputTest() {
    Assertions.assertEquals(LONG_NUMBER, converterUtils.toEpochMilli(String.valueOf(LONG_NUMBER)));
  }

  @Test
  void toEpochMilliInvalidInputTest() {
    Assertions.assertThrows(
        DateTimeParseException.class, () -> converterUtils.toEpochMilli("invalid input"));
  }

  @Test
  void numberToStringTest() {
    final String result = converterUtils.numberToString(LONG_NUMBER);
    Assertions.assertEquals("999999", result);
  }

  @Test
  void stringToLongTest() {
    final Long result = converterUtils.toLong("9999999999999999");
    Assertions.assertEquals(9999999999999999L, result);
  }

  @Test
  void intersectTest() {
    final List<String> result =
        converterUtils.intersect(List.of(List.of("1", "2"), List.of("2", "4")));
    Assertions.assertEquals(List.of("2"), result);
  }

  @Test
  void intersectWhenInputIsEmptyTest() {
    final List<String> result = converterUtils.intersect(List.of());
    Assertions.assertEquals(List.of(), result);
  }

  @Test
  void intersectWhenInputIsNullTest() {
    final List<String> result = converterUtils.intersect(null);
    Assertions.assertEquals(List.of(), result);
  }

  @Test
  void splitToListTest() {
    final List<String> result = ConverterUtils.splitToList("1,2,3", ",");
    Assertions.assertEquals(List.of("1", "2", "3"), result);
  }

  @Test
  void splitToListWhenInputIsEmptyTest() {
    final List<String> result = ConverterUtils.splitToList("", ",");
    Assertions.assertEquals(List.of(), result);
  }

  @Test
  void splitCsvArrayToListTest() {
    final List<String> result = ConverterUtils.splitCsvArrayToList("asd|asd1");
    Assertions.assertEquals(List.of("asd", "asd1"), result);
  }

  @Test
  void splitCsvArrayToListWhenWithoutDelimiterTest() {
    final List<String> result = ConverterUtils.splitCsvArrayToList("asd");
    Assertions.assertEquals(List.of("asd"), result);
  }

  @Test
  void splitCsvArrayToListWithCommaDelimiterTest() {
    final List<String> result = ConverterUtils.splitCsvArrayToList("asd,asd1");
    Assertions.assertEquals(List.of("asd", "asd1"), result);
  }

  @Test
  void splitListToStringDelimiterTest() {
    final String result =
        ConverterUtils.splitListToStringDelimiter(
            CoreConstants.CSV_ARRAY_SPLIT, List.of("element1", "element2", "element3"));
    Assertions.assertEquals("element1|element2|element3", result);
  }

  @Test
  void splitListToStringDelimiterNullTest() {
    final String result =
        ConverterUtils.splitListToStringDelimiter(CoreConstants.CSV_ARRAY_SPLIT, List.of());
    Assertions.assertNull(result);
  }

  @Test
  void splitCsvArrayToListWhenInputNullTest() {
    final List<String> result = ConverterUtils.splitCsvArrayToList(null);
    Assertions.assertEquals(List.of(), result);
  }

  @Test
  void splitCsvLocationToCountryRegionTest() {
    final Tuple2<Optional<String>, Optional<String>> result =
        ConverterUtils.splitCsvLocationToCountryRegion("country-region");
    Assertions.assertEquals(Tuple2.of(Optional.of("country"), Optional.of("region")), result);
  }

  @Test
  void splitCsvLocationToCountryRegionNoSplitTest() {
    final Tuple2<Optional<String>, Optional<String>> result =
        ConverterUtils.splitCsvLocationToCountryRegion("country");
    Assertions.assertEquals(Tuple2.of(Optional.of("country"), Optional.empty()), result);
  }

  @Test
  void splitCsvLocationToCountryRegionStartsWithSplitTest() {
    final Tuple2<Optional<String>, Optional<String>> result =
        ConverterUtils.splitCsvLocationToCountryRegion("-region");
    Assertions.assertEquals(Tuple2.of(Optional.empty(), Optional.of("region")), result);
  }

  @Test
  void splitCsvLocationToCountryRegionListTest() {
    final List<Tuple2<Optional<String>, Optional<String>>> result =
        ConverterUtils.splitCsvLocationToCountryRegionList("country-region|country1-region1");
    Assertions.assertEquals(
        List.of(
            Tuple2.of(Optional.of("country"), Optional.of("region")),
            Tuple2.of(Optional.of("country1"), Optional.of("region1"))),
        result);
  }

  @Test
  void splitToEnergyTypesTest() {
    final List<EnergyType> result = ConverterUtils.splitToEnergyTypes("WIND|SOLAR");
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(result.get(0), EnergyType.WIND);
    Assertions.assertEquals(result.get(1), EnergyType.SOLAR);
    Assertions.assertNotEquals(result.get(1), EnergyType.ANY);
  }

  @Test
  void toAgreementDistributionsTest() {
    final String input =
        "317d638a-7340-44eb-a171-b82f175b78dc:10:MWh,"
            + "00bd3b96-3bbe-4133-8abd-af46a1c88942:3000:KWh,"
            + "f487f316-2d3d-409f-b94f-1d2c039e32a2:4000:Wh";

    final List<ZlCertificateData.AgreementDistribution> agreementDistributions =
        ConverterUtils.toAgreementDistributions(input);
    Assertions.assertNotNull(agreementDistributions);
    Assertions.assertEquals(3, agreementDistributions.size());
    Assertions.assertEquals(
        List.of(
            ZlCertificateData.AgreementDistribution.builder()
                .agreementId("317d638a-7340-44eb-a171-b82f175b78dc")
                .energyAmount(BigDecimal.valueOf(10000000))
                .energyUnit(ConsumptionUnit.Wh)
                .build(),
            ZlCertificateData.AgreementDistribution.builder()
                .agreementId("00bd3b96-3bbe-4133-8abd-af46a1c88942")
                .energyAmount(BigDecimal.valueOf(3000000))
                .energyUnit(ConsumptionUnit.Wh)
                .build(),
            ZlCertificateData.AgreementDistribution.builder()
                .agreementId("f487f316-2d3d-409f-b94f-1d2c039e32a2")
                .energyAmount(BigDecimal.valueOf(4000))
                .energyUnit(ConsumptionUnit.Wh)
                .build()),
        agreementDistributions);
  }
}
