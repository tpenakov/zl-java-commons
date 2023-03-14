package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlOffer;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionLocationRq;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.OfferSaveRq;
import green.zerolabs.commons.core.model.graphql.generated.W3ProductType;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.UnitTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public class OfferSaveRqConverterTest {

  private OfferSaveRqConverter converter;
  private ConverterUtils converterUtils;

  @BeforeEach
  void setUp() {
    converterUtils = UnitTestUtils.of().getConverterUtils();
    converter = new OfferSaveRqConverter(UnitTestUtils.of().getJsonUtils(), converterUtils);
  }

  @Test
  void convertToDbTest() {
    final OfferSaveRq offerRq = generateOfferSaveRq();

    final ZlOffer offer =
        converter.convertToDb(offerRq, null).await().atMost(Duration.ofMillis(100));

    Assertions.assertNotNull(offer);
    Assertions.assertEquals(false, offer.getDisabled());
    Assertions.assertEquals(offerRq.getId(), offer.getId());
    Assertions.assertEquals(offerRq.getImage(), offer.getImage());
    Assertions.assertEquals(offerRq.getDescription(), offer.getDescription());
    Assertions.assertEquals(offerRq.getEnergyUnit(), offer.getEnergyUnit());
    Assertions.assertEquals(offerRq.getUnitPrice(), offer.getUnitPrice());

    Assertions.assertNotNull(offer.getIndexedData());

    Assertions.assertNotNull(offer.getIndexedData().getReportingStart());
    Assertions.assertNotNull(offer.getIndexedData().getReportingEnd());
    Assertions.assertNotNull(offer.getIndexedData().getCountryRegionIndexes());

    Assertions.assertEquals(offerRq.getInventoryId(), offer.getIndexedData().getInventoryId());
    Assertions.assertEquals(offerRq.getEnergyTypes(), offer.getIndexedData().getEnergyTypes());
    Assertions.assertEquals(offerRq.getProductType(), offer.getIndexedData().getProductType());

    Assertions.assertEquals(
        offerRq.getCountryRegions().get(0).getCountry(),
        offer.getIndexedData().getCountryRegions().get(0).getCountry());

    Assertions.assertEquals(
        offerRq.getCountryRegions().get(0).getRegion(),
        offer.getIndexedData().getCountryRegions().get(0).getRegionName());
  }

  @Test
  void convertToDbWhenCountryRegionsAreEmpty() {
    final OfferSaveRq offerRq = generateOfferSaveRq();
    offerRq.setCountryRegions(null);

    final ZlOffer offer =
        converter.convertToDb(offerRq, null).await().atMost(Duration.ofMillis(100));

    Assertions.assertTrue(
        CollectionUtils.isNullOrEmpty(offer.getIndexedData().getCountryRegions()));
  }

  @Test
  void convertToDtoTest() {
    Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> converter.convertToDto(null).await().atMost(Duration.ofMillis(100)));
  }

  private OfferSaveRq generateOfferSaveRq() {
    final OfferSaveRq offerRq = new OfferSaveRq();

    offerRq.setInventoryId("73799_1");
    offerRq.setEnergyUnit(ConsumptionUnit.KWh);
    offerRq.setUnitPrice(BigDecimal.TEN);
    offerRq.setProductType(W3ProductType.REC);
    offerRq.setReportingStart("2021-01-01T00:00:00Z");
    offerRq.setReportingEnd("2030-01-01T00:00:00Z");

    final ConsumptionLocationRq countryRegion = new ConsumptionLocationRq();
    countryRegion.setCountry("ESP");
    countryRegion.setRegion("CAT");

    offerRq.setCountryRegions(List.of(countryRegion));

    return offerRq;
  }
}
