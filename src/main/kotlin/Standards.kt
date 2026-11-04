import com.github.syari.kgit.KGit
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.MergeCommand.FastForwardMode
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L
private const val PULL_TIMEOUT_SECONDS = 15
private const val STANDARD_EXTENSION = "md"

private val logger = KotlinLogging.logger {}

internal class Standards(
    dir: File,
) {
    private val dir = dir.absoluteFile
    private val syncLock = Mutex()
    private var lastSync = 0L

    suspend fun sync() =
        syncLock.withLock {
            val now = System.currentTimeMillis()
            if (now - lastSync < SYNC_INTERVAL_MS) return@withLock
            lastSync = now
            try {
                withContext(Dispatchers.IO) { pull() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn(e) { "Git pull failed for $dir, serving local copy" }
            }
        }

    fun list(): String =
        files()
            .joinToString("\n") { "- ${it.nameWithoutExtension}: ${summary(it)}" }
            .ifEmpty { "No standards in $dir" }

    fun get(name: String): String? = files().find { it.nameWithoutExtension == name }?.readText()

    private fun pull() {
        val gitDir = FileRepositoryBuilder().findGitDir(dir).gitDir
        if (gitDir == null) {
            logger.warn { "$dir is not in a git repository, serving local files" }
            return
        }
        KGit.open(gitDir).use { git ->
            val result =
                git.pull {
                    setRebase(false)
                    setFastForward(FastForwardMode.FF_ONLY)
                    setTimeout(PULL_TIMEOUT_SECONDS)
                }
            if (!result.isSuccessful) {
                logger.warn { "Git pull not applied for $dir (${result.mergeResult?.mergeStatus}), serving local copy" }
            }
        }
    }

    private fun files() = dir.listFiles { file -> file.isFile && file.extension == STANDARD_EXTENSION }.orEmpty().sortedBy { it.name }

    private fun summary(file: File) = file.useLines { lines -> lines.firstOrNull { it.isNotBlank() }?.trimStart('#', ' ') }.orEmpty()
}
