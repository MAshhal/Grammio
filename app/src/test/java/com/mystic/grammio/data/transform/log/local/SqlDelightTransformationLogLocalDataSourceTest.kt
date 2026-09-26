package com.mystic.grammio.data.transform.log.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.db.GrammioDatabase
import com.mystic.grammio.data.db.Transformation_log
import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.testing.TestTransformations
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class SqlDelightTransformationLogLocalDataSourceTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { GrammioDatabase.Schema.create(it) }
    private val database = GrammioDatabase(driver)
    private val dataSource = SqlDelightTransformationLogLocalDataSource(database)

    @After
    fun tearDown() = driver.close()

    @Test
    fun `records a successful transformation`() = runTest {
        dataSource.record(
            entry(
                transformation = TestTransformations.translate,
                targetLanguageTag = "es",
                result = Outcome.Success("Hola."),
            ),
        )

        assertThat(rows()).containsExactly(
            Transformation_log(
                id = 1,
                created_at = 1_700_000_000_000,
                transformation = "translate",
                target_language = "es",
                provider = "anthropic",
                model_id = "claude-haiku-4-5",
                input_text = "Hello.",
                output_text = "Hola.",
                error = null,
                duration_ms = 1_234,
            ),
        )
    }

    @Test
    fun `records a failure and the model it happened on`() = runTest {
        dataSource.record(entry(result = Outcome.Failure(TransformError.RateLimited)))

        val row = rows().single()
        assertThat(row.transformation).isEqualTo("fix_grammar")
        assertThat(row.target_language).isNull()
        assertThat(row.model_id).isEqualTo("claude-haiku-4-5")
        assertThat(row.output_text).isNull()
        assertThat(row.error).isEqualTo("rate_limited")
    }

    @Test
    fun `records a failure before any model was chosen`() = runTest {
        dataSource.record(entry(modelId = null, result = Outcome.Failure(TransformError.MissingApiKey)))

        val row = rows().single()
        assertThat(row.model_id).isNull()
        assertThat(row.error).isEqualTo("missing_api_key")
    }

    @Test
    fun `lists the most recent entries first`() = runTest {
        dataSource.record(entry(inputText = "first", startedAt = Instant.fromEpochMilliseconds(1)))
        dataSource.record(entry(inputText = "second", startedAt = Instant.fromEpochMilliseconds(2)))

        assertThat(rows().map { it.input_text }).containsExactly("second", "first").inOrder()
    }

    @Test
    fun `reads entries back, newest first, limited`() = runTest {
        dataSource.record(entry(inputText = "oldest", startedAt = Instant.fromEpochMilliseconds(1)))
        dataSource.record(
            entry(
                inputText = "failed",
                startedAt = Instant.fromEpochMilliseconds(2),
                modelId = null,
                result = Outcome.Failure(TransformError.MissingApiKey),
            ),
        )
        dataSource.record(
            entry(
                transformation = TestTransformations.translate,
                targetLanguageTag = "es",
                inputText = "Hello.",
                startedAt = Instant.fromEpochMilliseconds(3),
                result = Outcome.Success("Hola."),
            ),
        )

        assertThat(dataSource.recent(limit = 2).first()).containsExactly(
            HistoryEntry(
                id = 3,
                startedAt = Instant.fromEpochMilliseconds(3),
                transformationId = "translate",
                targetLanguageTag = "es",
                modelId = "claude-haiku-4-5",
                inputText = "Hello.",
                result = Outcome.Success("Hola."),
            ),
            HistoryEntry(
                id = 2,
                startedAt = Instant.fromEpochMilliseconds(2),
                transformationId = "fix_grammar",
                targetLanguageTag = null,
                modelId = null,
                inputText = "failed",
                result = Outcome.Failure(TransformError.MissingApiKey),
            ),
        ).inOrder()
    }

    @Test
    fun `an error this version doesn't know reads as unknown`() = runTest {
        database.transformationLogQueries.insert(
            created_at = 1,
            transformation = "fix_grammar",
            target_language = null,
            provider = "gemini",
            model_id = "gemini-2.5-flash",
            input_text = "Hello.",
            output_text = null,
            error = "from_the_future",
            duration_ms = 5,
        )

        assertThat(dataSource.recent(limit = 1).first().single().result)
            .isEqualTo(Outcome.Failure(TransformError.Unknown))
    }

    @Test
    fun `clear deletes every entry`() = runTest {
        dataSource.record(entry())
        dataSource.record(entry())

        dataSource.clear()

        assertThat(rows()).isEmpty()
        assertThat(dataSource.recent(limit = 10).first()).isEmpty()
    }

    @Test
    fun `a database failure is swallowed`() = runTest {
        driver.close()

        dataSource.record(entry())
    }

    private fun rows() = database.transformationLogQueries.selectRecent(limit = 10).executeAsList()

    private fun entry(
        startedAt: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000),
        transformation: Transformation = TestTransformations.fixGrammar,
        targetLanguageTag: String? = null,
        modelId: String? = "claude-haiku-4-5",
        inputText: String = "Hello.",
        result: Outcome<String, TransformError> = Outcome.Success("Hello."),
    ) = TransformationLogEntry(
        startedAt = startedAt,
        transformation = transformation,
        targetLanguageTag = targetLanguageTag,
        provider = AiProvider.Anthropic,
        modelId = modelId,
        inputText = inputText,
        result = result,
        duration = 1_234.milliseconds,
    )
}
