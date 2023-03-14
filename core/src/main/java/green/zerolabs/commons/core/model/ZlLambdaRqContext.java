package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

/*
 * Created by triphon 15.06.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlLambdaRqContext implements Serializable {
  String requestId;
}
