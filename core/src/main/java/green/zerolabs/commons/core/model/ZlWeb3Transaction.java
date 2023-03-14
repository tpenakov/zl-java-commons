package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.IdOnly;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

/***
 * Created by Triphon Penakov 2023-01-14
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlWeb3Transaction implements Serializable, IdOnly, ZlInternal {
  private static final long serialVersionUID = 209410392859788566L;

  public enum Status {
    INITIAL,
    IN_PROGRESS,
    COMPLETED,
    ERROR
  }

  private String id;
  private String hash;

  @Builder.Default private Status web3TransactionStatus = Status.INITIAL;

  private String error;

  // internal fields
  @Builder.Default
  private ZlInternalData internal =
      ZlInternalData.builder().status(ZlInternalData.Status.OK).build();
}
