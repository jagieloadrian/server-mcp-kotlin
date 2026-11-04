import java.io.File
import java.util.concurrent.TimeUnit

// ponytail: pull at most every 5 min, lazily on tool call; add a background poller if docs must refresh while idle
const val SYNC_INTERVAL_MS = 5 * 60 * 1000L

class Standards(private val dir: File) {
    private var lastSync = 0L

    /** `git pull --ff-only` in [dir]; never merges or touches local changes. Failure = serve local copy. */
    @Synchronized
    fun sync() {
        val now = System.currentTimeMillis()
        if (now - lastSync < SYNC_INTERVAL_MS) return
        lastSync = now
        runCatching {
            val p = ProcessBuilder("git", "-C", dir.path, "pull", "--ff-only", "--quiet")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD) // stdout belongs to MCP protocol
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .apply { environment()["GIT_TERMINAL_PROMPT"] = "0" }
                .start()
            when {
                !p.waitFor(15, TimeUnit.SECONDS) -> { p.destroyForcibly(); log("git pull timed out") }
                p.exitValue() != 0 -> log("git pull failed (exit ${p.exitValue()}), serving local copy")
            }
        }.onFailure { log("git pull error: ${it.message}") }
    }

    fun list(): String =
        files().joinToString("\n") { "- ${it.nameWithoutExtension}: ${summary(it)}" }.ifEmpty { "No standards in $dir" }

    /** Lookup by name among existing files only, so `../x` and friends can't escape [dir]. */
    fun get(name: String): String? = files().find { it.nameWithoutExtension == name }?.readText()

    private fun files() = dir.listFiles { f -> f.isFile && f.extension == "md" }.orEmpty().sortedBy { it.name }

    private fun summary(f: File) = f.useLines { lines -> lines.firstOrNull { it.isNotBlank() }?.trimStart('#', ' ') }.orEmpty()

    private fun log(msg: String) = System.err.println("[standards] $msg")
}
