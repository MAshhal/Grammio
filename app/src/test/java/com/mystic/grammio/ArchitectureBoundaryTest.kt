package com.mystic.grammio

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

/**
 * Enforces the layer boundaries that separate Gradle modules would enforce, while the app is a
 * single module. Delete this once the layers are split into modules.
 */
class ArchitectureBoundaryTest {

    private val root = File("src/main/java/com/mystic/grammio")

    @Test
    fun `domain is pure Kotlin and depends on no other layer`() = assertNoImports(
        layer = "domain",
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
        layer = "data",
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
        layer = "presentation",
        forbidden = listOf("io.ktor.", "app.cash.sqldelight.", "com.mystic.grammio.data.", "com.mystic.grammio.di."),
    )

    private fun assertNoImports(
        layer: String,
        forbidden: List<String>,
    ) {
        val dir = File(root, layer)
        assertWithMessage("missing $dir (run from the module directory)").that(dir.isDirectory).isTrue()

        val violations = dir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines()
                    .filter { it.startsWith("import ") }
                    .map { it.removePrefix("import ").trim() }
                    .filter { import -> forbidden.any(import::startsWith) }
                    .map { "${file.relativeTo(root)}: $it" }
            }
            .toList()

        assertWithMessage("forbidden imports in $layer/").that(violations).isEmpty()
    }
}
