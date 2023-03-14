package green.zerolabs.commons.core.service.impl;

import green.zerolabs.commons.core.converter.db2dto.GeneratorDbToDtoConverter;
import green.zerolabs.commons.core.model.ZlBlockchainProperties;
import green.zerolabs.commons.core.model.ZlGenerator;
import green.zerolabs.commons.core.model.graphql.generated.GeneratorRs;
import green.zerolabs.commons.core.model.graphql.generated.W3BlockchainPropertiesRs;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.core.utils.UnitTestUtils;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.time.Duration;

import static green.zerolabs.commons.core.model.TestCoreConstants.IMAGES;
import static green.zerolabs.commons.core.model.TestCoreConstants.LINKS;
import static org.junit.jupiter.api.Assertions.*;

/***
 * Created by Triphon Penakov 2023-02-22
 */
@Getter
class DbToDtoConverterServiceImplTest {

  public static final String PK = "PK";
  UnitTestUtils unitTestUtils;
  DbToDtoConverterServiceImpl service;

  @BeforeEach
  void beforeEach() {
    unitTestUtils = UnitTestUtils.of();

    final JsonUtils jsonUtils = getUnitTestUtils().getJsonUtils();
    final GeneratorDbToDtoConverter converter = new GeneratorDbToDtoConverter(jsonUtils);
    service = new DbToDtoConverterServiceImpl(jsonUtils);
    service.registerConverter(converter);
  }

  @Test
  void convertToDbTest() {
    final GeneratorRs input = new GeneratorRs();
    input.setLinks(LINKS);
    input.setImages(IMAGES);
    final ZlGenerator converted =
        service
            .convertToDb(input, ZlGenerator.class, ZlGenerator.builder().build())
            .await()
            .atMost(Duration.ofSeconds(5));
    assertFalse(CollectionUtils.isNullOrEmpty(converted.getImagesTransformed()));
    assertFalse(CollectionUtils.isNullOrEmpty(converted.getLinksTransformed()));
    assertFalse(CollectionUtils.isNullOrEmpty(input.getImages()));
    assertFalse(CollectionUtils.isNullOrEmpty(input.getLinks()));
    assertEquals(IMAGES, converted.getImagesTransformed());
    assertEquals(LINKS, converted.getLinksTransformed());
    assertEquals(IMAGES, input.getImages());
    assertEquals(LINKS, input.getLinks());
  }

  @Test
  void convertToDb_WhenConverterIsNotRegisteredTest() {
    final W3BlockchainPropertiesRs input = new W3BlockchainPropertiesRs();
    input.setPlatformOperatorPrivateKey(PK);
    final ZlBlockchainProperties converted =
        service
            .convertToDb(
                input, ZlBlockchainProperties.class, ZlBlockchainProperties.builder().build())
            .await()
            .atMost(Duration.ofSeconds(5));
    assertEquals(PK, converted.getPlatformOperatorPrivateKey());
    assertEquals(PK, input.getPlatformOperatorPrivateKey());
  }

  @Test
  void convertToDb_WhenNullInputTest() {
    assertNull(
        service
            .convertToDb(null, W3BlockchainPropertiesRs.class, null)
            .await()
            .atMost(Duration.ofSeconds(5)));
  }

  @Test
  void convertToDtoTest() {
    final ZlGenerator input =
        ZlGenerator.builder().linksTransformed(LINKS).imagesTransformed(IMAGES).build();
    final GeneratorRs converted =
        service.convertToDto(input, GeneratorRs.class).await().atMost(Duration.ofSeconds(5));
    assertFalse(CollectionUtils.isNullOrEmpty(converted.getImages()));
    assertFalse(CollectionUtils.isNullOrEmpty(converted.getLinks()));
    assertFalse(CollectionUtils.isNullOrEmpty(input.getImagesTransformed()));
    assertFalse(CollectionUtils.isNullOrEmpty(input.getLinksTransformed()));
    assertEquals(IMAGES, converted.getImages());
    assertEquals(LINKS, converted.getLinks());
    assertEquals(IMAGES, input.getImagesTransformed());
    assertEquals(LINKS, input.getLinksTransformed());
  }

  @Test
  void convertToDto_WhenConverterIsNotRegisteredTest() {
    final ZlBlockchainProperties input =
        ZlBlockchainProperties.builder().platformOperatorPrivateKey(PK).build();
    final W3BlockchainPropertiesRs converted =
        service
            .convertToDto(input, W3BlockchainPropertiesRs.class)
            .await()
            .atMost(Duration.ofSeconds(5));
    assertEquals(PK, converted.getPlatformOperatorPrivateKey());
    assertEquals(PK, input.getPlatformOperatorPrivateKey());
  }

  @Test
  void convertToDto_WhenNullInputTest() {
    assertNull(
        service
            .convertToDto(null, W3BlockchainPropertiesRs.class)
            .await()
            .atMost(Duration.ofSeconds(5)));
  }

}
