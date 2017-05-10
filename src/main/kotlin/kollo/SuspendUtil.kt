package kollo

import com.spotify.apollo.RequestContext
import com.spotify.apollo.route.AsyncHandler
import com.spotify.apollo.route.Route
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun <T> CompletionStage<T>.await(): T {
    return suspendCoroutine { cont: Continuation<T> ->
        whenComplete { result, exception ->
            if (exception == null) {
                cont.resume(result)
            } else {
                cont.resumeWithException(exception)
            }
        }
    }
}

object SuspendRoute {
    fun <T> async(
            method: String, uri: String, handler: suspend (context: RequestContext) -> T): Route<AsyncHandler<T>> {
        return Route.async(method, uri, { context: RequestContext ->
            val responseFuture = CompletableFuture<T>()
            launch(CommonPool) {
                try {
                    val response = handler.invoke(context)
                    responseFuture.complete(response)
                } catch (t: Throwable) {
                    responseFuture.completeExceptionally(t)
                }
            }
            responseFuture
        })
    }
}
