package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlCountryRegion;
import green.zerolabs.commons.core.model.ZlOffer;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRq;
import green.zerolabs.commons.core.model.graphql.generated.OfferSaveRq;
import green.zerolabs.commons.core.utils.CountryRegionUtils;
import green.zerolabs.commons.core.utils.DateConverter;
import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class OfferSaveRqConverter extends GenericInternalDbToDtoConverter<ZlOffer, OfferSaveRq> {

  private final DateConverter dateConverter;

  public OfferSaveRqConverter(final DeepClone cloneable, final DateConverter dateConverter) {
    super(cloneable, ZlOffer.class, OfferSaveRq.class);
    this.dateConverter = dateConverter;
  }

  @Override
  public Uni<ZlOffer> convertToDb(final OfferSaveRq item, final ZlOffer previous) {
    return transformToEntity(item);
  }

  @Override
  public Uni<OfferSaveRq> convertToDto(final ZlOffer item) {
    return Uni.createFrom().failure(UnsupportedOperationException::new);
  }

  private Uni<ZlOffer> transformToEntity(final OfferSaveRq offerRq) {
    final ZlOffer.IndexedOfferData indexedOfferData =
        ZlOffer.IndexedOfferData.builder()
            .inventoryId(offerRq.getInventoryId())
            .productType(offerRq.getProductType())
            .reportingStart(dateConverter.toEpochMilli(offerRq.getReportingStart()))
            .reportingEnd(dateConverter.toEpochMilli(offerRq.getReportingEnd()))
            .countryRegions(transformToZl(offerRq.getCountryRegions()))
            .countryRegionIndexes(transformToCountryRegionIndexes(offerRq.getCountryRegions()))
            .build();

    final ZlOffer offer =
        ZlOffer.builder()
            .id(offerRq.getId())
            .unitPrice(offerRq.getUnitPrice())
            .energyUnit(offerRq.getEnergyUnit())
            .image(offerRq.getImage())
            .description(offerRq.getDescription())
            .disabled(false)
            .indexedData(indexedOfferData)
            .build();

    return Uni.createFrom().item(offer);
  }

  private List<String> transformToCountryRegionIndexes(
      final List<ConsumptionLocationRq> countryRegions) {
    if (CollectionUtils.isNullOrEmpty(countryRegions)) {
      return Collections.emptyList();
    }

    return countryRegions.stream()
        .map(CountryRegionUtils::toCountryRegionIndex)
        .filter(StringUtils::isNotBlank)
        .collect(toList());
  }

  private List<ZlCountryRegion> transformToZl(final List<ConsumptionLocationRq> countryRegionsRq) {
    if (Objects.isNull(countryRegionsRq)) {
      return Collections.emptyList();
    }

    return countryRegionsRq.stream()
        .map(
            rq ->
                ZlCountryRegion.builder()
                    .country(rq.getCountry())
                    .regionName(rq.getRegion())
                    .build())
        .collect(toList());
  }
}
