import com.github.syari.kgit.KGit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val MAIN_BRANCH = "main"
private const val STANDARDS_DIR = "standards"
private const val KOTLIN_STANDARD = "kotlin"
private const val KOTLIN_FILE = "$STANDARDS_DIR/$KOTLIN_STANDARD.md"
private const val KOTLIN_RULE = "Prefer val over var"

class StandardsTest {
    @TempDir
    lateinit var root: File

    private val origin get() = root.resolve("origin.git")
    private val author get() = root.resolve("author")
    private val server get() = root.resolve("server")
    private val serverStandards get() = server.resolve(STANDARDS_DIR)

    @Test
    fun `should serve standard pushed to remote when sync runs`() =
        runTest {
            // given
            createRemoteWithServerClone()
            push("add kotlin", KOTLIN_FILE to "# Kotlin\n$KOTLIN_RULE")
            val standards = Standards(serverStandards)

            // when
            standards.sync()

            // then
            standards.get(KOTLIN_STANDARD).shouldNotBeNull() shouldContain KOTLIN_RULE
        }

    @Test
    fun `should not pull again when sync runs within interval`() =
        runTest {
            // given
            createRemoteWithServerClone()
            val standards = Standards(serverStandards)
            standards.sync()
            push("add kotlin", KOTLIN_FILE to "# Kotlin\n$KOTLIN_RULE")

            // when
            standards.sync()

            // then
            standards.get(KOTLIN_STANDARD).shouldBeNull()
        }

    @Test
    fun `should keep local commit when remote history diverged`() =
        runTest {
            // given
            createRemoteWithServerClone()
            serverStandards.resolve("local.md").writeText("# Local")
            KGit.open(server).use { git -> commitAll(git, "add local") }
            push("add kotlin", KOTLIN_FILE to "# Kotlin\n$KOTLIN_RULE")
            val standards = Standards(serverStandards)

            // when
            standards.sync()

            // then
            standards.get("local").shouldNotBeNull()
            standards.get(KOTLIN_STANDARD).shouldBeNull()
        }

    @Test
    fun `should serve local files when remote is unreachable`() =
        runTest {
            // given
            createRemoteWithServerClone()
            origin.deleteRecursively()
            serverStandards.resolve("$KOTLIN_STANDARD.md").writeText("# Kotlin\n$KOTLIN_RULE")
            val standards = Standards(serverStandards)

            // when
            standards.sync()

            // then
            standards.get(KOTLIN_STANDARD).shouldNotBeNull() shouldContain KOTLIN_RULE
        }

    @Test
    fun `should serve local files when directory is not in git repository`() =
        runTest {
            // given
            val input = plainStandards("$KOTLIN_STANDARD.md" to "# Kotlin\n$KOTLIN_RULE")
            val standards = Standards(input)

            // when
            standards.sync()

            // then
            standards.get(KOTLIN_STANDARD).shouldNotBeNull() shouldContain KOTLIN_RULE
        }

    @Test
    fun `should list name and first heading when markdown files exist`() {
        // given
        val input = plainStandards("a.md" to "# Alpha\nbody", "b.md" to "\n\nBeta", "notes.txt" to "skip")

        // when
        val result = Standards(input).list()

        // then
        result shouldBe "- a: Alpha\n- b: Beta"
    }

    @Test
    fun `should report no standards when directory is empty`() {
        // given
        val input = plainStandards()

        // when
        val result = Standards(input).list()

        // then
        result shouldContain "No standards"
    }

    @Test
    fun `should return null when name escapes standards directory`() {
        // given
        root.resolve("secret.md").writeText("secret")
        val input = plainStandards("$KOTLIN_STANDARD.md" to "# Kotlin")

        // when
        val result = Standards(input).get("../secret")

        // then
        result.shouldBeNull()
    }

    @Test
    fun `should return null when standard does not exist`() {
        // given
        val input = plainStandards("$KOTLIN_STANDARD.md" to "# Kotlin")

        // when
        val result = Standards(input).get("missing")

        // then
        result.shouldBeNull()
    }

    private fun plainStandards(vararg files: Pair<String, String>): File =
        root.resolve(STANDARDS_DIR).apply {
            mkdirs()
            files.forEach { (name, text) -> resolve(name).writeText(text) }
        }

    private fun createRemoteWithServerClone() {
        KGit
            .init {
                setDirectory(origin)
                setBare(true)
                setInitialBranch(MAIN_BRANCH)
            }.close()
        KGit
            .init {
                setDirectory(author)
                setInitialBranch(MAIN_BRANCH)
            }.close()
        push("init")
        KGit
            .cloneRepository {
                setURI(origin.toURI().toString())
                setDirectory(server)
            }.close()
        serverStandards.mkdirs()
    }

    private fun push(
        commitMessage: String,
        vararg files: Pair<String, String>,
    ) {
        files.forEach { (path, text) -> author.resolve(path).apply { parentFile.mkdirs() }.writeText(text) }
        KGit.open(author).use { git ->
            commitAll(git, commitMessage)
            git.push {
                remote = origin.toURI().toString()
                add(MAIN_BRANCH)
            }
        }
    }

    private fun commitAll(
        git: KGit,
        commitMessage: String,
    ) {
        git.add { addFilepattern(".") }
        git.commit {
            message = commitMessage
            setAuthor("test", "test@example.com")
            setAllowEmpty(true)
        }
    }
}
