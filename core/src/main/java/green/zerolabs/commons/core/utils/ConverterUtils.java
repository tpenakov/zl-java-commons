package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.CoreConstants;
import green.zerolabs.commons.core.model.ZlCertificateData;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import io.smallrye.mutiny.tuples.Tuple2;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static green.zerolabs.commons.core.model.CoreConstants.CSV_ARRAY_SPLIT;
import static green.zerolabs.commons.core.service.ZlCertificateJsonService.COUNTRY_REGION_SPLIT;

/*
 * Created by triphon 10.04.22 г.
 */
public class ConverterUtils implements DateConverter {

  public static final String NUMBER_FORMAT = "{0,number,#}";

  private static final ConsumptionUnitUtils consumptionUnitUtils = ConsumptionUnitUtils.of();

  public static List<EnergyType> splitToEnergyTypes(final String value) {
    return splitCsvArrayToList(value).stream()
        .map(EnergyType::valueOf)
        .collect(Collectors.toList());
  }

  public String toString(final Integer num) {
    return Optional.ofNullable(num)
        .map(number -> MessageFormat.format(NUMBER_FORMAT, number))
        .orElse(null);
  }

  public String toString(final Long num) {
    return Optional.ofNullable(num)
        .map(number -> MessageFormat.format(NUMBER_FORMAT, number))
        .orElse(null);
  }

  public String toString(final BigInteger num) {
    return Optional.ofNullable(num)
        .map(number -> MessageFormat.format(NUMBER_FORMAT, number))
        .orElse(null);
  }

  public Long toLong(final String number) {
    return Optional.ofNullable(number).map(Long::parseLong).orElse(null);
  }

  @Override
  public String epochMilliToString(final Long millis) {
    return Instant.ofEpochMilli(millis).toString();
  }

  @Override
  public Long epochStringToMilli(final String instant) {
    return Instant.parse(instant).toEpochMilli();
  }

  @Override
  public Long toEpochMilli(final String instant) {
    try {
      return Instant.parse(instant).toEpochMilli();
    } catch (final DateTimeParseException e) {
      try {
        return Long.valueOf(instant);
      } catch (final NumberFormatException ex) {
        throw e;
      }
    }
  }

  public String numberToString(final Long number) {
    return MessageFormat.format(NUMBER_FORMAT, number);
  }

  public List<String> intersect(final List<List<String>> lists) {
    if (Objects.isNull(lists)) {
      return List.of();
    }

    return lists.stream()
        .reduce(
            (strings, strings2) ->
                strings.stream().distinct().filter(strings2::contains).collect(Collectors.toList()))
        .orElseGet(List::of);
  }

  public static List<String> splitToList(final String input, final String delimiter) {
    return StringUtils.isNotBlank(input)
        ? Arrays.asList(
            green.zerolabs.commons.core.utils.apache.commons.StringUtils.split(input, delimiter))
        : List.of();
  }

  public static List<String> splitCsvArrayToList(final String input) {
    return Optional.ofNullable(input)
        .filter(StringUtils::isNotBlank)
        .map(
            s ->
                s.contains(CSV_ARRAY_SPLIT)
                    ? splitToList(input, CSV_ARRAY_SPLIT)
                    : splitToList(input, CoreConstants.COMMA))
        .orElseGet(List::of);
  }

  public static String splitListToStringDelimiter(final String delimiter, final List<String> list) {
    return CollectionUtils.isNullOrEmpty(list) ? null : String.join(delimiter, list);
  }

  public static Tuple2<Optional<String>, Optional<String>> splitCsvLocationToCountryRegion(
      final String input) {
    final List<String> countryRegions = splitToList(input, COUNTRY_REGION_SPLIT);
    if (countryRegions.size() == 1 && input.startsWith(COUNTRY_REGION_SPLIT)) {
      return Tuple2.of(Optional.empty(), Optional.of(countryRegions.get(0)));
    }

    return Tuple2.of(
        countryRegions.stream().limit(1L).findAny(), countryRegions.stream().skip(1).findAny());
  }

  public static List<Tuple2<Optional<String>, Optional<String>>>
      splitCsvLocationToCountryRegionList(final String input) {
    return splitCsvArrayToList(input).stream()
        .map(ConverterUtils::splitCsvLocationToCountryRegion)
        .collect(Collectors.toList());
  }

  public static byte[] uuidToBytes32(final UUID uuid) {
    final ByteBuffer bb = ByteBuffer.wrap(new byte[32]);

    bb.putLong(0L);
    bb.putLong(0L);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());

    return bb.array();
  }

  public static List<ZlCertificateData.AgreementDistribution> toAgreementDistributions(
      final String agreementDistributionsCsv) {
    return splitCsvArrayToList(agreementDistributionsCsv).stream()
        .filter(StringUtils::isNotBlank)
        .map(
            agreementDistributionCsv ->
                green.zerolabs.commons.core.utils.apache.commons.StringUtils.split(
                    agreementDistributionCsv, CoreConstants.AGREEMENT_DISTRIBUTION_SPLIT))
        .map(
            data ->
                ZlCertificateData.AgreementDistribution.builder()
                    .agreementId(data[0])
                    .energyAmount(new BigDecimal(data[1]))
                    .energyUnit(consumptionUnitUtils.toConsumptionUnit(data[2]))
                    .build())
        .map(
            agreementDistribution ->
                ZlCertificateData.AgreementDistribution.builder()
                    .agreementId(agreementDistribution.getAgreementId())
                    .energyAmount(
                        consumptionUnitUtils.convert(
                            agreementDistribution.getEnergyAmount(),
                            agreementDistribution.getEnergyUnit(),
                            ConsumptionUnit.Wh,
                            0))
                    .energyUnit(ConsumptionUnit.Wh)
                    .build())
        .collect(Collectors.toList());
  }
}
