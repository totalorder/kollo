import com.spotify.apollo.Request
import com.spotify.apollo.RequestContext
import com.spotify.apollo.request.RequestContexts
import com.spotify.apollo.request.RequestMetadataImpl
import com.spotify.apollo.test.StubClient
import kollo.App
import kollo.JavaHandler
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.*

class AppTest {
    @Test
    fun regularHandler() {
        val response = App.regularHandler(createRequestContext())
        assertEquals("Hello 0", response.toCompletableFuture().get().payload().get())
    }

    @Test
    fun suspendHandler() = runBlocking {
        val response = App.suspendHandler(createRequestContext())
        assertEquals("Hello 0", response.payload().get())
    }

    @Test
    fun javaHandler() = runBlocking {
        val response = JavaHandler.javaHandler(createRequestContext())
        assertEquals("Hello 0", response.toCompletableFuture().get().payload().get())
    }

    private fun createRequestContext(pathArgs: Map<String, String> = mapOf()) = RequestContexts.create(
            Request.forUri("hm://dummy-uri/"),
            StubClient(),
            pathArgs,
            0,
            RequestMetadataImpl.create(
                    Instant.EPOCH,
                    Optional.empty(),
                    Optional.empty()))!!
}
