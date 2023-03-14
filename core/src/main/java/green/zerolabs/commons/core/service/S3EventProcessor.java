package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.S3EventMessage;
import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import io.smallrye.mutiny.Uni;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Created by triphon 27.02.22 г.
 */
public interface S3EventProcessor {

  Collection<S3EventProcessor> PROCESSORS = new ConcurrentLinkedQueue<>();

  Boolean isSupported(S3EventMessage.Record input, byte[] data, final ZlLambdaRqContext context);

  Uni<Boolean> handle(S3EventMessage.Record input, byte[] data, final ZlLambdaRqContext context);

  default Collection<S3EventProcessor> getAllProcessors() {
    return PROCESSORS;
  }
}

