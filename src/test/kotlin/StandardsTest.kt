import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StandardsTest {
    private fun git(dir: File, vararg args: String) {
        val p = ProcessBuilder("git", "-c", "user.name=t", "-c", "user.email=t@t", *args)
            .directory(dir).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readText()
        check(p.waitFor() == 0) { "git ${args.joinToString(" ")}: $out" }
    }

    @Test
    fun `pulls changes pushed to remote and blocks path escape`() {
        val root = createTempDirectory("standards").toFile()
        val origin = root.resolve("origin.git").apply { mkdirs() }
        git(origin, "init", "--bare", "-q")
        git(root, "clone", "-q", origin.path, "author")
        val author = root.resolve("author")
        git(author, "commit", "-q", "--allow-empty", "-m", "init")
        git(author, "push", "-q", "origin", "HEAD")
        git(root, "clone", "-q", origin.path, "server") // server copy is now behind the next push
        File(root, "secret.md").writeText("nope")

        author.resolve("kotlin.md").writeText("# Kotlin rules\nNo !!")
        git(author, "add", ".")
        git(author, "commit", "-qm", "add kotlin")
        git(author, "push", "-q", "origin", "HEAD")

        val standards = Standards(root.resolve("server"))
        standards.sync()

        assertEquals("- kotlin: Kotlin rules", standards.list())
        assertTrue(standards.get("kotlin")!!.contains("No !!"))
        assertNull(standards.get("../secret"))
        assertNull(standards.get("missing"))
    }
}
