package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.converter.InstantDeserializer;
import green.zerolabs.commons.core.model.graphql.generated.*;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ProofUtils {
  public static final String PRODUCT_TYPES = "productTypes";
  public static final String ENERGY_SOURCES = "energySources";
  public static final String ENERGY_TYPES = "energyTypes";
  public static final String COUNTRY_REGIONS = "countryRegionList";

  public static final String START_DATE = "startDate";
  public static final String END_DATE = "endDate";

  public static ProofDetailsRs toProofDetailsRs(
          final ProofRq rq, final Optional<String> userName, final ConverterUtils converterUtils) {
    final ProofDetailsRs details = new ProofDetailsRs();

    final String consumptionAmount = converterUtils.toString(rq.getConsumptionAmount());

    details.setUserDetails(new ProofUserDetails());
    details.getUserDetails().setId(userName.orElse(null));
    details.setConsumptionAmount(consumptionAmount);
    details.setConsumptionUnit(rq.getConsumptionUnit());
    details.setConsumptionEntityId(rq.getConsumptionEntityId());
    details.setProductTypes(rq.getProductTypes());
    details.setEnergyTypes(rq.getEnergyTypes());
    details.setMetadata(rq.getMetadata());
    details.setCertificateDataId(rq.getCertificateDataId());
    Optional.ofNullable(rq.getConsumptionPeriod())
        .ifPresent(
            consumptionPeriodRq -> {
              details.setStartDate(consumptionPeriodRq.getStartDate());
              details.setEndDate(consumptionPeriodRq.getEndDate());
            });

    final List<ConsumptionLocationRs> consumptionLocationRsList =
        Optional.ofNullable(rq.getConsumptionLocations()).stream()
            .flatMap(Collection::stream)
            .map(
                locationRq -> {
                  final String country = locationRq.getCountry();
                  if (StringUtils.isBlank(country)) {
                    return null;
                  }
                  final ConsumptionLocationRs consumptionLocationRs =
                      addConsumptionLocation(details);
                  consumptionLocationRs.setCountry(country);
                  final String region = locationRq.getRegion();
                  if (StringUtils.isNotBlank(region)) {
                    consumptionLocationRs.setRegion(region);
                  }
                  return consumptionLocationRs;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (!CollectionUtils.isNullOrEmpty(consumptionLocationRsList)) {
      details.setConsumptionLocations(consumptionLocationRsList);
    }
    return details;
  }

  public static ConsumptionLocationRs addConsumptionLocation(final ProofDetailsRs details) {
    final List<ConsumptionLocationRs> consumptionLocations = ensureConsumptionLocations(details);
    final ConsumptionLocationRs result = new ConsumptionLocationRs();
    consumptionLocations.add(result);
    return result;
  }

  public static List<ConsumptionLocationRs> ensureConsumptionLocations(
      final ProofDetailsRs details) {
    return Optional.ofNullable(details.getConsumptionLocations())
        .orElseGet(
            () -> {
              details.setConsumptionLocations(new ArrayList<>());
              return details.getConsumptionLocations();
            });
  }

  public static void fixProofRs(final ProofRs proofRs) {
    Optional.ofNullable(proofRs).map(ProofRs::getRootCertificates).stream()
        .flatMap(Collection::stream)
        .forEach(
            certificateRs -> {
              Optional.ofNullable(certificateRs.getCertificateData())
                  .filter(
                      certificateDataRs ->
                          StringUtils.isNotBlank(certificateDataRs.getRedemptionDate()))
                  .ifPresent(
                      certificateDataRs ->
                          certificateDataRs.setRedemptionDate(
                              InstantDeserializer.toInstant(certificateDataRs.getRedemptionDate())
                                  .toString()));
            });
  }
}
