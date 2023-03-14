package green.zerolabs.commons.dynamo.db.exception;

import green.zerolabs.commons.core.error.GraphqlExpectedException;
import lombok.Getter;

@Getter
public class InvalidPaginationTokenException extends GraphqlExpectedException {

  public InvalidPaginationTokenException(final Throwable cause, final String token) {
    super("Invalid pagination token: " + token, cause);
  }
}
