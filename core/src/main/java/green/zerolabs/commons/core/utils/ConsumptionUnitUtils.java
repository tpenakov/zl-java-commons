package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit.*;
import static java.math.RoundingMode.HALF_UP;

/***
 * Created by Triphon Penakov 2022-12-07
 */
@NoArgsConstructor(staticName = "of")
@Getter(AccessLevel.PROTECTED)
public class ConsumptionUnitUtils {

  public static final int SCALE = 10;
  private static final Map<ConsumptionUnit, BigDecimal> UNIT_QUANTITY_MAP =
      Map.of(
          Wh, BigDecimal.valueOf(1).setScale(SCALE, HALF_UP),
          KWh, BigDecimal.valueOf(1000).setScale(SCALE, HALF_UP),
          MWh, BigDecimal.valueOf(1000000).setScale(SCALE, HALF_UP));
  public static final String NULL_ARGUMENTS = "Null Argument(s)";
  public static final int DEFAULT_OUTPUT_SCALE = 3;

  public ConsumptionUnit toConsumptionUnit(final String value) {
    return ConsumptionUnit.valueOf(value);
  }

  public String prettyPrint(final Integer amount, final ConsumptionUnit inputUnit) {
    return prettyPrint(() -> BigDecimal.valueOf(amount), inputUnit);
  }

  public String prettyPrint(final Long amount, final ConsumptionUnit inputUnit) {
    return prettyPrint(() -> BigDecimal.valueOf(amount), inputUnit);
  }

  public String prettyPrint(final Double amount, final ConsumptionUnit inputUnit) {
    return prettyPrint(() -> BigDecimal.valueOf(amount), inputUnit);
  }

  public String prettyPrint(final Float amount, final ConsumptionUnit inputUnit) {
    return prettyPrint(() -> BigDecimal.valueOf(amount), inputUnit);
  }

  public String prettyPrint(final String amount, final ConsumptionUnit inputUnit) {
    return prettyPrint(() -> new BigDecimal(amount), inputUnit);
  }

  public String prettyPrint(final BigDecimal amount, final ConsumptionUnit inputUnit) {
    return prettyPrint(() -> amount, inputUnit);
  }

  String prettyPrint(final Supplier<BigDecimal> amountFn, final ConsumptionUnit inputUnit) {
    final BigDecimal amount = amountFn.get();
    final Tuple2<BigDecimal, ConsumptionUnit> converted =
        Arrays.stream(values())
            .map(
                unit -> {
                  final BigDecimal result = convert(amount, inputUnit, unit, 0);
                  if (BigDecimal.ONE.compareTo(result) > 0) {
                    return null;
                  }
                  return Tuple2.of(result, unit);
                })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> Tuple2.of(convert(amount, inputUnit, Wh), Wh));
    return MessageFormat.format("{0} {1}", converted.getItem1(), converted.getItem2().name());
  }

  public BigDecimal convert(
      final Integer amount, final ConsumptionUnit inputUnit, final ConsumptionUnit outputUnit) {
    return convert(amount, inputUnit, outputUnit, DEFAULT_OUTPUT_SCALE);
  }

  public BigDecimal convert(
      final Integer amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    return convert(() -> BigDecimal.valueOf(amount), inputUnit, outputUnit, scale);
  }

  public BigDecimal convert(
      final Long amount, final ConsumptionUnit inputUnit, final ConsumptionUnit outputUnit) {
    return convert(amount, inputUnit, outputUnit, DEFAULT_OUTPUT_SCALE);
  }

  public BigDecimal convert(
      final Long amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    return convert(() -> BigDecimal.valueOf(amount), inputUnit, outputUnit, scale);
  }

  public BigDecimal convert(
      final Double amount, final ConsumptionUnit inputUnit, final ConsumptionUnit outputUnit) {
    return convert(amount, inputUnit, outputUnit, DEFAULT_OUTPUT_SCALE);
  }

  public BigDecimal convert(
      final Double amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    return convert(() -> BigDecimal.valueOf(amount), inputUnit, outputUnit, scale);
  }

  public BigDecimal convert(
      final Float amount, final ConsumptionUnit inputUnit, final ConsumptionUnit outputUnit) {
    return convert(amount, inputUnit, outputUnit, DEFAULT_OUTPUT_SCALE);
  }

  public BigDecimal convert(
      final Float amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    return convert(() -> BigDecimal.valueOf(amount), inputUnit, outputUnit, scale);
  }

  public BigDecimal convert(
      final BigDecimal amount, final ConsumptionUnit inputUnit, final ConsumptionUnit outputUnit) {
    return convert(amount, inputUnit, outputUnit, DEFAULT_OUTPUT_SCALE);
  }

  public BigDecimal convert(
      final BigDecimal amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    return convert(() -> amount, inputUnit, outputUnit, scale);
  }

  BigDecimal convert(
      final Supplier<BigDecimal> amountFn,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    final BigDecimal amount = amountFn.get();
    checkArguments(amount, inputUnit, outputUnit, scale);
    return amount
        .setScale(SCALE, HALF_UP)
        .multiply(UNIT_QUANTITY_MAP.get(inputUnit))
        .divide(UNIT_QUANTITY_MAP.get(outputUnit), HALF_UP)
        .setScale(scale, HALF_UP);
  }

  void checkArguments(
      final BigDecimal amount,
      final ConsumptionUnit inputUnit,
      final ConsumptionUnit outputUnit,
      final Integer scale) {
    if (Objects.isNull(amount)
        || Objects.isNull(inputUnit)
        || Objects.isNull(outputUnit)
        || Objects.isNull(scale)) {
      throw new RuntimeException(NULL_ARGUMENTS);
    }
  }
}
