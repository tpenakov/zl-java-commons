package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlGenerator;
import green.zerolabs.commons.core.model.ZlProof;
import green.zerolabs.commons.core.model.graphql.generated.ProofRs;
import green.zerolabs.commons.core.utils.DeepClone;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;
import java.util.Optional;

/***
 * Created by Triphon Penakov 2023-02-22
 */
@Getter(AccessLevel.PROTECTED)
public class ProofDbToDtoConverter extends GenericInternalDbToDtoConverter<ZlProof, ProofRs> {

  private final GeneratorDbToDtoConverter generatorDbToDtoConverter;

  public ProofDbToDtoConverter(
      final DeepClone cloneable, final GeneratorDbToDtoConverter generatorDbToDtoConverter) {
    super(cloneable, ZlProof.class, ProofRs.class);
    this.generatorDbToDtoConverter = generatorDbToDtoConverter;
  }

  @Override
  public Uni<ZlProof> convertToDb(final ProofRs item, final ZlProof previous) {
    return moveToTmpFromDto(item)
        .onItem()
        .transformToUni(
            tmpItem ->
                super.convertToDb(item, previous)
                    .onItem()
                    .ifNotNull()
                    .invoke(converted -> fillDbFromTmp(tmpItem, converted))
                    .onItemOrFailure()
                    .call((unused, throwable) -> fillDtoFromDb(item, tmpItem)));
  }

  @Override
  public Uni<ProofRs> convertToDto(final ZlProof item) {
    final ZlProof tmpItem = moveToTmpFromDb(item);

    return super.convertToDto(item)
        .onItem()
        .ifNotNull()
        .call(converted -> fillDtoFromDb(converted, tmpItem))
        .onItemOrFailure()
        .invoke((unused, throwable) -> fillDbFromTmp(tmpItem, item));
  }

  static ZlProof moveToTmpFromDb(final ZlProof item) {
    final ZlProof result = ZlProof.builder().generators(item.getGenerators()).build();
    item.setGenerators(null);
    return result;
  }

  Uni<ZlProof> moveToTmpFromDto(final ProofRs item) {
    final ZlProof tmpItem = ZlProof.builder().build();

    final Optional<ProofRs> proofRsOptional = Optional.ofNullable(item);
    return Uni.createFrom()
        .item(tmpItem)
        .onItem()
        .call(
            zlProof ->
                Uni.createFrom()
                    .optional(
                        proofRsOptional
                            .map(ProofRs::getGenerators)
                            .filter(items -> !CollectionUtils.isNullOrEmpty(items)))
                    .onItem()
                    .ifNotNull()
                    .transformToMulti(items -> Multi.createFrom().iterable(items))
                    .onItem()
                    .transformToUni(rs -> getGeneratorDbToDtoConverter().convertToDb(rs, null))
                    .concatenate()
                    .collect()
                    .asList()
                    .onItem()
                    .invoke(
                        zlGenerators -> {
                          zlProof.setGenerators(
                              CollectionUtils.isNullOrEmpty(zlGenerators) ? null : zlGenerators);
                          proofRsOptional.ifPresent(proofRs -> proofRs.setGenerators(null));
                        }));
  }

  static void fillDbFromTmp(final ZlProof tmpItem, final ZlProof converted) {
    final List<ZlGenerator> generators = tmpItem.getGenerators();
    converted.setGenerators(CollectionUtils.isNullOrEmpty(generators) ? null : generators);
  }

  Uni<Void> fillDtoFromDb(final ProofRs dto, final ZlProof db) {
    return Uni.createFrom()
        .optional(Optional.ofNullable(db).map(ZlProof::getGenerators))
        .onItem()
        .ifNotNull()
        .transformToMulti(items -> Multi.createFrom().iterable(items))
        .onItem()
        .transformToUni(item -> getGeneratorDbToDtoConverter().convertToDto(item))
        .concatenate()
        .collect()
        .asList()
        .onItem()
        .ifNotNull()
        .invoke(
            items ->
                Optional.ofNullable(dto)
                    .ifPresent(
                        proofRs ->
                            proofRs.setGenerators(
                                CollectionUtils.isNullOrEmpty(items) ? null : items)))
        .replaceWithVoid();
  }
}
