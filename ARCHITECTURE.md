# Grammio architecture

Grammio is a single `:app` module with Clean Architecture layers kept apart by package. Each
top-level package could become its own Gradle module without moving any code between layers.

```
com.mystic.grammio
├── domain/         What the app does. Pure Kotlin: no Android, Ktor or Koin.
├── data/           How it's done: Gemini over Ktor, the encrypted key store.
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
| `domain/model` | Business types | `Transformation`, `TransformedText` |
| `domain/error` | The closed set of failures | `TransformError` |
| `domain/result` | The success-or-typed-failure type | `Outcome` |
| `domain/repository` | Interfaces the domain needs from the outside world | `TextTransformRepository` |
| `domain/usecase` | One application rule each | `TransformTextUseCase` |
| `data/<concept>` | The repository implementation for that concept | `data/transform/TextTransformRepositoryImpl` |
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
| `*DataSource` | One source of data | Performs the I/O. Remote data sources are stateless. |
| `*Mapper` | Pure conversion | No logging, no I/O, no state. |
| `*Dto` | Wire model | `internal`, `@Serializable`, only the fields actually used. |
| `*UiState` / `*Action` / `*Effect` | Screen state, user intents, one-off events | Immutable data. The ViewModel is the only thing that creates state. |
| `*ViewModel` | Screen logic | Exposes `StateFlow` state and a `Flow` of effects, and takes actions through `onAction`. |
| `*Activity` | Host | Parses the Intent, renders the screen and performs effects. Contains no logic. |

**Use cases are pragmatic.** A ViewModel may call a repository interface directly when a use case
would only pass the call through. `SettingsViewModel` observes and clears the key through
`ApiKeyRepository`, but saves through `SaveApiKeyUseCase` because saving has rules (trim the key,
refuse a blank one).

**The API key never leaves `data/`.** Only `ApiKeyLocalDataSource` can read it back.
`TextTransformRepositoryImpl` passes it to the LLM data source per call. The UI can only see
whether a key exists.

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
