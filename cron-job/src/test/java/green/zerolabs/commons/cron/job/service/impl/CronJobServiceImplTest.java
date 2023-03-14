package green.zerolabs.commons.cron.job.service.impl;

import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import green.zerolabs.commons.core.service.CronJobProcessor;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;

import static green.zerolabs.commons.cron.job.service.impl.CronJobServiceImpl.*;
import static org.mockito.ArgumentMatchers.any;

/***
 * Created by Triphon Penakov 2022-11-21
 */
@Getter(AccessLevel.PACKAGE)
@Slf4j
class CronJobServiceImplTest {

  public static final String WRONG = "wrong";
  CronJobServiceImpl service;

  @BeforeEach
  void beforeEach() {
    service = Mockito.spy(new CronJobServiceImpl());
    CronJobProcessor.PROCESSORS.clear();
  }

  @Test
  void isSupportedTest() {
    Assertions.assertFalse(getService().isSupported(Map.of(), ZlLambdaRqContext.builder().build()));
    Assertions.assertTrue(
        getService()
            .isSupported(
                Map.of(DETAIL_TYPE, SCHEDULED_EVENT), ZlLambdaRqContext.builder().build()));
  }

  @Test
  void getPriorityTest() {
    Assertions.assertEquals(PRIORITY, getService().getPriority());
  }

  @Test
  void logOutputTest() {
    Assertions.assertEquals(Boolean.TRUE, getService().logOutput(Boolean.TRUE));
  }

  @Test
  void handle_EmptyProcessorsList_TrueTest() {
    Assertions.assertEquals(
        Boolean.TRUE,
        getService()
            .handle(Map.of(), ZlLambdaRqContext.builder().build())
            .await()
            .atMost(Duration.ofSeconds(5)));
  }

  @Test
  void handle_WithProcessor_OkTest() {
    handle_WithProcessor_OkTest(true, true, true);
    handle_WithProcessor_OkTest(true, false, false);
    handle_WithProcessor_OkTest(false, true, false);
    handle_WithProcessor_OkTest(false, false, false);
  }

  @Test
  void handle_WithProcessorThrow_FalseTest() {
    final CronJobProcessor cronJobProcessor = Mockito.spy(CronJobProcessor.class);
    Mockito.doReturn(Uni.createFrom().failure(new RuntimeException(WRONG)))
        .when(cronJobProcessor)
        .handle(any());
    CronJobProcessor.PROCESSORS.add(cronJobProcessor);
    Assertions.assertFalse(
        getService()
            .handle(Map.of(), ZlLambdaRqContext.builder().build())
            .onItem()
            .castTo(Boolean.class)
            .await()
            .atMost(Duration.ofSeconds(5)));
    Mockito.verify(getService(), Mockito.times(1)).handleError(any());
  }

  void handle_WithProcessor_OkTest(
      final boolean invocation1, final boolean invocation2, final boolean expected) {
    final CronJobProcessor cronJobProcessor = Mockito.spy(CronJobProcessor.class);
    Mockito.doReturn(Uni.createFrom().item(invocation1), Uni.createFrom().item(invocation2))
        .when(cronJobProcessor)
        .handle(any());
    CronJobProcessor.PROCESSORS.add(cronJobProcessor);
    CronJobProcessor.PROCESSORS.add(cronJobProcessor);
    Assertions.assertEquals(
        expected,
        getService()
            .handle(Map.of(), ZlLambdaRqContext.builder().build())
            .await()
            .atMost(Duration.ofSeconds(5)));
    Mockito.verify(cronJobProcessor, Mockito.times(2)).handle(any());
  }
}
