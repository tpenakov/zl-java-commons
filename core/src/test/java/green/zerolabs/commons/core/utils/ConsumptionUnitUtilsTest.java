package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit.*;
import static green.zerolabs.commons.core.utils.ConsumptionUnitUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/***
 * Created by Triphon Penakov 2022-12-07
 */
@Getter(AccessLevel.PACKAGE)
class ConsumptionUnitUtilsTest {

  ConsumptionUnitUtils utils = of();

  @Test
  void toConsumptionUnitTest() {
    Assertions.assertEquals(MWh, getUtils().toConsumptionUnit("MWh"));
  }

  @Test
  void prettyPrintTest() {
    Assertions.assertEquals("10 KWh", getUtils().prettyPrint(Integer.valueOf(10000), Wh));
    Assertions.assertEquals("10 MWh", getUtils().prettyPrint(Long.valueOf(10000000), Wh));
  }

  @Test
  void prettyPrint_Wh_OkTest() {
    Assertions.assertEquals("10 Wh", getUtils().prettyPrint(Integer.valueOf(10), Wh));
    Assertions.assertEquals("10 Wh", getUtils().prettyPrint(Long.valueOf(10), Wh));
    Assertions.assertEquals("10 Wh", getUtils().prettyPrint(Double.valueOf(10), Wh));
    Assertions.assertEquals("10 Wh", getUtils().prettyPrint(Float.valueOf(10), Wh));
    Assertions.assertEquals("10 Wh", getUtils().prettyPrint(BigDecimal.valueOf(10), Wh));
    Assertions.assertEquals("10 Wh", getUtils().prettyPrint("10", Wh));
  }

  @Test
  void prettyPrint_WhLessThanOne_OkTest() {
    Assertions.assertEquals("0.001 Wh", getUtils().prettyPrint(.001, Wh));
  }

  @Test
  void prettyPrint_KWh_OkTest() {
    Assertions.assertEquals("10 KWh", getUtils().prettyPrint(Integer.valueOf(10), KWh));
  }

  @Test
  void prettyPrint_MWh_OkTest() {
    Assertions.assertEquals("10 MWh", getUtils().prettyPrint(Integer.valueOf(10), MWh));
  }

  @Test
  void convert_NullArguments_ThrowTest() {
    checkArgumentsAndThrow(BigDecimal.ONE, Wh, MWh, null);
    checkArgumentsAndThrow(BigDecimal.ONE, Wh, null, null);
    checkArgumentsAndThrow(BigDecimal.ONE, null, null, null);
    checkArgumentsAndThrow(null, null, null, null);
  }

  @Test
  void convertTest() {
    Assertions.assertEquals(
        BigDecimal.TEN.setScale(DEFAULT_OUTPUT_SCALE, RoundingMode.HALF_UP),
        getUtils().convert(10000, KWh, MWh));
    Assertions.assertEquals(
        BigDecimal.TEN.setScale(DEFAULT_OUTPUT_SCALE, RoundingMode.HALF_UP),
        getUtils().convert(10000L, KWh, MWh));
    Assertions.assertEquals(
        BigDecimal.TEN.setScale(DEFAULT_OUTPUT_SCALE, RoundingMode.HALF_UP),
        getUtils().convert(Float.valueOf(10000L), KWh, MWh));
    Assertions.assertEquals(
        BigDecimal.TEN.setScale(DEFAULT_OUTPUT_SCALE, RoundingMode.HALF_UP),
        getUtils().convert(Double.valueOf(10000L), KWh, MWh));
    Assertions.assertEquals(
        BigDecimal.TEN.setScale(DEFAULT_OUTPUT_SCALE, RoundingMode.HALF_UP),
        getUtils().convert(BigDecimal.valueOf(.01), KWh, Wh));
  }

  void checkArgumentsAndThrow(
      final BigDecimal amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    final RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> getUtils().checkArguments(amount, inputUnit, outputUnit, scale));
    assertEquals(NULL_ARGUMENTS, exception.getMessage());
  }
}
