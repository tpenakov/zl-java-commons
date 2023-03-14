package green.zerolabs.commons.core.validation;

import green.zerolabs.commons.core.model.ZlSqsItem;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/***
 * Created by Triphon Penakov 2022-11-02
 */
@SuppressWarnings("unused")
public interface UploadJsonDataRqItemValidator<T> extends GraphQlValidator<T> {
  Collection<UploadJsonDataRqItemValidator> VALIDATORS = new ConcurrentLinkedQueue<>();

  boolean isSupported(final ZlSqsItem.Store rq);

  List<T> getItems(final ZlSqsItem.Store rq);
}
