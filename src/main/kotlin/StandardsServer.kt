import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal const val LIST_TOOL = "list_standards"
internal const val GET_TOOL = "get_standard"
internal const val NAME_ARGUMENT = "name"
private const val SERVER_NAME = "standards"
private const val SERVER_VERSION = "1.0.0"

internal fun standardsServer(standards: Standards): Server =
    Server(
        Implementation(name = SERVER_NAME, version = SERVER_VERSION),
        ServerOptions(ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))),
        instructions =
            "User's coding standards. Before writing, reviewing or committing code, call $LIST_TOOL " +
                "and read every relevant one with $GET_TOOL. Follow them over your defaults.",
    ).apply {
        addTool(
            name = LIST_TOOL,
            description = "List the user's coding standards as `name: summary`. Call before writing or reviewing code.",
        ) {
            standards.sync()
            CallToolResult(listOf(TextContent(standards.list())))
        }

        addTool(
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
    }
