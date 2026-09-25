package com.mystic.grammio

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Enforces the rules in ARCHITECTURE.md that the compiler can't: layer boundaries (which separate
 * Gradle modules would enforce) and a few per-role conventions. Scans source files, so run it from
 * the module directory, as Gradle does.
 */
class ArchitectureBoundaryTest {

    private val root = File("src/main/java/com/mystic/grammio")

    @Test
    fun `domain is pure Kotlin and depends on no other layer`() = assertNoImports(
        dir = "domain",
        forbidden = listOf(
            "android.",
            "androidx.",
            "io.ktor.",
            "app.cash.sqldelight.",
            "org.koin.",
            "com.mystic.grammio.data.",
            "com.mystic.grammio.presentation.",
            "com.mystic.grammio.di.",
        ),
    )

    @Test
    fun `data does not depend on presentation`() = assertNoImports(
        dir = "data",
        forbidden = listOf(
            "androidx.compose.",
            "androidx.lifecycle.",
            "org.koin.",
            "com.mystic.grammio.presentation.",
            "com.mystic.grammio.di.",
        ),
    )

    @Test
    fun `presentation does not depend on data`() = assertNoImports(
        dir = "presentation",
        forbidden = listOf(
            "io.ktor.",
            "app.cash.sqldelight.",
            "com.mystic.grammio.data.",
            "com.mystic.grammio.di.",
        ),
    )

    @Test
    fun `mappers are pure conversions`() = assertNoImports(
        dir = "data",
        onlyIn = "mapper",
        forbidden = listOf("co.touchlab.kermit.", "io.ktor.client.HttpClient", "androidx.datastore."),
    )

    @Test
    fun `components are stateless and know nothing of ViewModels`() = assertNoImports(
        dir = "presentation",
        onlyIn = "components",
        forbidden = listOf("androidx.lifecycle.", "org.koin."),
    )

    @Test
    fun `wire models are named Dto`() {
        val violations = kotlinFiles("data", onlyIn = "dto")
            .flatMap { file ->
                CLASS_DECLARATION.findAll(file.readText())
                    .map { it.groupValues[1] }
                    .filterNot { it.endsWith("Dto") }
                    .map { "${file.relativeTo(root)}: $it" }
            }
            .toList()

        assertWithMessage("classes in dto/ packages must end with Dto").that(violations).isEmpty()
    }

    private fun assertNoImports(
        dir: String,
        forbidden: List<String>,
        onlyIn: String? = null,
    ) {
        val violations = kotlinFiles(dir, onlyIn)
            .flatMap { file ->
                file.readLines()
                    .filter { it.startsWith("import ") }
                    .map { it.removePrefix("import ").trim() }
                    .filter { import -> forbidden.any(import::startsWith) }
                    .map { "${file.relativeTo(root)}: $it" }
            }
            .toList()

        assertWithMessage("forbidden imports in $dir/").that(violations).isEmpty()
    }

    /** Kotlin files under [dir]; with [onlyIn], just those inside a package segment of that name. */
    private fun kotlinFiles(
        dir: String,
        onlyIn: String? = null,
    ): Sequence<File> {
        val base = File(root, dir)
        assertWithMessage("missing $base (run from the module directory)").that(base.isDirectory).isTrue()
        return base.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { file -> onlyIn == null || onlyIn in file.relativeTo(base).invariantSeparatorsPath.split("/") }
            .also { files ->
                if (onlyIn != null) {
                    assertWithMessage("no $onlyIn/ package under $dir/").that(files.any()).isTrue()
                }
            }
    }

    private companion object {
        val CLASS_DECLARATION = Regex("""^\s*(?:[a-z]+\s+)*class\s+(\w+)""", RegexOption.MULTILINE)
    }
}
