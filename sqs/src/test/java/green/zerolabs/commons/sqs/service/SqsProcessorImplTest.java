package green.zerolabs.commons.sqs.service;

import io.smallrye.mutiny.Context;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static green.zerolabs.commons.core.service.SqsProcessor.FIFO_GROUP_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/***
 * Created by Triphon Penakov 2023-02-06
 */
class SqsProcessorImplTest {

  @Test
  void getMessageGroupId_WhenFifoQueue_OkTest() {
    final String groupId = "groupId";
    final SqsProcessorImpl sqsProcessor = new SqsProcessorImpl(null, null, "any.fifo", 0);
    assertEquals(
        groupId, sqsProcessor.getMessageGroupId(Context.from(Map.of(FIFO_GROUP_ID, groupId))));
    assertEquals(FIFO_GROUP_ID, sqsProcessor.getMessageGroupId(Context.empty()));
  }
}
