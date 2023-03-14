package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlGenerator;
import green.zerolabs.commons.core.model.graphql.generated.GeneratorRs;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/***
 * Created by Triphon Penakov 2023-02-22
 */
public class GeneratorDbToDtoConverter
    extends GenericInternalDbToDtoConverter<ZlGenerator, GeneratorRs> {
  public GeneratorDbToDtoConverter(final DeepClone cloneable) {
    super(cloneable, ZlGenerator.class, GeneratorRs.class);
  }

  @Override
  public Uni<ZlGenerator> convertToDb(final GeneratorRs item, final ZlGenerator previous) {
    final ZlGenerator tmpItem = moveToTmpFromDto(item);
    return super.convertToDb(item, previous)
        .onItem()
        .ifNotNull()
        .invoke(converted -> fillDbFromTmp(tmpItem, converted))
        .onItemOrFailure()
        .invoke((unused, throwable) -> fillDtoFromDb(item, tmpItem));
  }

  @Override
  public Uni<GeneratorRs> convertToDto(final ZlGenerator item) {
    final ZlGenerator tmpItem = moveToTmpFromDb(item);

    return super.convertToDto(item)
        .onItem()
        .ifNotNull()
        .invoke(converted -> fillDtoFromDb(converted, tmpItem))
        .onItemOrFailure()
        .invoke((unused, throwable) -> fillDbFromTmp(tmpItem, item));
  }

  static ZlGenerator moveToTmpFromDb(final ZlGenerator item) {
    final ZlGenerator result =
        ZlGenerator.builder()
            .images(item.getImages())
            .links(item.getLinks())
            .imagesTransformed(item.getImagesTransformed())
            .linksTransformed(item.getLinksTransformed())
            .nameplateCapacity(item.getNameplateCapacity())
            .commissioningDate(item.getCommissioningDate())
            .build();

    if (CollectionUtils.isNullOrEmpty(result.getImagesTransformed())
        && StringUtils.isNotBlank(result.getImages())) {
      result.setImagesTransformed(ConverterUtils.splitCsvArrayToList(result.getImages()));
    }

    if (CollectionUtils.isNullOrEmpty(result.getLinksTransformed())
        && StringUtils.isNotBlank(result.getLinks())) {
      result.setLinksTransformed(ConverterUtils.splitCsvArrayToList(result.getLinks()));
    }

    item.setImages(null);
    item.setLinks(null);
    item.setImagesTransformed(null);
    item.setLinksTransformed(null);
    item.setCommissioningDate(null);
    item.setNameplateCapacity(null);

    return result;
  }

  static ZlGenerator moveToTmpFromDto(final GeneratorRs item) {
    final ZlGenerator tmpItem =
        ZlGenerator.builder()
            .imagesTransformed(item.getImages())
            .linksTransformed(item.getLinks())
            .commissioningDate(
                Objects.isNull(item.getCommissioningDate())
                    ? null
                    : Instant.parse(item.getCommissioningDate()))
            .nameplateCapacity(
                Objects.nonNull(item.getNameplateCapacity())
                    ? BigDecimal.valueOf(item.getNameplateCapacity())
                    : null)
            .build();
    item.setImages(null);
    item.setLinks(null);
    item.setCommissioningDate(null);
    item.setNameplateCapacity(null);
    return tmpItem;
  }

  static void fillDbFromTmp(final ZlGenerator tmpItem, final ZlGenerator converted) {
    converted.setLinksTransformed(tmpItem.getLinksTransformed());
    converted.setImagesTransformed(tmpItem.getImagesTransformed());
    converted.setCommissioningDate(tmpItem.getCommissioningDate());
    converted.setNameplateCapacity(tmpItem.getNameplateCapacity());
  }

  static void fillDtoFromDb(final GeneratorRs dto, final ZlGenerator db) {
    dto.setLinks(db.getLinksTransformed());
    dto.setImages(db.getImagesTransformed());
    dto.setCommissioningDate(
        Optional.ofNullable(db.getCommissioningDate()).map(Objects::toString).orElse(null));
    dto.setNameplateCapacity(
        Optional.ofNullable(db.getNameplateCapacity()).map(BigDecimal::longValue).orElse(null));
  }
}
