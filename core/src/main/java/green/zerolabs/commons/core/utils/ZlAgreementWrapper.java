package green.zerolabs.commons.core.utils;

import green.zerolabs.commons.core.model.CoreConstants;
import green.zerolabs.commons.core.model.GraphQlRequest;
import green.zerolabs.commons.core.model.ZlAgreement;
import green.zerolabs.commons.core.model.ZlOrder;
import green.zerolabs.commons.core.model.graphql.generated.*;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static green.zerolabs.commons.core.model.ProofConstants.ADD_PROOF;

/*
 * Created by Triphon Penakov 2022-09-02
 */
public class ZlAgreementWrapper {

  public static void fillAgreement(final ZlAgreement zlAgreement, final ZlOrder order) {
    zlAgreement.setOrderId(order.getId());
    zlAgreement.setProductType(
        Optional.ofNullable(order.getProductType())
            .filter(StringUtils::isNotBlank)
            .map(W3ProductType::valueOf)
            .orElse(null));
    zlAgreement.setEnergySources(order.getEnergySources());
    zlAgreement.setCountryRegionList(order.getCountryRegionList());
    zlAgreement.setOrderDate(order.getDeliveryDate());
    zlAgreement.setReportingStart(order.getReportingStart());
    zlAgreement.setReportingEnd(order.getReportingEnd());
    zlAgreement.setSellerId(order.getSellerId());
    zlAgreement.setSupplierId(order.getSupplierId());
    zlAgreement.setLabel(order.getLabel());
  }

  public static void fillProofRq(final ProofRq proofRq, final ZlAgreement agreement) {
    final ConsumptionPeriodRq consumptionPeriodRq = new ConsumptionPeriodRq();
    consumptionPeriodRq.setStartDate(agreement.getReportingStart().toString());
    consumptionPeriodRq.setEndDate(agreement.getReportingEnd().toString());
    proofRq.setConsumptionPeriod(consumptionPeriodRq);
    proofRq.setConsumptionAmount(agreement.getEnergyAmount().longValue());
    proofRq.setConsumptionUnit(
        Optional.ofNullable(agreement.getEnergyUnit())
            .filter(StringUtils::isNotBlank)
            .map(ConsumptionUnit::valueOf)
            .orElse(ConsumptionUnit.Wh));

    proofRq.setProductTypes(
        Optional.ofNullable(agreement.getProductType()).map(List::of).orElse(null));
    proofRq.setEnergyTypes(
        ConverterUtils.splitCsvArrayToList(agreement.getEnergySources()).stream()
            .map(EnergyType::valueOf)
            .collect(Collectors.toList()));
    proofRq.setConsumptionLocations(toConsumptionLocations(agreement));
    proofRq.setBeneficiaryId(agreement.getBeneficiaryId());
    proofRq.setConsumptionEntityId(agreement.getConsumptionEntityId());
    proofRq.setMetadata(agreement.getMetadata());
  }

  public static List<ConsumptionLocationRq> toConsumptionLocations(final ZlAgreement agreement) {
    final String countryRegionList = agreement.getCountryRegionList();
    if (StringUtils.isBlank(countryRegionList)) {
      return List.of();
    }
    return ConverterUtils.splitCsvLocationToCountryRegionList(countryRegionList).stream()
        .map(
            objects -> {
              final String country = objects.getItem1().orElse(null);
              if (StringUtils.isBlank(country)) {
                return null;
              }
              final ConsumptionLocationRq rq = new ConsumptionLocationRq();
              rq.setCountry(country);
              rq.setRegion(objects.getItem2().orElse(null));
              return rq;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public static void ensureError(final ZlAgreement agreement, final String error) {
    agreement.setAgreementStatus(ZlAgreement.Status.ERROR);
    ensureErrors(agreement).add(error);
  }

  public static List<String> ensureErrors(final ZlAgreement agreement) {
    if (agreement.getErrors() == null) {
      agreement.setErrors(new ArrayList<>());
      return ensureErrors(agreement);
    }

    return agreement.getErrors();
  }

  public static boolean isError(final ZlAgreement agreement) {
    return Optional.ofNullable(agreement)
        .map(ZlAgreement::getAgreementStatus)
        .map(status -> status.equals(ZlAgreement.Status.ERROR))
        .orElse(false);
  }

  public static GraphQlRequest toAddProofGraphQlRequest(
          final ZlAgreement agreement, final JsonUtils jsonUtils) {
    final ProofRq proofRq = new ProofRq();
    proofRq.setAgreementId(agreement.getId());
    return GraphQlRequest.builder()
        .typeName(CoreConstants.MUTATION)
        .fieldName(ADD_PROOF)
        .identity(GraphQlRequest.Identity.builder().username(agreement.getUserId()).build())
        .arguments(Map.of(CoreConstants.BODY, jsonUtils.toMap(proofRq)))
        .build();
  }
}
