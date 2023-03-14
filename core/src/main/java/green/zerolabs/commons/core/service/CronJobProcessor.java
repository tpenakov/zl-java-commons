package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import io.smallrye.mutiny.Uni;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Created by triphon 27.02.22 г.
 */
public interface CronJobProcessor {

  Collection<CronJobProcessor> PROCESSORS = new ConcurrentLinkedQueue<>();

  Uni<Boolean> handle(final ZlLambdaRqContext context);
}
