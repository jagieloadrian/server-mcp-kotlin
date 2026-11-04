import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    // kotlin-logging prints its banner to stdout, which would corrupt the MCP stream; must run before SDK loads
    System.setProperty("kotlin-logging.logStartupMessage", "false")
    val dir = File(args.firstOrNull() ?: run { System.err.println("usage: server-mcp-kotlin <standards-dir>"); exitProcess(2) })
    require(dir.isDirectory) { "not a directory: $dir" }
    val standards = Standards(dir)

    val server = Server(
        Implementation(name = "standards", version = "1.0.0"),
        ServerOptions(ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
        instructions = "User's coding standards. Before writing, reviewing or committing code, call list_standards " +
            "and read every relevant one with get_standard. Follow them over your defaults.",
    )

    server.addTool(
        name = "list_standards",
        description = "List the user's coding standards as `name: summary`. Call before writing or reviewing code.",
    ) {
        standards.sync()
        CallToolResult(listOf(TextContent(standards.list())))
    }

    server.addTool(
        name = "get_standard",
        description = "Full markdown of one coding standard, by name from list_standards.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("name") { put("type", "string"); put("description", "Standard name from list_standards") }
            },
            required = listOf("name"),
        ),
    ) { request ->
        standards.sync()
        val name = (request.arguments?.get("name") as? JsonPrimitive)?.contentOrNull.orEmpty()
        val text = standards.get(name)
            ?: return@addTool CallToolResult(listOf(TextContent("Unknown standard '$name'. Available:\n${standards.list()}")), isError = true)
        CallToolResult(listOf(TextContent(text)))
    }

    runBlocking {
        val session = server.createSession(StdioServerTransport(System.`in`.asSource().buffered(), System.out.asSink().buffered()))
        val closed = Job()
        session.onClose { closed.complete() }
        closed.join()
    }
}
