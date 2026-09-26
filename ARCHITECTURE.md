# Grammio architecture

Grammio is a single `:app` module with Clean Architecture layers kept apart by package. Each
top-level package could become its own Gradle module without moving any code between layers.

```
com.mystic.grammio
├── domain/         What the app does. Pure Kotlin: no Android, Ktor or Koin.
├── data/           How it's done: LLM APIs over Ktor, the encrypted key store, provider settings.
├── presentation/   What the user sees: activities, ViewModels, Compose UI.
└── di/             Koin modules. The only package that sees every layer.
```

## Dependency rule

```
presentation ──▶ domain ◀── data
                   ▲
di ────────────────┴── wires all three together
```

- `domain` depends on nothing else in the app.
- `data` implements domain interfaces and never imports `presentation`.
- `presentation` talks to `domain` only and never imports `data`.

`ArchitectureBoundaryTest` fails the build when any of these rules, or the per-role conventions
below, is broken.

## Where things go

| Package | Holds | Example |
|---|---|---|
| `domain/model` | Business types | `Transformation`, `AiProvider`, `AiModel` |
| `domain/error` | The closed set of failures | `TransformError` |
| `domain/result` | The success-or-typed-failure type | `Outcome` |
| `domain/repository` | Interfaces the domain needs from the outside world | `TextTransformRepository`, `ModelCatalogRepository` |
| `domain/usecase` | One application rule each | `TransformTextUseCase` |
| `data/<concept>` | The repository implementation for that concept | `data/transform/TextTransformRepositoryImpl` |
| `data/provider` | Provider defaults, stored settings, and turning them into a connection | `ProviderDefaults`, `ProviderConnectionResolver` |
| `data/llm` | The vendor-neutral LLM contract and the provider → data source registry | `LlmDataSource`, `LlmDataSourceRegistry` |
| `data/<concept>/local`, `data/llm/<vendor>` | Data sources that do I/O | `EncryptedApiKeyLocalDataSource`, `GeminiDataSource` |
| `data/**/dto` | Wire models | `GenerateContentRequestDto` |
| `data/**/mapper` | Pure conversions between wire models and app types | `GeminiErrorMapper` |
| `presentation/<screen>` | Activity, ViewModel, UiState, Action, Effect, Screen | `presentation/process/*` |
| `presentation/<screen>/components` | Stateless composables used by that screen | `ResultCard` |
| `presentation/<screen>/model` | UI mapping of domain types (labels, messages, options) | `TransformErrorMessage` |
| `presentation/common`, `presentation/theme` | Shared UI helpers and theme | `Clipboard`, `GrammioTheme` |

## Roles and naming

| Suffix | Role | Rules |
|---|---|---|
| `*UseCase` | One application rule | Exposes a single `operator fun invoke`. Only exists when there is a rule to hold (see below). |
| `*Repository` | Domain interface | Written in domain terms, with no HTTP, prompt or storage concepts. |
| `*RepositoryImpl` | Its data implementation | Orchestrates data sources, prompts and mappers, and does no I/O itself. |
| `*DataSource` | One source of data | Performs the I/O. Remote data sources hold no configuration: key, base URL and model arrive with each call. |
| `*Mapper` | Pure conversion | No logging, no I/O, no state. |
| `*Dto` | Wire model | `internal`, `@Serializable`, only the fields actually used. |
| `*UiState` / `*Action` / `*Effect` | Screen state, user intents, one-off events | Immutable data. The ViewModel is the only thing that creates state. |
| `*ViewModel` | Screen logic | Exposes `StateFlow` state and a `Flow` of effects, and takes actions through `onAction`. |
| `*Activity` | Host | Parses the Intent, renders the screen and performs effects. Contains no logic. |

**Use cases are pragmatic.** A ViewModel may call a repository interface directly when a use case
would only pass the call through. `SettingsViewModel` observes and clears the key through
`ApiKeyRepository`, but saves through `SaveApiKeyUseCase` because saving has rules (trim the key,
refuse a blank one).

**API keys never leave `data/`.** Each provider has its own encrypted key, and only
`ApiKeyLocalDataSource` can read one back. `ProviderConnectionResolver` puts it into the
`LlmConnection` handed to the LLM data source per call. The UI can only see whether a key exists.

## LLM providers

The user picks a provider in Settings, and `TextTransformRepositoryImpl` sends the text to it:

```
ProviderConnectionResolver ── active provider, its key, base URL and model ──▶ LlmConnection
LlmDataSourceRegistry ─────── provider ──▶ GeminiDataSource | OpenAiDataSource | AnthropicDataSource
```

- One `LlmDataSource` per wire API, in `data/llm/<vendor>/` with its own DTOs and mappers.
  `OpenAiDataSource` serves both OpenAI and any OpenAI-compatible endpoint; only the base URL differs.
- Failures common to every HTTP API (status codes, timeouts, offline) map through
  `data/llm/mapper/HttpErrorMapper`. Vendor mappers handle their own error bodies first.
- `ProviderDefaults` holds each provider's base URL and default model. The user can override the
  model with one listed by the provider's API (`ModelCatalogRepository`). A custom endpoint has no
  defaults, so it reports `TransformError.ProviderNotConfigured` until both are set.
- Persisted names come from `AiProvider.storageKey`, never from the enum name.

To add a provider:

1. Add an `AiProvider` entry and its `storageKey`.
2. Give it a base URL and default model in `ProviderDefaults`.
3. If it speaks a new wire API, add a data source under `data/llm/<vendor>/` with a MockEngine test.
4. Route it in `LlmDataSourceRegistry` and wire any new data source in `DataModule`.
5. Give it a label (`AiProviderLabel`) and, if it has one, a key link (`AiProviderKeyLink`).

The exhaustive `when`s in steps 1, 2, 4 and 5 fail to compile until each one is done.

## Transformation log

Shown to the user as "history". It is opt-in: nothing is recorded until the Settings switch
(`HistorySettingsRepository`, its own `history_settings` DataStore file) is on.

While it is on, `TextTransformRepositoryImpl` records every attempt, successful or not, through
`TransformationLogLocalDataSource` into the `transformation_log` table of `grammio.db`
(SQLDelight, schema in `src/main/sqldelight`). Each row holds the input text, the cleaned-up
output or the error, the transformation (plus target language), provider, model and duration.

- Turning history off stops recording; it does not delete what was already saved.
- Transformations, providers and errors are stored by their `storageKey`, never the Kotlin name.
- Recording is best effort: a database failure is logged and never costs the user their result.
- Input rejected by `TransformTextUseCase` (blank, too long) never reaches the repository, so it
  is not logged.

## Files

- One public top-level type per file, named after the file. Extension functions live in a file
  named for what they map, such as `TransformationLabel.kt`.
- A request or response DTO file may also hold the small nested DTOs that only it uses.
- Tests mirror the main package layout. Shared fakes live in `testing/`.

## Formatting

Formatting uses ktlint (`android_studio` style) through Spotless. The rules live in
`.editorconfig`, so the IDE formatter and ktlint agree: a 120-character line limit, trailing
commas, and one parameter per line once there are two or more.

```
./gradlew spotlessApply     # format
./gradlew spotlessCheck     # verify (CI / before committing)
```

If you change `.editorconfig`, run `./gradlew --stop` first. The Gradle daemon caches the ktlint
settings.
