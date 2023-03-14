package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/*
 * Created by triphon 6.03.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlInternalData implements Serializable {
  private static final long serialVersionUID = -5243477122866635282L;

  public static final String LOCK_VERSION = "lockVersion";

  public enum Status {
    DRAFT,
    OK,
    ERROR
  }

  @Builder.Default private Status status = Status.DRAFT;
  @Builder.Default private Instant createdDate = Instant.now();
  @Builder.Default private Instant updatedDate = Instant.now();
  @Builder.Default private Long lockVersion = 0L;
  private List<String> errors;
}
