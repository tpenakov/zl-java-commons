package green.zerolabs.commons.lambda.triggers.emulator.service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/***
 * Created by Triphon Penakov 2023-02-13
 */
@Path("/2015-03-31/functions/function/invocations")
public interface LambdaTriggerService {

  @POST
  CompletionStage<String> send(Map<String, Object> data);
}
