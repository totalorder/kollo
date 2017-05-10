package kollo
import com.spotify.apollo.Environment
import com.spotify.apollo.RequestContext
import com.spotify.apollo.Response
import com.spotify.apollo.core.Service
import com.spotify.apollo.httpservice.HttpService
import com.spotify.apollo.route.Route
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


object App {
    val scheduledExecutorFuture = CompletableFuture<ScheduledExecutorService>()

    @JvmStatic fun main(args: Array<String>) {
        val service = HttpService.usingAppInit(this::init, "kollo").build()

        HttpService.boot(
                service,
                { instance: Service.Instance ->
                    scheduledExecutorFuture.complete(instance.scheduledExecutorService) },
                args)
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
        scheduledExecutorFuture.get().schedule({ future.complete(millis) }, millis, TimeUnit.MILLISECONDS)
        return future
    }
}
