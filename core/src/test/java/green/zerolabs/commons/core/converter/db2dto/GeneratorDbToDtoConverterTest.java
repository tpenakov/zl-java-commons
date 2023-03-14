package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlGenerator;
import green.zerolabs.commons.core.model.graphql.generated.GeneratorRs;
import green.zerolabs.commons.core.utils.UnitTestUtils;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static green.zerolabs.commons.core.model.TestCoreConstants.IMAGES;
import static green.zerolabs.commons.core.model.TestCoreConstants.LINKS;
import static org.junit.jupiter.api.Assertions.*;

/***
 * Created by Triphon Penakov 2023-02-22
 */
@Getter
class GeneratorDbToDtoConverterTest {

  UnitTestUtils unitTestUtils;
  GeneratorDbToDtoConverter converter;

  @BeforeEach
  void beforeEach() {
    unitTestUtils = UnitTestUtils.of();
    converter = new GeneratorDbToDtoConverter(getUnitTestUtils().getJsonUtils());
  }

  @Test
  void convertToDbTest() {
    final GeneratorRs input = new GeneratorRs();
    input.setLinks(LINKS);
    input.setImages(IMAGES);
    input.setNameplateCapacity(Long.MAX_VALUE);
    input.setCommissioningDate(Instant.now().toString());
    final ZlGenerator converted =
        converter
            .convertToDb(input, ZlGenerator.builder().build())
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

    assertNotNull(input.getCommissioningDate());
    assertNotNull(converted.getCommissioningDate());
    assertEquals(input.getCommissioningDate(), converted.getCommissioningDate().toString());
    assertNotNull(input.getNameplateCapacity());
    assertNotNull(converted.getNameplateCapacity());
    assertEquals(input.getNameplateCapacity(), converted.getNameplateCapacity().longValue());
  }

  @Test
  void convertToDbWithNullImagesAndLinksTest() {
    final GeneratorRs input = new GeneratorRs();
    input.setNameplateCapacity(Long.MAX_VALUE);
    input.setCommissioningDate(Instant.now().toString());
    final ZlGenerator converted =
        converter
            .convertToDb(input, ZlGenerator.builder().build())
            .await()
            .atMost(Duration.ofSeconds(5));
    assertTrue(CollectionUtils.isNullOrEmpty(input.getImages()));
    assertTrue(CollectionUtils.isNullOrEmpty(input.getLinks()));
    assertNull(converted.getImagesTransformed());
    assertNull(converted.getLinksTransformed());
    assertNotNull(input.getCommissioningDate());
    assertNotNull(converted.getCommissioningDate());
    assertEquals(input.getCommissioningDate(), converted.getCommissioningDate().toString());
    assertNotNull(input.getNameplateCapacity());
    assertNotNull(converted.getNameplateCapacity());
    assertEquals(input.getNameplateCapacity(), converted.getNameplateCapacity().longValue());
  }

  @Test
  void convertToDtoTest() {
    final ZlGenerator input =
        ZlGenerator.builder()
            .linksTransformed(LINKS)
            .imagesTransformed(IMAGES)
            .nameplateCapacity(BigDecimal.valueOf(Long.MAX_VALUE))
            .commissioningDate(Instant.now())
            .build();
    final GeneratorRs converted =
        converter.convertToDto(input).await().atMost(Duration.ofSeconds(5));
    assertFalse(CollectionUtils.isNullOrEmpty(converted.getImages()));
    assertFalse(CollectionUtils.isNullOrEmpty(converted.getLinks()));
    assertFalse(CollectionUtils.isNullOrEmpty(input.getImagesTransformed()));
    assertFalse(CollectionUtils.isNullOrEmpty(input.getLinksTransformed()));
    assertEquals(IMAGES, converted.getImages());
    assertEquals(LINKS, converted.getLinks());
    assertEquals(IMAGES, input.getImagesTransformed());
    assertEquals(LINKS, input.getLinksTransformed());

    assertNotNull(input.getCommissioningDate());
    assertNotNull(converted.getCommissioningDate());
    assertEquals(input.getCommissioningDate().toString(), converted.getCommissioningDate());
    assertNotNull(input.getNameplateCapacity());
    assertNotNull(converted.getNameplateCapacity());
    assertEquals(input.getNameplateCapacity().longValue(), converted.getNameplateCapacity());
  }

  @Test
  void convertToDtoWithNullImagesAndLinksTest() {
    final ZlGenerator input =
        ZlGenerator.builder()
            .linksTransformed(null)
            .imagesTransformed(null)
            .nameplateCapacity(BigDecimal.valueOf(Long.MAX_VALUE))
            .commissioningDate(Instant.now())
            .build();
    final GeneratorRs converted =
        converter.convertToDto(input).await().atMost(Duration.ofSeconds(5));
    assertNull(converted.getImages());
    assertNull(converted.getLinks());

    assertNotNull(input.getCommissioningDate());
    assertNotNull(converted.getCommissioningDate());
    assertEquals(input.getCommissioningDate().toString(), converted.getCommissioningDate());
    assertNotNull(input.getNameplateCapacity());
    assertNotNull(converted.getNameplateCapacity());
    assertEquals(input.getNameplateCapacity().longValue(), converted.getNameplateCapacity());
  }
}
