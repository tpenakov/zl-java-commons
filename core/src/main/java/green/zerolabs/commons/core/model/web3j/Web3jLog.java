package green.zerolabs.commons.core.model.web3j;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * Created by triphon 27.07.22 г.
 */
@Data
@NoArgsConstructor
@RegisterForReflection
public class Web3jLog {
  private static final long serialVersionUID = -4328280921178924552L;

  private boolean removed;
  private String logIndex;
  private String transactionIndex;
  private String transactionHash;
  private String blockHash;
  private String blockNumber;
  private String address;
  private String data;
  private String type;
  private List<String> topics;
}
