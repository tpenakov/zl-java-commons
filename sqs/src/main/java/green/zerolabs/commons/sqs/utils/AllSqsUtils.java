package green.zerolabs.commons.sqs.utils;

import green.zerolabs.commons.core.model.SqsEventMessage;
import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsEventProcessor;
import green.zerolabs.commons.core.service.SqsProcessor;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.core.utils.MutinyUtils;
import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/*
 * Created by triphon 16.06.22 г.
 */
@Getter
@Setter
@Slf4j
public class AllSqsUtils {
  private final JsonUtils jsonUtils;

  @Getter(AccessLevel.PRIVATE)
  private final Collection<SqsEventProcessor> sqsEventProcessors = new ConcurrentLinkedQueue<>();

  public AllSqsUtils(final JsonUtils jsonUtils) {
    this.jsonUtils = jsonUtils;
  }

  public Stream<SqsEventProcessor> getProcessors() {
    return getSqsEventProcessors().stream();
  }

  public void addProcessor(final SqsEventProcessor processor) {
    getSqsEventProcessors().add(processor);
  }

  public Uni<String> sendWithRandomGroupId(
      final SqsProcessor sqsProcessor, final ZlSqsItem zlSqsItem) {
    final Context context =
        Context.from(Map.of(SqsProcessor.FIFO_GROUP_ID, UUID.randomUUID().toString()));
    return sendWithRandomGroupId(sqsProcessor, zlSqsItem, context);
  }

  public Uni<String> sendWithRandomGroupId(
      final SqsProcessor sqsProcessor, final ZlSqsItem zlSqsItem, final Context context) {
    return MutinyUtils.runWithContext(
        () -> sqsProcessor.send(zlSqsItem), new AtomicReference<>(context));
  }

  public SqsEventMessage toSqsEventMessage(final ZlSqsItem sqsItem) {
    return SqsEventMessage.builder()
        .records(
            List.of(
                SqsEventMessage.Record.builder()
                    .messageId(sqsItem.getMessageId())
                    .receiptHandle(sqsItem.getReceiptHandle())
                    .eventSource(SqsEventMessage.Record.AWS_SQS)
                    .body(getJsonUtils().toStringLazy(sqsItem).toString())
                    .build()))
        .build();
  }
}
