package kollo;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;

import java.util.concurrent.CompletionStage;

public class JavaHandler {
  public static CompletionStage<Response<String>> javaHandler(RequestContext requestContext) {
    long sleepTime = requestContext.request().parameter("sleep").map(Long::parseLong)
        .orElse(0L);
    return App.INSTANCE.sleep(sleepTime).thenApply(slept ->
        Response.forPayload("Hello "  + slept));
  }
}
