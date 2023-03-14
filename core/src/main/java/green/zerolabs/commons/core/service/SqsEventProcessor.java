package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import green.zerolabs.commons.core.model.ZlSqsItem;
import io.smallrye.mutiny.Uni;

/*
 * Created by triphon 27.02.22 г.
 */
public interface SqsEventProcessor {

  Boolean isSupported(ZlSqsItem input, final ZlLambdaRqContext context);

  Uni<Boolean> handle(ZlSqsItem item, final ZlLambdaRqContext context);
}
