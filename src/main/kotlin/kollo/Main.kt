package kollo
import com.spotify.apollo.Environment
import com.spotify.apollo.RequestContext
import com.spotify.apollo.Response
import com.spotify.apollo.core.Service
import com.spotify.apollo.httpservice.HttpService
import com.spotify.apollo.route.Route
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object App {
    val scheduledExecutor = Executors.newScheduledThreadPool(10)!!

    @JvmStatic fun main(args: Array<String>) {
        HttpService.boot(this::init, "kollo", args)
    }

    private fun init(environment: Environment) {
        environment.routingEngine()
                .registerAutoRoute(Route.async("GET", "/regular", { rc -> regularHandler(rc) }))
                .registerAutoRoute(SuspendRoute.async("GET", "/suspended", { rc -> suspendHandler(rc) }))
    }

    suspend fun suspendHandler(requestContext: RequestContext): Response<String> {
        val sleepTime = requestContext.request().parameter("sleep").map { it.toLong() }.orElse(0)
        val slept = sleep(sleepTime).await()
        return Response.forPayload("Hello " + slept)
    }

    fun regularHandler(requestContext: RequestContext): CompletionStage<Response<String>> {
        val sleepTime = requestContext.request().parameter("sleep").map { it.toLong() }.orElse(0)
        return sleep(sleepTime).thenApply { slept ->
            Response.forPayload("Hello " + slept)
        }
    }

    fun sleep(millis: Long): CompletionStage<Long> {
        val future = CompletableFuture<Long>()
        scheduledExecutor.schedule({ future.complete(millis) }, millis, TimeUnit.MILLISECONDS)
        return future
    }
}
