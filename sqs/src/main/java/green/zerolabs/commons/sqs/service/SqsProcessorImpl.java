package green.zerolabs.commons.sqs.service;

import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsProcessor;
import green.zerolabs.commons.core.utils.JsonUtils;
import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * Created by triphon 15.05.22 г.
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class SqsProcessorImpl implements SqsProcessor {

  private final JsonUtils jsonUtils;
  private final SqsAsyncClient sqs;
  private final String queueUrl;
  private final Boolean isFifoQueue;
  private final Boolean isRequireRandomMessageDeduplicationId;
  private final Integer queueDelay;

  public SqsProcessorImpl(
      final JsonUtils jsonUtils,
      final SqsAsyncClient sqs,
      final String queueUrl,
      final Integer queueDelay) {
    this.jsonUtils = jsonUtils;
    this.sqs = sqs;
    this.queueUrl = queueUrl;
    this.queueDelay = queueDelay;
    // log.info("queueUrl: {}", queueUrl);
    isFifoQueue = queueUrl.endsWith(".fifo");
    isRequireRandomMessageDeduplicationId = isFifoQueue && queueUrl.contains("with-duplication");
  }

  @Override
  public Uni<String> send(final ZlSqsItem data) {
    return send(data, null);
  }

  @Override
  public Uni<String> send(final ZlSqsItem data, final Integer delayInSeconds) {
    data.setMessageId(null);
    data.setReceiptHandle(null);
    return Uni.createFrom()
        .context(context -> sendToQueue(data, delayInSeconds, context))
        .onFailure()
        .retry()
        .atMost(5)
        .map(sendMessageResponse -> sendMessageResponse.messageId());
  }

  @Override
  public Uni<List<ZlSqsItem>> receive() {
    return Uni.createFrom()
        .completionStage(getSqs().receiveMessage(builder -> builder.queueUrl(getQueueUrl())))
        .onItem()
        .ifNull()
        .continueWith(ReceiveMessageResponse.builder().messages(List.of()).build())
        .map(
            receiveMessageResponse ->
                Optional.ofNullable(receiveMessageResponse.messages()).stream()
                    .flatMap(Collection::stream)
                    .map(
                        message ->
                            Optional.ofNullable(message.body())
                                .flatMap(body -> getJsonUtils().readValue(body, ZlSqsItem.class))
                                .map(
                                    zlSqsItem -> {
                                      zlSqsItem.setMessageId(message.messageId());
                                      zlSqsItem.setReceiptHandle(message.receiptHandle());
                                      return zlSqsItem;
                                    }))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
  }

  private Uni<SendMessageResponse> sendToQueue(
      final ZlSqsItem data, final Integer delayInSeconds, final Context context) {
    return Uni.createFrom()
        .completionStage(
            () ->
                getSqs()
                    .sendMessage(
                        builder ->
                            builder
                                .queueUrl(getQueueUrl())
                                .messageBody(getJsonUtils().toStringLazy(data).toString())
                                .messageGroupId(getMessageGroupId(context))
                                .messageDeduplicationId(
                                    isRequireRandomMessageDeduplicationId
                                        ? UUID.randomUUID().toString()
                                        : null)
                                .delaySeconds(
                                    getIsFifoQueue()
                                        ? null
                                        : Optional.ofNullable(delayInSeconds)
                                            .orElseGet(() -> getQueueDelay()))));
  }

  String getMessageGroupId(final Context context) {
    return getIsFifoQueue() ? context.getOrElse(FIFO_GROUP_ID, () -> FIFO_GROUP_ID) : null;
  }
}
