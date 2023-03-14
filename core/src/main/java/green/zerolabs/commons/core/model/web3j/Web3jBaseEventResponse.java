package green.zerolabs.commons.core.model.web3j;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/*
 * Created by triphon 27.07.22 г.
 */
@Data
@NoArgsConstructor
@RegisterForReflection
public class Web3jBaseEventResponse {
  private static final long serialVersionUID = -8518108076530932049L;

  private Instant creationDateTime = Instant.now();
  private Web3jLog log;
}
