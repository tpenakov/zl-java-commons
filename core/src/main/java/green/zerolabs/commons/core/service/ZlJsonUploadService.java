package green.zerolabs.commons.core.service;

import io.smallrye.mutiny.Uni;

import java.io.Serializable;
import java.util.List;

/*
 * Created by triphon 28.04.22 г.
 */
public interface ZlJsonUploadService<T extends Serializable> extends SqsEventProcessor {

  Uni<T> findByUploadId(String id);

  Class<T> getModelClass();

  List<String> splitToList(final String input);
}
