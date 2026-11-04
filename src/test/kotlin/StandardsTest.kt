import com.github.syari.kgit.KGit
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StandardsTest {
    @Test
    fun `pulls changes pushed to remote and blocks path escape`() {
        val root = createTempDirectory("standards").toFile()
        val origin = root.resolve("origin.git")
        KGit.init { setDirectory(origin); setBare(true); setInitialBranch("main") }.close()
        val authorDir = root.resolve("author")
        val server = root.resolve("server")

        KGit.init { setDirectory(authorDir); setInitialBranch("main") }.use { author ->
            fun commitAndPush(msg: String) {
                author.add { addFilepattern(".") }
                author.commit { message = msg; setAuthor("t", "t@t"); setAllowEmpty(true) }
                author.push { remote = origin.toURI().toString(); add("main") }
            }
            commitAndPush("init")
            KGit.cloneRepository { setURI(origin.toURI().toString()); setDirectory(server) }.close()
            // server copy is now behind the next push; standards live in a subfolder like the real repo
            authorDir.resolve("standards").mkdirs()
            authorDir.resolve("standards/kotlin.md").writeText("# Kotlin rules\nNo !!")
            commitAndPush("add kotlin")
        }
        server.resolve("standards").mkdirs()
        File(server, "secret.md").writeText("nope")

        val standards = Standards(server.resolve("standards"))
        runBlocking { standards.sync() }

        assertEquals("- kotlin: Kotlin rules", standards.list())
        assertTrue(standards.get("kotlin")!!.contains("No !!"))
        assertNull(standards.get("../secret"))
        assertNull(standards.get("missing"))
    }
}
