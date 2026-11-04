import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Job
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.File

private const val LOGGING_BANNER_PROPERTY = "kotlin-logging.logStartupMessage"
private const val SERVER_NAME = "standards"
private const val SERVER_VERSION = "1.0.0"
private const val LIST_TOOL = "list_standards"
private const val GET_TOOL = "get_standard"
private const val NAME_ARGUMENT = "name"

suspend fun main(args: Array<String>) {
    System.setProperty(LOGGING_BANNER_PROPERTY, "false")
    val dir = File(requireNotNull(args.firstOrNull()) { "Usage: server-mcp-kotlin <standards-dir>" })
    require(dir.isDirectory) { "Not a directory: $dir" }
    val standards = Standards(dir)

    val server =
        Server(
            Implementation(name = SERVER_NAME, version = SERVER_VERSION),
            ServerOptions(ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
            instructions =
                "User's coding standards. Before writing, reviewing or committing code, call $LIST_TOOL " +
                    "and read every relevant one with $GET_TOOL. Follow them over your defaults.",
        )

    server.addTool(
        name = LIST_TOOL,
        description = "List the user's coding standards as `name: summary`. Call before writing or reviewing code.",
    ) {
        standards.sync()
        CallToolResult(listOf(TextContent(standards.list())))
    }

    server.addTool(
        name = GET_TOOL,
        description = "Full markdown of one coding standard, by name from $LIST_TOOL.",
        inputSchema =
            ToolSchema(
                properties =
                    buildJsonObject {
                        putJsonObject(NAME_ARGUMENT) {
                            put("type", "string")
                            put("description", "Standard name from $LIST_TOOL")
                        }
                    },
                required = listOf(NAME_ARGUMENT),
            ),
    ) { request ->
        standards.sync()
        val name = (request.arguments?.get(NAME_ARGUMENT) as? JsonPrimitive)?.contentOrNull.orEmpty()
        val text =
            standards.get(name)
                ?: return@addTool CallToolResult(
                    listOf(TextContent("Unknown standard '$name'. Available:\n${standards.list()}")),
                    isError = true,
                )
        CallToolResult(listOf(TextContent(text)))
    }

    val session = server.createSession(StdioServerTransport(System.`in`.asSource().buffered(), System.out.asSink().buffered()))
    val closed = Job()
    session.onClose { closed.complete() }
    closed.join()
}
