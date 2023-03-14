package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlCountryRegion;
import green.zerolabs.commons.core.model.ZlOffer;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRs;
import green.zerolabs.commons.core.model.graphql.generated.OfferRs;
import green.zerolabs.commons.core.utils.DateConverter;
import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Uni;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class OfferRsConverter extends GenericInternalDbToDtoConverter<ZlOffer, OfferRs> {

  private final DateConverter dateConverter;

  public OfferRsConverter(final DeepClone cloneable, final DateConverter dateConverter) {
    super(cloneable, ZlOffer.class, OfferRs.class);
    this.dateConverter = dateConverter;
  }

  @Override
  public Uni<ZlOffer> convertToDb(final OfferRs item, final ZlOffer previous) {
    return Uni.createFrom().failure(UnsupportedOperationException::new);
  }

  @Override
  public Uni<OfferRs> convertToDto(final ZlOffer item) {
    return transformToResponse(item);
  }

  public Uni<OfferRs> transformToResponse(final ZlOffer zlOffer) {
    final OfferRs offerRs = new OfferRs();

    offerRs.setId(zlOffer.getId());
    offerRs.setUnitPrice(zlOffer.getUnitPrice());
    offerRs.setImage(zlOffer.getImage());
    offerRs.setDisabled(zlOffer.getDisabled());
    offerRs.setDescription(zlOffer.getDescription());
    offerRs.setEnergyUnit(zlOffer.getEnergyUnit());

    offerRs.setInventoryId(zlOffer.getIndexedData().getInventoryId());
    offerRs.setProductType(zlOffer.getIndexedData().getProductType());
    offerRs.setReportingStart(
        dateConverter.epochMilliToString(zlOffer.getIndexedData().getReportingStart()));
    offerRs.setReportingEnd(
        dateConverter.epochMilliToString(zlOffer.getIndexedData().getReportingEnd()));
    offerRs.setEnergyTypes(zlOffer.getIndexedData().getEnergyTypes());
    offerRs.setCountryRegions(transformToRs(zlOffer.getIndexedData().getCountryRegions()));

    return Uni.createFrom().item(offerRs);
  }

  private List<ConsumptionLocationRs> transformToRs(final List<ZlCountryRegion> countryRegions) {
    if (Objects.isNull(countryRegions)) {
      return Collections.emptyList();
    }

    return countryRegions.stream()
        .map(
            zl -> {
              final ConsumptionLocationRs rs = new ConsumptionLocationRs();
              rs.setCountry(zl.getCountry());
              rs.setRegion(zl.getRegionName());
              return rs;
            })
        .collect(toList());
  }
}
