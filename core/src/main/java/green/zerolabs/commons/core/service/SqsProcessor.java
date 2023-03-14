package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.ZlSqsItem;
import io.smallrye.mutiny.Uni;

import java.util.List;

/*
 * Created by triphon 15.05.22 г.
 */
public interface SqsProcessor {
  String FIFO_GROUP_ID = "fifoGroupId";

  Uni<String> send(ZlSqsItem data);

  Uni<String> send(ZlSqsItem data, Integer delayInSeconds);

  Uni<List<ZlSqsItem>> receive();
}
