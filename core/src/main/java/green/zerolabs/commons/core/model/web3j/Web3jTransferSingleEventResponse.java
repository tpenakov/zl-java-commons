package green.zerolabs.commons.core.model.web3j;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.math.BigInteger;

/*
 * Created by triphon 27.07.22 г.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@RegisterForReflection
public class Web3jTransferSingleEventResponse extends Web3jBaseEventResponse {
  private static final long serialVersionUID = -7069538378919614035L;

  private String operator;

  private String from;

  private String to;

  private BigInteger id;

  private BigInteger value;
}
