package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.GraphQlRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class GraphQlProcessorTest {

  GraphQlProcessor graphQlProcessor;

  @BeforeEach
  void beforeEach() {
    graphQlProcessor = Mockito.spy(GraphQlProcessor.class);
  }

  @Test
  void processorsTest() {
    Assertions.assertTrue(graphQlProcessor.PROCESSORS.isEmpty());
  }

  @Test
  void getUserName() {
    final GraphQlRequest request =
        GraphQlRequest.builder()
            .identity(GraphQlRequest.Identity.builder().username("user-1").build())
            .build();
    final Optional<String> result = graphQlProcessor.getUserName(request);

    Assertions.assertEquals(Optional.of("user-1"), result);
  }
}
