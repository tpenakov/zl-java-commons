package green.zerolabs.commons.lambda.triggers.emulator;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
/***
 * Created by Triphon Penakov 2023-02-10
 */
public class GreetingResource {
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    return "Hello from RESTEasy Reactive";
  }
}
