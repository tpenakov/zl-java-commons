package green.zerolabs.commons.lambda.triggers.emulator.config;

import green.zerolabs.commons.core.service.SqsProcessor;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.sqs.service.SqsProcessorWrapper;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import static green.zerolabs.commons.core.model.CoreConstants.*;

/*
 * Created by triphon 16.06.22 г.
 */
@SuppressWarnings("unused")
@Slf4j
public class ServicesImplConfig {

  void startup(@Observes final StartupEvent event) throws Exception {
    log.info("ServicesImplConfig.startup");
  }

  @Produces
  @ApplicationScoped
  @Named(SQS_DB_PROCESSOR)
  SqsProcessor sqsDbProcessor(
      final JsonUtils jsonUtils, final SqsAsyncClient sqs, final DbSqsConfig config) {
    return new SqsProcessorWrapper(
        new green.zerolabs.commons.sqs.service.SqsProcessorImpl(
            jsonUtils, sqs, config.url(), config.delaySeconds()));
  }

  @Produces
  @ApplicationScoped
  @Named(SQS_W3_PROCESSOR)
  SqsProcessor sqsW3Processor(
      final JsonUtils jsonUtils, final SqsAsyncClient sqs, final W3SqsConfig config) {
    return new SqsProcessorWrapper(
        new green.zerolabs.commons.sqs.service.SqsProcessorImpl(
            jsonUtils, sqs, config.url(), config.delaySeconds()));
  }

  @Produces
  @ApplicationScoped
  @Named(SQS_W3_RETRY_PROCESSOR)
  SqsProcessor sqsW3RetryProcessor(
      final JsonUtils jsonUtils, final SqsAsyncClient sqs, final W3RetrySqsConfig config) {
    return new SqsProcessorWrapper(
        new green.zerolabs.commons.sqs.service.SqsProcessorImpl(
            jsonUtils, sqs, config.url(), config.delaySeconds()));
  }

  @Produces
  @ApplicationScoped
  @Named(SQS_S3_PROCESSOR)
  SqsProcessor sqsS3Processor(
      final JsonUtils jsonUtils, final SqsAsyncClient sqs, final S3SqsConfig config) {
    return new SqsProcessorWrapper(
        new green.zerolabs.commons.sqs.service.SqsProcessorImpl(
            jsonUtils, sqs, config.url(), config.delaySeconds()));
  }

  @Produces
  @ApplicationScoped
  @Named(SQS_IPFS_RETRY_PROCESSOR)
  SqsProcessor sqsS3RetryProcessor(
      final JsonUtils jsonUtils, final SqsAsyncClient sqs, final S3RetrySqsConfig config) {
    return new SqsProcessorWrapper(
        new green.zerolabs.commons.sqs.service.SqsProcessorImpl(
            jsonUtils, sqs, config.url(), config.delaySeconds()));
  }

  @Produces
  @ApplicationScoped
  @Named(SQS_DB_WITH_DUPLICATION_PROCESSOR)
  SqsProcessor sqsDbWithDuplicationProcessor(
      final JsonUtils jsonUtils,
      final SqsAsyncClient sqs,
      final DbWithDuplicationSqsConfig config) {
    return new SqsProcessorWrapper(
        new green.zerolabs.commons.sqs.service.SqsProcessorImpl(
            jsonUtils, sqs, config.url(), config.delaySeconds()));
  }
}
