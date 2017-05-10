package kollo
import com.google.common.util.concurrent.ListeningScheduledExecutorService
import com.spotify.apollo.Environment
import com.spotify.apollo.RequestContext
import com.spotify.apollo.Response
import com.spotify.apollo.httpservice.HttpService
import com.spotify.apollo.route.Route
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit


object App {
    private val LOG = LoggerFactory.getLogger(App::class.java)

    @JvmStatic fun main(args: Array<String>) {
        val service = HttpService.usingAppInit(this::init, "kollo").build()

        HttpService.boot(service, { instance ->
            this.scheduledExecutor = instance.scheduledExecutorService
        }, args)
    }

    private var scheduledExecutor: ListeningScheduledExecutorService? = null

    private fun init(environment: Environment) {
        environment.routingEngine()
                .registerAutoRoute(Route.async("GET", "/regular", this::regularHandler))
                .registerAutoRoute(Route.async("GET", "/asfuture", this::asFutureHandler))
                .registerAutoRoute(SuspendRoute.async("GET", "/suspended", { rq -> suspendHandler(rq) }))
    }

    fun asFutureHandler(context: RequestContext) = asFuture<Response<String>> {
        val slept = sleepSuspend(1)
        Response.forPayload("Hello " + slept)
    }

    suspend fun suspendHandler(context: RequestContext): Response<String> {
        val slept = sleepSuspend(1)
        return Response.forPayload("Hello " + slept)
    }

    fun regularHandler(context: RequestContext): CompletionStage<Response<String>> {
        return sleep(1).thenApply { slept ->
            Response.forPayload("Hello " + slept)
        }
    }

    suspend fun sleepSuspend(millis: Long) = await(sleep(millis))

    fun sleep(millis: Long): CompletionStage<Long> {
        val future = CompletableFuture<Long>()
        scheduledExecutor?.schedule({ future.complete(millis) }, millis, TimeUnit.MILLISECONDS)
        return future
    }
}
