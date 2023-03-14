package green.zerolabs.commons.sqs.service;

import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsProcessor;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

/*
 * Created by triphon 21.06.22 г.
 */
@Getter(AccessLevel.PROTECTED)
public class SqsProcessorWrapper implements SqsProcessor {

  private final SqsProcessor processor;

  public SqsProcessorWrapper(final SqsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Uni<String> send(final ZlSqsItem data) {
    return processor.send(data);
  }

  @Override
  public Uni<String> send(final ZlSqsItem data, final Integer delayInSeconds) {
    return processor.send(data, delayInSeconds);
  }

  @Override
  public Uni<List<ZlSqsItem>> receive() {
    return processor.receive();
  }
}
