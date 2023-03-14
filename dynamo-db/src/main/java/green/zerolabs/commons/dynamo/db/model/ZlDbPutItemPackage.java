package green.zerolabs.commons.dynamo.db.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/*
 * Created by triphon 3.05.22 г.
 */
@Builder
@Data
@Jacksonized
public class ZlDbPutItemPackage {
  private ZlDbItem item;
  private String condition;
  private Map<String, String> conditionNames;
  private Map<String, AttributeValue> conditionValues;
  private Boolean runFirst;
}
