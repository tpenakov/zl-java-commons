package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/*
 * Created by triphon 13.03.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class GraphQlRequest implements Serializable {
  private static final long serialVersionUID = -3329574905750882583L;

  private String typeName;
  private String fieldName;
  private Map<String, Object> arguments;
  private Prev prev;
  private Identity identity;
  private Request request;
  private Object source;

  @Builder
  @Data
  @Jacksonized
  public static class Request implements Serializable {
    private static final long serialVersionUID = -1522304555945348347L;

    private Map<String, Object> headers;
  }

  @Builder
  @Data
  @Jacksonized
  public static class Prev implements Serializable {
    private static final long serialVersionUID = 4418560183869140333L;

    private Map<String, Object> result;
  }

  @Builder
  @Data
  @Jacksonized
  public static class Identity implements Serializable {
    private static final long serialVersionUID = 7340537115308141762L;

    private String defaultAuthStrategy;
    private String issuer;
    private String sub;
    private String username;
    private List<String> sourceIp;
    private Map<String, Object> claims;
  }
}
