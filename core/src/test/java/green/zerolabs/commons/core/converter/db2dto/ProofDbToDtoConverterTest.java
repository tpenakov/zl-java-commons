package green.zerolabs.commons.core.converter.db2dto;

import green.zerolabs.commons.core.model.ZlInternal;
import green.zerolabs.commons.core.model.ZlProof;
import green.zerolabs.commons.core.model.graphql.generated.ProofRs;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.core.utils.UnitTestUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/***
 * Created by Triphon Penakov 2023-03-08
 */
@Getter
@Slf4j
class ProofDbToDtoConverterTest {

  UnitTestUtils unitTestUtils;
  JsonUtils jsonUtils;
  ConverterUtils converterUtils;

  ProofDbToDtoConverter converter;

  @BeforeEach
  void beforeEach() {
    unitTestUtils = UnitTestUtils.of();
    jsonUtils = getUnitTestUtils().getJsonUtils();
    converterUtils = getUnitTestUtils().getConverterUtils();

    converter = new ProofDbToDtoConverter(jsonUtils, new GeneratorDbToDtoConverter(jsonUtils));
  }

  @Test
  void proofV1ConversionsTest() {
    final ZlProof zlProof =
        getUnitTestUtils()
            .readFromFile("json/proof/v1/proof-v1-with-generator.json", ZlProof.class);

    zlProof.getIndexedCertificates().stream()
        .forEach(
            rs -> {
              final Long milli = getConverterUtils().toEpochMilli(rs.getStartDate());
              rs.setStartDate(getConverterUtils().epochMilliToString(milli));
            });

    final ProofRs proofRs =
        getConverter().convertToDto(zlProof).await().atMost(Duration.ofSeconds(5));
    final ZlProof result =
        getConverter().convertToDb(proofRs, null).await().atMost(Duration.ofSeconds(5));

    clearInternals(zlProof);
    clearInternals(result);

    log.info("initial proof: {}", getJsonUtils().toStringLazy(zlProof));
    log.info("result  proof: {}", getJsonUtils().toStringLazy(result));
    log.info("ProofRs: {}", getJsonUtils().toStringLazy(proofRs));

    Assertions.assertEquals(zlProof, result);
  }

  @Test
  void moveToTmpFromDto_WhenEmptyGenerators_ThenNullTest() {
    final ZlProof item = ZlProof.builder().generators(List.of()).build();
    final ProofRs result = getConverter().convertToDto(item).await().atMost(Duration.ofSeconds(5));
    Assertions.assertNull(result.getGenerators());

    result.setGenerators(List.of());
    final ZlProof tmp =
        getConverter().moveToTmpFromDto(result).await().atMost(Duration.ofSeconds(5));
    Assertions.assertNull(tmp.getGenerators());
  }

  private static void clearInternals(final ZlProof zlProof) {
    clearInternal(zlProof);
    Optional.ofNullable(zlProof.getGenerators()).stream()
        .flatMap(Collection::stream)
        .forEach(zlInternal -> clearInternal(zlInternal));
  }

  private static void clearInternal(final ZlInternal zlInternal) {
    if (Objects.isNull(zlInternal)) {
      return;
    }
    zlInternal.setInternal(null);
  }
}
