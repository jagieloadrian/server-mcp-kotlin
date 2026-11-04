import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.File

private const val LOGGING_BANNER_PROPERTY = "kotlin-logging.logStartupMessage"

suspend fun main(args: Array<String>) {
    System.setProperty(LOGGING_BANNER_PROPERTY, "false")
    val dir = File(requireNotNull(args.firstOrNull()) { "Usage: server-mcp-kotlin <standards-dir>" })
    require(dir.isDirectory) { "Not a directory: $dir" }
    val transport = StdioServerTransport(System.`in`.asSource().buffered(), System.out.asSink().buffered())
    val session = standardsServer(Standards(dir)).createSession(transport)
    val closed = Job()
    session.onClose { closed.complete() }
    closed.join()
}
