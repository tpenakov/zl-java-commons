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
public class S3EventMessage implements Serializable {

  private static final long serialVersionUID = -5000860776966545772L;
  public static final String RECORDS = "Records";

  @JsonProperty(RECORDS)
  private List<Record> records;

  @Builder
  @Data
  @Jacksonized
  public static class Record implements Serializable {

    private static final long serialVersionUID = -8360998830281543685L;

    public static final String S3_NAME = "s3";

    private String eventVersion;
    private String eventSource;
    private String awsRegion;
    private String eventTime;
    private String eventName;
    private S3 s3;

    @Builder
    @Data
    @Jacksonized
    public static class S3 implements Serializable {
      private static final long serialVersionUID = 3782814060507864091L;

      public static final String BUCKET = "bucket";

      private String s3SchemaVersion;
      private String configurationId;
      private Bucket bucket;
      private Object object;

      @Builder
      @Data
      @Jacksonized
      public static class Bucket implements Serializable {
        private static final long serialVersionUID = 1966895439708133308L;
        private String name;
        private String arn;
      }

      @Builder
      @Data
      @Jacksonized
      public static class Object implements Serializable {
        private static final long serialVersionUID = 374617876839494353L;
        private String key;
        private Integer size;
        private String eTag;
      }
    }
  }
}
