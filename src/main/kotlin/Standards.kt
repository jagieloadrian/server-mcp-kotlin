import com.github.syari.kgit.KGit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.MergeCommand.FastForwardMode
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

// ponytail: pull at most every 24 hours, lazily on tool call; add a background poller if docs must refresh while idle
const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L

class Standards(dir: File) {
    private val dir = dir.absoluteFile // relative File has null parent, so findGitDir couldn't walk up
    private var lastSync = 0L // guarded by mutex
    private val mutex = Mutex() // one pull at a time (JGit locks the index); waiters suspend, no thread blocked

    /** Throttled pull; a concurrent caller waits for the running pull, then skips. */
    suspend fun sync() = mutex.withLock {
        val now = System.currentTimeMillis()
        if (now - lastSync < SYNC_INTERVAL_MS) return@withLock
        lastSync = now
        withContext(Dispatchers.IO) { pull() }
    }

    /** Fast-forward-only pull of the repo containing [dir]; never merges or touches local changes. Failure = serve local copy. */
    private fun pull() {
        runCatching {
            // dir may be a subfolder (repo/standards), so search upward for .git
            val gitDir = FileRepositoryBuilder().findGitDir(dir).gitDir ?: return log("$dir is not in a git repo, serving local files")
            KGit.open(gitDir).use { git ->
                val result = git.pull {
                    setRebase(false) // ignore pull.rebase from user config
                    setFastForward(FastForwardMode.FF_ONLY)
                    setTimeout(15)
                }
                if (!result.isSuccessful) log("git pull not applied (${result.mergeResult?.mergeStatus}), serving local copy")
            }
        }.onFailure { log("git pull failed: ${it.message}, serving local copy") }
    }

    fun list(): String =
        files().joinToString("\n") { "- ${it.nameWithoutExtension}: ${summary(it)}" }.ifEmpty { "No standards in $dir" }

    /** Lookup by name among existing files only, so `../x` and friends can't escape [dir]. */
    fun get(name: String): String? = files().find { it.nameWithoutExtension == name }?.readText()

    private fun files() = dir.listFiles { f -> f.isFile && f.extension == "md" }.orEmpty().sortedBy { it.name }

    private fun summary(f: File) = f.useLines { lines -> lines.firstOrNull { it.isNotBlank() }?.trimStart('#', ' ') }.orEmpty()

    private fun log(msg: String) = System.err.println("[standards] $msg")
}
