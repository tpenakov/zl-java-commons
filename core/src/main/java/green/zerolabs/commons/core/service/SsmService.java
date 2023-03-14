package green.zerolabs.commons.core.service;

import io.smallrye.mutiny.Uni;

/*
 * Created by triphon 15.05.22 г.
 */
public interface SsmService {
  Uni<String> getEncryptionKey();
}
