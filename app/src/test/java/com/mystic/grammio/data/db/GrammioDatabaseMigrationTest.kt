package com.mystic.grammio.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

/** Upgrades a database as the first release created it, the way an existing install is upgraded. */
class GrammioDatabaseMigrationTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

    @After
    fun tearDown() = driver.close()

    @Test
    fun `version 1 gains the transformation table and keeps its log`() {
        driver.execute(null, VERSION_1_SCHEMA, 0)
        driver.execute(
            null,
            "INSERT INTO transformation_log VALUES (1, 1, 'fix_grammar', NULL, 'gemini', NULL, 'a', 'b', NULL, 5)",
            0,
        )

        GrammioDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = GrammioDatabase.Schema.version)

        val database = GrammioDatabase(driver)
        database.transformationQueries.insert("id", "Name", "Do it.", "sparkle", 1, 0.5)
        assertThat(database.transformationQueries.selectAll().executeAsList().map { it.id }).containsExactly("id")
        assertThat(database.transformationLogQueries.selectRecent(10).executeAsList().map { it.transformation })
            .containsExactly("fix_grammar")
    }

    private companion object {
        /** transformation_log exactly as version 1 created it. */
        const val VERSION_1_SCHEMA = """
            CREATE TABLE transformation_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                created_at INTEGER NOT NULL,
                transformation TEXT NOT NULL,
                target_language TEXT,
                provider TEXT NOT NULL,
                model_id TEXT,
                input_text TEXT NOT NULL,
                output_text TEXT,
                error TEXT,
                duration_ms INTEGER NOT NULL
            )
        """
    }
}
