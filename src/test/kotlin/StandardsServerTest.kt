import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.modelcontextprotocol.kotlin.sdk.ExperimentalMcpApi
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.testing.ChannelTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val KOTLIN_STANDARD = "kotlin"
private const val KOTLIN_TEXT = "# Kotlin\nPrefer val over var"

class StandardsServerTest {
    @TempDir
    lateinit var root: File

    @Test
    fun `should tell client to read standards when client connects`() =
        mcpTest {
            // given
            val client = connectedClient()

            // when
            val result = client.serverInstructions

            // then
            result.orEmpty() shouldContain LIST_TOOL
        }

    @Test
    fun `should list standards when list tool is called`() =
        mcpTest {
            // given
            val client = connectedClient()

            // when
            val result = client.callTool(LIST_TOOL, emptyMap())

            // then
            result.text() shouldBe "- $KOTLIN_STANDARD: Kotlin"
        }

    @Test
    fun `should return standard content when get tool is called with existing name`() =
        mcpTest {
            // given
            val client = connectedClient()

            // when
            val result = client.callTool(GET_TOOL, mapOf(NAME_ARGUMENT to KOTLIN_STANDARD))

            // then
            result.text() shouldBe KOTLIN_TEXT
            result.isError shouldBe null
        }

    @Test
    fun `should return error listing available standards when get tool is called with unknown name`() =
        mcpTest {
            // given
            val client = connectedClient()

            // when
            val result = client.callTool(GET_TOOL, mapOf(NAME_ARGUMENT to "missing"))

            // then
            result.isError shouldBe true
            result.text() shouldContain "- $KOTLIN_STANDARD: Kotlin"
        }

    @Test
    fun `should return error when get tool is called without name`() =
        mcpTest {
            // given
            val client = connectedClient()

            // when
            val result = client.callTool(GET_TOOL, emptyMap())

            // then
            result.isError shouldBe true
            result.text() shouldContain "Available"
        }

    private fun mcpTest(block: suspend CoroutineScope.() -> Unit) = runTest { withContext(Dispatchers.Default, block) }

    @OptIn(ExperimentalMcpApi::class)
    private suspend fun connectedClient(): Client {
        val dir =
            root.resolve("standards").apply {
                mkdirs()
                resolve("$KOTLIN_STANDARD.md").writeText(KOTLIN_TEXT)
            }
        val transports = ChannelTransport.createLinkedPair()
        standardsServer(Standards(dir)).createSession(transports.serverTransport)
        return Client(Implementation(name = "test", version = "1.0.0")).apply { connect(transports.clientTransport) }
    }

    private fun CallToolResult.text() = content.filterIsInstance<TextContent>().joinToString("\n") { it.text }
}
