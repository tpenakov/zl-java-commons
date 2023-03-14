package green.zerolabs.commons.dynamo.db.utils;

import green.zerolabs.commons.core.model.graphql.generated.AgreementRs;
import green.zerolabs.commons.core.model.graphql.generated.ProofRs;
import green.zerolabs.commons.core.utils.MutinyUtils;
import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/***
 * Created by Triphon Penakov 2022-11-04
 */
@Getter(AccessLevel.PACKAGE)
@Slf4j
class DynamoDbFillDetailsUtilsTest {

  UnitTestUtils unitTestUtils;
  DynamoDbFinder pkFinder;

  @BeforeEach
  void beforeEach() {
    unitTestUtils = UnitTestUtils.of();
    pkFinder = getUnitTestUtils().getPkFinder();
  }

  @Test
  void initTest() {
    assertNotNull(new DynamoDbFillDetailsUtils());
  }

  @Test
  void fill_NullId_Test() {
    final ProofRs proofRs = new ProofRs();
    DynamoDbFillDetailsUtils.fill(
            getPkFinder(), null, null, AgreementRs.class, proofRs::setAgreement)
        .await()
        .atMost(Duration.ofSeconds(1));
    assertNull(proofRs.getAgreement());
  }

  @Test
  void fillTest() {

    final AgreementRs agreementRs = new AgreementRs();
    final String agreementId = "agreementId";
    agreementRs.setId(agreementId);
    doReturn(Uni.createFrom().item(agreementRs))
        .when(getPkFinder())
        .findOne(anyString(), anyString(), eq(AgreementRs.class));

    final ProofRs proofRs = new ProofRs();
    DynamoDbFillDetailsUtils.fill(
            getPkFinder(),
            agreementId,
            "dataKey",
            AgreementRs.class,
            proofRs::setAgreement)
        .await()
        .atMost(Duration.ofSeconds(1));
    assertNotNull(proofRs.getAgreement());
    assertEquals(agreementId, proofRs.getAgreement().getId());
  }

  @Test
  void fill_UseContextCache_Test() {

    final AgreementRs agreementRs = new AgreementRs();
    final String agreementId = "agreementId";
    agreementRs.setId(agreementId);
    doReturn(Uni.createFrom().item(agreementRs))
        .when(getPkFinder())
        .findOne(anyString(), anyString(), eq(AgreementRs.class));

    final Context context = Context.empty();

    final ProofRs proofRs = new ProofRs();
    MutinyUtils.runWithContext(
            () ->
                Multi.createFrom()
                    .range(0, 5)
                    .onItem()
                    .transformToUni(
                        integer ->
                            DynamoDbFillDetailsUtils.fill(
                                getPkFinder(),
                                agreementId,
                                "dataKey",
                                AgreementRs.class,
                                proofRs::setAgreement))
                    .concatenate()
                    .collect()
                    .asList(),
            new AtomicReference<>(context))
        .await()
        .atMost(Duration.ofSeconds(1));
    assertNotNull(proofRs.getAgreement());
    assertEquals(agreementId, proofRs.getAgreement().getId());

    verify(getPkFinder(), times(1)).findOne(anyString(), anyString(), eq(AgreementRs.class));
  }
}
