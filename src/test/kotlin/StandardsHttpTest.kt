import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.modelcontextprotocol.kotlin.sdk.client.mcpStreamableHttp
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val ALLOWED_HOST = "localhost"

class StandardsHttpTest {
    @TempDir
    lateinit var root: File

    @Test
    fun `should answer ok when liveness probe is called`() =
        testApplication {
            // given
            serveStandards()

            // when
            val result = client.get(LIVE_PATH)

            // then
            result.status shouldBe HttpStatusCode.OK
        }

    @Test
    fun `should answer ok when readiness probe is called`() =
        testApplication {
            // given
            serveStandards()

            // when
            val result = client.get(READY_PATH)

            // then
            result.status shouldBe HttpStatusCode.OK
        }

    @Test
    fun `should list standards when list tool is called over http`() =
        testApplication {
            // given
            serveStandards()
            val mcp = createClient { install(SSE) }.mcpStreamableHttp(MCP_PATH) { header(HttpHeaders.Host, ALLOWED_HOST) }

            // when
            val result = mcp.callTool(LIST_TOOL, emptyMap())

            // then
            result.text() shouldContain "- kotlin: Kotlin"
        }

    @Test
    fun `should reject mcp request when host is not allowed`() =
        testApplication {
            // given
            serveStandards()

            // when
            val result = client.post(MCP_PATH) { header(HttpHeaders.Host, "evil.example") }

            // then
            result.status shouldBe HttpStatusCode.Forbidden
        }

    private fun ApplicationTestBuilder.serveStandards() {
        val dir =
            root.resolve("standards").apply {
                mkdirs()
                resolve("kotlin.md").writeText("# Kotlin\nPrefer val over var")
            }
        application { standardsHttp(Standards(dir), listOf(ALLOWED_HOST)) }
    }
}
