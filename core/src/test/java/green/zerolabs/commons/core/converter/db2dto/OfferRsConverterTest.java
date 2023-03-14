package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.converter.InstantDeserializer;
import green.zerolabs.commons.core.model.ZlCountryRegion;
import green.zerolabs.commons.core.model.ZlOffer;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import green.zerolabs.commons.core.model.graphql.generated.OfferRs;
import green.zerolabs.commons.core.model.graphql.generated.W3ProductType;
import green.zerolabs.commons.core.utils.UnitTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public class OfferRsConverterTest {
  private static final String REPORTING_START_DATE = "2021-03-01T08:07:29Z";
  private static final String REPORTING_END_DATE = "2025-03-01T08:07:29Z";

  private OfferRsConverter converter;

  @BeforeEach
  void setUp() {
    final UnitTestUtils testUtils = UnitTestUtils.of();
    converter = new OfferRsConverter(testUtils.getJsonUtils(), testUtils.getConverterUtils());
  }

  @Test
  void convertToDbTest() {
    Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> converter.convertToDb(null, null).await().atMost(Duration.ofMillis(100)));
  }

  @Test
  void convertToDtoTest() {
    final ZlOffer zlOffer = generateZlOffer();

    final OfferRs offerRs =
        converter.convertToDto(generateZlOffer()).await().atMost(Duration.ofMillis(100));

    Assertions.assertNotNull(offerRs);
    Assertions.assertEquals(zlOffer.getId(), offerRs.getId());
    Assertions.assertEquals(zlOffer.getImage(), offerRs.getImage());
    Assertions.assertEquals(zlOffer.getDescription(), offerRs.getDescription());
    Assertions.assertEquals(zlOffer.getEnergyUnit(), offerRs.getEnergyUnit());
    Assertions.assertEquals(zlOffer.getUnitPrice(), offerRs.getUnitPrice());
    Assertions.assertEquals(zlOffer.getIndexedData().getInventoryId(), offerRs.getInventoryId());
    Assertions.assertEquals(zlOffer.getIndexedData().getEnergyTypes(), offerRs.getEnergyTypes());
    Assertions.assertEquals(zlOffer.getIndexedData().getProductType(), offerRs.getProductType());
    Assertions.assertEquals(REPORTING_START_DATE, offerRs.getReportingStart());
    Assertions.assertEquals(REPORTING_END_DATE, offerRs.getReportingEnd());

    Assertions.assertEquals(
        zlOffer.getIndexedData().getCountryRegions().get(0).getCountry(),
        offerRs.getCountryRegions().get(0).getCountry());

    Assertions.assertEquals(
        zlOffer.getIndexedData().getCountryRegions().get(0).getRegionName(),
        offerRs.getCountryRegions().get(0).getRegion());
  }

  @Test
  void convertToDbWhenCountryRegionsAreEmpty() {
    final ZlOffer zlOffer = generateZlOffer();
    zlOffer.getIndexedData().setCountryRegions(null);

    final OfferRs offerRs = converter.convertToDto(zlOffer).await().atMost(Duration.ofMillis(100));

    Assertions.assertTrue(CollectionUtils.isNullOrEmpty(offerRs.getCountryRegions()));
  }

  private ZlOffer generateZlOffer() {
    final ZlOffer.IndexedOfferData indexedOfferData =
        ZlOffer.IndexedOfferData.builder()
            .inventoryId("73799_1")
            .reportingStart(InstantDeserializer.toInstant(REPORTING_START_DATE).toEpochMilli())
            .reportingEnd(InstantDeserializer.toInstant(REPORTING_END_DATE).toEpochMilli())
            .productType(W3ProductType.GO)
            .energyTypes(List.of(EnergyType.MARINE))
            .countryRegionIndexes(List.of("US-CA"))
            .countryRegions(
                List.of(ZlCountryRegion.builder().country("US").regionName("CA").build()))
            .build();

    return ZlOffer.builder()
        .indexedData(indexedOfferData)
        .id("id")
        .disabled(false)
        .description("desc")
        .image("image")
        .energyUnit(ConsumptionUnit.KWh)
        .unitPrice(BigDecimal.ONE)
        .build();
  }
}
