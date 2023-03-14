package green.zerolabs.commons.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.error.GraphqlExpectedException;
import green.zerolabs.commons.core.model.GraphQlRequest;
import green.zerolabs.commons.core.model.graphql.generated.ConsumptionEntityRq;
import green.zerolabs.commons.core.model.graphql.generated.ErrorResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Slf4j
public class GraphQlWrapperTest {

  public static final String USERNAME = "user1";
  private GraphQlWrapper wrapper;

  private GraphQlRequest request;

  @BeforeEach
  protected void beforeEach() {
    wrapper = new GraphQlWrapper(UnitTestUtils.of().getJsonUtils());
    request = createDummyRequest();
  }

  @Test
  public void getUsernameTest() {
    final Optional<String> result = wrapper.getUsername(request);
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(USERNAME, result.get());
  }

  @Test
  public void getUsernameNullIdentityTest() {
    request.setIdentity(null);
    final Optional<String> result = wrapper.getUsername(request);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void getUsernameNullUsernameTest() {
    request.getIdentity().setUsername(null);
    final Optional<String> result = wrapper.getUsername(request);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void getUsernameEmptyUsernameTest() {
    request.getIdentity().setUsername(" ");
    final Optional<String> result = wrapper.getUsername(request);
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void getBodyTest() {
    final Optional<Object> result = wrapper.getBody(request, map -> map.get("arg1"));
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals("test1", result.get());
  }

  @Test
  public void getBodyTest2() {
    final Optional<Object> result = wrapper.getBody(request, map -> map.get("arg3"));
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void getBodyNullRequestTest() {
    final Optional<Object> result = wrapper.getBody(null, map -> map.get("arg1"));
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void getBodyNullArgumentsTest() {
    request.setArguments(null);
    final Optional<Object> result = wrapper.getBody(request, map -> map.get("arg1"));
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  public void getBodyArgumentTest() {
    final Optional<ConsumptionEntityRq> result =
        wrapper.getBody(request, "body", ConsumptionEntityRq.class);
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals("id-1", result.get().getId());
  }

  @Test
  void handleExpectedExceptionTest() {
    final ErrorResponse response =
        wrapper.handleError(new DummyException("dummy message"), new DummyResponse());

    Assertions.assertEquals("dummy message", response.getErrors().get(0));
    Assertions.assertTrue(response.getIsError());
  }

  @Test
  void handleNonExpectedExceptionTest() {
    final ErrorResponse response =
        wrapper.handleError(new RuntimeException("dummy message"), new DummyResponse());

    Assertions.assertNotEquals("dummy message", response.getErrors().get(0));
    Assertions.assertTrue(response.getIsError());
  }

  @Test
  void inputToRqTest() {
    Assertions.assertEquals(
        USERNAME,
        GraphQlWrapper.INPUT_TO_RQ_FN
            .apply(getObjectMapper(), getJsonUtils().toMap(getRequest()))
            .getIdentity()
            .getUsername());
  }

  private ObjectMapper getObjectMapper() {
    return getJsonUtils().getObjectMapper();
  }

  private JsonUtils getJsonUtils() {
    return getWrapper().getJsonUtils();
  }

  private static class DummyException extends GraphqlExpectedException {
    public DummyException(final String message) {
      super(message);
    }

    public DummyException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

  private static class DummyResponse implements ErrorResponse {

    private Boolean isError;

    private List<String> errors;

    @Override
    public Boolean getIsError() {
      return isError;
    }

    @Override
    public void setIsError(final Boolean isError) {
      this.isError = isError;
    }

    @Override
    public List<String> getErrors() {
      return errors;
    }

    @Override
    public void setErrors(final List<String> errors) {
      this.errors = errors;
    }
  }

  private GraphQlRequest createDummyRequest() {
    return GraphQlRequest.builder()
        .arguments(Map.of("arg1", "test1", "arg2", "test2", "body", Map.of("id", "id-1")))
        .identity(GraphQlRequest.Identity.builder().username(USERNAME).build())
        .build();
  }
}
