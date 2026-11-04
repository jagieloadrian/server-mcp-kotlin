import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.File

private const val LOGGING_BANNER_PROPERTY = "kotlin-logging.logStartupMessage"
private const val HTTP_HOSTS_ENV = "MCP_HTTP_HOSTS"
private const val HTTP_BIND_ADDRESS = "0.0.0.0"
private const val HTTP_PORT = 8080

suspend fun main(args: Array<String>) {
    System.setProperty(LOGGING_BANNER_PROPERTY, "false")
    val dir = File(requireNotNull(args.firstOrNull()) { "Usage: server-mcp-kotlin <standards-dir>" })
    require(dir.isDirectory) { "Not a directory: $dir" }
    val standards = Standards(dir)
    val httpHosts = System.getenv(HTTP_HOSTS_ENV)?.split(',')
    if (httpHosts == null) serveStdio(standards) else serveHttp(standards, httpHosts)
}

private suspend fun serveStdio(standards: Standards) {
    val transport = StdioServerTransport(System.`in`.asSource().buffered(), System.out.asSink().buffered())
    val session = standardsServer(standards).createSession(transport)
    val closed = Job()
    session.onClose { closed.complete() }
    closed.join()
}

private suspend fun serveHttp(
    standards: Standards,
    allowedHosts: List<String>,
) {
    embeddedServer(CIO, port = HTTP_PORT, host = HTTP_BIND_ADDRESS) { standardsHttp(standards, allowedHosts) }
        .startSuspend(wait = true)
}
