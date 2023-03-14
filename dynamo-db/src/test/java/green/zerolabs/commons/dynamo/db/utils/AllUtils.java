package green.zerolabs.commons.dynamo.db.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.service.web3.ZlW3BlockchainPropertiesRsWrapper;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.GraphQlWrapper;
import green.zerolabs.commons.core.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Getter
@Slf4j
public class AllUtils {
  private final DynamoDbUtils dynamoDbUtils;
  private final GraphQlWrapper graphQlWrapper;
  private final ConverterUtils converterUtils;
  private final JsonUtils jsonUtils;
  private final ObjectMapper objectMapper;
  private final ZlW3BlockchainPropertiesRsWrapper zlW3BlockchainPropertiesRsWrapper;

  public AllUtils(
      final DynamoDbUtils dynamoDbUtils,
      final GraphQlWrapper graphQlWrapper,
      final ConverterUtils converterUtils,
      final JsonUtils jsonUtils,
      final ObjectMapper objectMapper,
      final ZlW3BlockchainPropertiesRsWrapper zlW3BlockchainPropertiesRsWrapper) {
    this.dynamoDbUtils = dynamoDbUtils;
    this.graphQlWrapper = graphQlWrapper;
    this.converterUtils = converterUtils;
    this.jsonUtils = jsonUtils;
    this.objectMapper = objectMapper;
    this.zlW3BlockchainPropertiesRsWrapper = zlW3BlockchainPropertiesRsWrapper;
  }
}
