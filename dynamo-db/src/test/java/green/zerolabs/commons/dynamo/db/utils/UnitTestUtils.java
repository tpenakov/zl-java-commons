package green.zerolabs.commons.dynamo.db.utils;

import static green.zerolabs.commons.dynamo.db.model.Constants.TABLE_NAME;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.apache.shiro.service.impl.CryptoServiceImpl;
import green.zerolabs.commons.core.model.CoreConstants;
import green.zerolabs.commons.core.service.CryptoService;
import green.zerolabs.commons.core.service.web3.impl.ZlW3BlockchainPropertiesRsWrapperImpl;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.GraphQlWrapper;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import green.zerolabs.commons.dynamo.db.service.DynamoDbFinderFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Getter
@Setter
@Slf4j
public class UnitTestUtils {
  public static final String PK =
      "0xf304b49aabfcb58adffecf28afff5dfa1cc0d44fb9415c248519eee992f3d80c";
  public static final String MNEMONIC_PHRASE =
      "lamp certain myself clog man student wire rubber hand crucial supreme carpet rose consider biology pole despair dentist emotion endorse high athlete birth area";

  public static final String BLOCKCHAIN_ADDRESS = "0xd173313a51f8fc37bcf67569b463abd89d81844f";

  public static final Function<InputStream, String> READ_TEXT_FILE_FN =
      Unchecked.<InputStream, String>function(s -> IOUtils.toString(s, StandardCharsets.UTF_8));

  private final AllUtils allUtils;
  private final DynamoDbFinder pkFinder;
  private final DynamoDbFinder textFinder;
  private final DynamoDbFinder numericFinder;

  protected UnitTestUtils() {
    final ObjectMapper objectMapper = new ObjectMapper();
    final JsonUtils jsonUtils = new JsonUtils(objectMapper);
    final GraphQlWrapper graphQlWrapper = new GraphQlWrapper(jsonUtils);
    final ConverterUtils converterUtils = new ConverterUtils();
    final CryptoService cryptoService = new CryptoServiceImpl(CoreConstants.SOME_SECRET_VALUE);
    final DynamoDbUtils dynamoDbUtils =
        Mockito.spy(
            new DynamoDbUtils(
                jsonUtils,
                converterUtils,
                Mockito.mock(DynamoDbAsyncClient.class),
                TABLE_NAME));
    allUtils =
        Mockito.spy(
            new AllUtils(
                dynamoDbUtils,
                graphQlWrapper,
                converterUtils,
                jsonUtils,
                objectMapper,
                new ZlW3BlockchainPropertiesRsWrapperImpl(cryptoService)));
    final DynamoDbFinderFactory dynamoDbFinderFactory = Mockito.spy(DynamoDbFinderFactory.class);
    pkFinder = Mockito.spy(new DynamoDbFinder(dynamoDbUtils));
    Mockito.doReturn(pkFinder).when(dynamoDbFinderFactory).createPkFinder();
    numericFinder = Mockito.spy(new DynamoDbFinder(ZlDbItem.GSI_NUMERIC_NAME, dynamoDbUtils));
    Mockito.doReturn(numericFinder).when(dynamoDbFinderFactory).createNumericFinder();
    textFinder = Mockito.spy(new DynamoDbFinder(ZlDbItem.GSI_NAME, dynamoDbUtils));
    Mockito.doReturn(textFinder).when(dynamoDbFinderFactory).createTextFinder();
  }

  public static UnitTestUtils of() {
    return new UnitTestUtils();
  }

  public Boolean hasAwsCredentials() {
    return Optional.ofNullable(System.getProperty("aws.secretAccessKey"))
        .map(s -> StringUtils.isNotBlank(s))
        .orElse(false);
  }

  public DynamoDbUtils getDynamoDbUtils() {
    return getAllUtils().getDynamoDbUtils();
  }

  public void waitFor(final Duration duration) {
    Multi.createFrom().ticks().every(duration).skip().first().toUni().await().indefinitely();
  }

  public <T> T readFromFile(final String path, final Class<T> clazz) {
    final String data =
        READ_TEXT_FILE_FN.apply(getClass().getClassLoader().getResourceAsStream(path));
    return getJsonUtils().readValue(data, clazz).get();
  }

  private JsonUtils getJsonUtils() {
    return getAllUtils().getJsonUtils();
  }

  public <T> T readJson(final String file, final TypeReference<T> typeRef) {
    try {
      final InputStream resourceAsStream = getClass().getResourceAsStream(file);
      return getJsonUtils().getObjectMapper().readValue(resourceAsStream, typeRef);
    } catch (final IOException e) {
      Assertions.fail(e);
      throw new RuntimeException(e);
    }
  }
}
