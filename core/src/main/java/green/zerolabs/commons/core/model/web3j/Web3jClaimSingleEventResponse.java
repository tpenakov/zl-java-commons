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
public class Web3jClaimSingleEventResponse extends Web3jBaseEventResponse {
  private static final long serialVersionUID = 2002893657769424259L;

  private String _claimIssuer;

  private String _claimSubject;

  private BigInteger _topic;

  private BigInteger _id;

  private BigInteger _value;

  private byte[] _claimData;
}
