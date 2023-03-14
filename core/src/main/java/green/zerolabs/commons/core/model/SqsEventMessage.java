package green.zerolabs.commons.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;

/*
 * Created by triphon 26.02.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class SqsEventMessage implements Serializable {

  private static final long serialVersionUID = 8090872265600867801L;
  public static final String RECORDS = "Records";

  @JsonProperty(RECORDS)
  private List<Record> records;

  @Builder
  @Data
  @Jacksonized
  public static class Record implements Serializable {
    private static final long serialVersionUID = -7846286036047212216L;
    public static final String MESSAGE_ID = "messageId";
    public static final String EVENT_SOURCE = "eventSource";
    public static final String AWS_SQS = "aws:sqs";

    private String messageId;
    private String receiptHandle;
    private String body;
    private Object attributes;
    private Object messageAttributes;
    private String md5OfBody;
    private String eventSource;
    private String eventSourceARN;
    private String awsRegion;
  }
}
