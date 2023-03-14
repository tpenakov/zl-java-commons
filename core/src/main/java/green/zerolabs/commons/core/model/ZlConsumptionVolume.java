package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

/*
 * Created by Triphon Penakov 2022-09-01
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlConsumptionVolume {
  private BigDecimal quantity;
  @Builder.Default private ConsumptionUnit unit = ConsumptionUnit.Wh;
}
