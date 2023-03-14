package green.zerolabs.commons.lambda.triggers.emulator;

import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsProcessor;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.lambda.triggers.emulator.config.*;
import green.zerolabs.commons.lambda.triggers.emulator.service.LambdaTriggerService;
import green.zerolabs.commons.sqs.utils.AllSqsUtils;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static green.zerolabs.commons.core.model.CoreConstants.*;

/***
 * Created by Triphon Penakov 2023-02-10
 */
@ApplicationScoped
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class SqsTriggers {
  private final JsonUtils jsonUtils;
  private final AllSqsUtils allSqsUtils;
  private final SqsProcessor sqsW3Processor;
  private final SqsProcessor sqsW3RetryProcessor;
  private final SqsProcessor sqsS3Processor;
  private final SqsProcessor sqsS3RetryProcessor;
  private final SqsProcessor sqsDbProcessor;
  private final SqsProcessor sqsDbWithDuplicationProcessor;

  private final SqsAsyncClient sqs;

  final DbWithDuplicationSqsConfig dbWithDuplicationSqsConfig;
  final DbSqsConfig dbSqsConfig;
  final W3SqsConfig w3SqsConfig;
  final W3RetrySqsConfig w3RetrySqsConfig;
  final S3SqsConfig s3SqsConfig;
  final S3RetrySqsConfig s3RetrySqsConfig;

  public SqsTriggers(
      final JsonUtils jsonUtils,
      final AllSqsUtils allSqsUtils,
      @Named(SQS_W3_PROCESSOR) final SqsProcessor sqsW3Processor,
      @Named(SQS_W3_RETRY_PROCESSOR) final SqsProcessor sqsW3RetryProcessor,
      @Named(SQS_S3_PROCESSOR) final SqsProcessor sqsS3Processor,
      @Named(SQS_IPFS_RETRY_PROCESSOR) final SqsProcessor sqsS3RetryProcessor,
      @Named(SQS_DB_PROCESSOR) final SqsProcessor sqsDbProcessor,
      @Named(SQS_DB_WITH_DUPLICATION_PROCESSOR) final SqsProcessor sqsDbWithDuplicationProcessor,
      final SqsAsyncClient sqs,
      final DbWithDuplicationSqsConfig dbWithDuplicationSqsConfig,
      final DbSqsConfig dbSqsConfig,
      final W3SqsConfig w3SqsConfig,
      final W3RetrySqsConfig w3RetrySqsConfig,
      final S3SqsConfig s3SqsConfig,
      final S3RetrySqsConfig s3RetrySqsConfig) {
    this.jsonUtils = jsonUtils;
    this.allSqsUtils = allSqsUtils;
    this.sqsW3Processor = sqsW3Processor;
    this.sqsW3RetryProcessor = sqsW3RetryProcessor;
    this.sqsS3Processor = sqsS3Processor;
    this.sqsS3RetryProcessor = sqsS3RetryProcessor;
    this.sqsDbProcessor = sqsDbProcessor;
    this.sqsDbWithDuplicationProcessor = sqsDbWithDuplicationProcessor;
    this.sqs = sqs;
    this.dbWithDuplicationSqsConfig = dbWithDuplicationSqsConfig;
    this.dbSqsConfig = dbSqsConfig;
    this.w3SqsConfig = w3SqsConfig;
    this.w3RetrySqsConfig = w3RetrySqsConfig;
    this.s3SqsConfig = s3SqsConfig;
    this.s3RetrySqsConfig = s3RetrySqsConfig;
  }

  @SuppressWarnings("unused")
  void startup(@Observes final StartupEvent event) {
    log.info("Triggers.startup");

    run(getSqsDbProcessor(), getDbSqsConfig());
    run(getSqsDbWithDuplicationProcessor(), getDbWithDuplicationSqsConfig());
    run(getSqsW3Processor(), getW3SqsConfig());
    run(getSqsW3RetryProcessor(), getW3RetrySqsConfig());
    run(getSqsS3Processor(), getS3SqsConfig());
    run(getSqsS3RetryProcessor(), getS3RetrySqsConfig());
  }

  void run(final SqsProcessor sqsProcessor, final SqsConfig config) {
    final LambdaTriggerService triggerService =
        RestClientBuilder.newBuilder()
            .baseUri(URI.create(config.lambdaUrl()))
            .build(LambdaTriggerService.class);
    Uni.createFrom()
        .deferred(() -> Multi.createFrom().ticks().every(Duration.ofMillis(100)).toUni())
        .onItem()
        .transformToUni(aLong -> sqsProcessor.receive())
        .onItem()
        .ifNotNull()
        .transformToMulti(items -> Multi.createFrom().iterable(items))
        .onItem()
        .transformToUni(
            zlSqsItem ->
                Uni.createFrom()
                    .completionStage(
                        () ->
                            getSqs()
                                .deleteMessage(
                                    builder ->
                                        builder
                                            .queueUrl(config.url())
                                            .receiptHandle(zlSqsItem.getReceiptHandle())))
                    .onItem()
                    .transform(unused -> zlSqsItem))
        .concatenate()
        .onFailure()
        .retry()
        .indefinitely()
        .collect()
        .asList()
        .repeat()
        .indefinitely()
        .filter(CollectionUtils::isNotEmpty)
        .onItem()
        .transformToUni(
            zlSqsItems ->
                Multi.createFrom()
                    .iterable(zlSqsItems)
                    .onItem()
                    .transformToUni(
                        zlSqsItem ->
                            processItem(zlSqsItem, triggerService)
                                .onItem()
                                .ifNull()
                                .continueWith(Map.of()))
                    .concatenate()
                    .collect()
                    .asList()
                    .onItem()
                    .ignore()
                    .andSwitchTo(() -> Uni.createFrom().voidItem()))
        .concatenate()
        .subscribe()
        .with(
            items -> {}, throwable -> log.error("error: ", throwable), () -> log.info("Completed"));
  }

  private Uni<Map<String, Object>> processItem(
      final ZlSqsItem zlSqsItem, final LambdaTriggerService triggerService) {
    final Map<String, Object> data =
        getJsonUtils().toMap(getAllSqsUtils().toSqsEventMessage(zlSqsItem));
    return Uni.createFrom()
        .deferred(() -> Uni.createFrom().item(triggerService))
        .onSubscription()
        .invoke(() -> log.info("zlSqsItem : {}", getJsonUtils().toStringLazy(zlSqsItem)))
        .onItem()
        .ifNotNull()
        .transformToUni(service -> Uni.createFrom().completionStage(service.send(data)))
        .onItem()
        .ifNotNull()
        .transform(
            s -> {
              log.debug("lambda result: {}", s);
              return getJsonUtils().toMap(s);
            });
  }
}
