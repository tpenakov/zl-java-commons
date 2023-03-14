package green.zerolabs.commons.core.error;

public class GraphqlExpectedException extends RuntimeException {

  public GraphqlExpectedException(final String message) {
    super(message);
  }

  public GraphqlExpectedException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
