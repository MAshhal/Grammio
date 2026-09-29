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
| `domain/model` | Business types | `Transformation`, `TransformationIcon`, `AiProvider`, `AiModel` |
| `domain/error` | The closed set of failures | `TransformError` |
| `domain/result` | The success-or-typed-failure type | `Outcome` |
| `domain/repository` | Interfaces the domain needs from the outside world | `TextTransformRepository`, `TransformationRepository` |
| `domain/usecase` | One application rule each | `TransformTextUseCase` |
| `data/<concept>` | The repository implementation for that concept | `data/transform/TextTransformRepositoryImpl` |
| `data/provider` | Provider defaults, stored settings, and turning them into a connection | `ProviderDefaults`, `ProviderConnectionResolver` |
| `data/llm` | The vendor-neutral LLM contract and the provider → data source registry | `LlmDataSource`, `LlmDataSourceRegistry` |
| `data/<concept>/local`, `data/llm/<vendor>` | Data sources that do I/O | `EncryptedApiKeyLocalDataSource`, `GeminiDataSource` |
| `data/**/dto` | Wire models | `GenerateContentRequestDto` |
| `data/**/mapper` | Pure conversions between wire models and app types | `GeminiErrorMapper` |
| `presentation/<screen>` | Activity, ViewModel, UiState, Action, Effect, Screen | `presentation/process/*` |
| `presentation/settings/<page>` | One Settings page: ViewModel, UiState, Action, Screen | `presentation/settings/provider/*` |
| `presentation/<screen>/components` | Stateless composables used by that screen | `ResultCard` |
| `presentation/<screen>/model` | UI mapping of domain types (labels, messages, options) | `TransformErrorMessage` |
| `presentation/accessibility` | The accessibility service entry point and the selection it hands the sheet | `GrammioAccessibilityService`, `SelectionReplacer` |
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
| `*Service` | Host | Like an Activity: finds its input, opens a screen with it. Contains no logic. |
| `*Route` | Navigation key | `@Serializable` `NavKey`, so the back stack survives process death. |

**Use cases are pragmatic.** A ViewModel may call a repository interface directly when a use case
would only pass the call through. `SettingsViewModel` observes and clears the key through
`ApiKeyRepository`, but saves through `SaveApiKeyUseCase` because saving has rules (trim the key,
refuse a blank one).

## Entry points

Other apps reach the process sheet (`ProcessTextActivity`) two ways, and both end in the same
ViewModel, transformations and result actions:

```
Selection menu ─── PROCESS_TEXT ──────────────────────────────▶ ProcessTextActivity
Accessibility button ─ GrammioAccessibilityService ─ alias ───▶ ProcessTextActivity
```

- **Selection menu.** The system lists Grammio for `ACTION_PROCESS_TEXT`. Replace returns the text
  as the activity result; the caller puts it in place.
- **Accessibility service.** For apps whose menu doesn't list PROCESS_TEXT actions. The service
  (opt-in, in the system's Accessibility settings) listens only for selection changes and keeps a
  reference to where text was last selected, never the text. Pressing the accessibility button or
  shortcut reads the selection (`TextSelection`) from the focused field, or else that view, and
  opens the sheet through the non-exported `AccessibilityProcessTextActivity` alias with the same
  PROCESS_TEXT extras. A service has no caller to return a result to, so it leaves the field with
  `SelectionReplacer`, and Replace writes the whole text back with the selection swapped. If the app
  refuses, the result is copied instead. Password fields are never read.
- `ProcessTextInput` decides whether Replace is offered: the selection isn't read-only, and either
  the caller wants a result or the launch came through the alias. Only Grammio can start the alias,
  so another app can't claim to be the service.

## Settings navigation

`SettingsActivity` is the only settings activity. It hosts `SettingsNavigation`, a Navigation3
`NavDisplay` over a back stack of `SettingsRoute`s:

```
Home ─┬─ Provider            provider, endpoint, API key, model
      ├─ Transformations ─── TransformationEditor(id)   (null id = new)
      ├─ SystemPrompt
      └─ History             history switch, privacy note, recent entries
```

- Each entry gets its own `ViewModelStore` (`rememberViewModelStoreNavEntryDecorator`), so a page's
  ViewModel lives exactly as long as the page is on the stack. Pages get theirs with `koinViewModel()`.
- Screens take `onBack` and navigation lambdas; they never touch the back stack themselves. A
  ViewModel that needs to leave its page emits an effect (the editor's `Close`).
- `SettingsActivity.providerIntent` and `transformationsIntent` open Settings on a page with Home
  underneath. The process sheet uses them to send the user to the page that fixes the problem.
- A page grows out of the row that opens it and shrinks back into it (a container transform,
  `PageMorph.kt`). The page is a `pageEntry<>`, and whatever opens it is marked with
  `Modifier.morphsInto(route)`; the route is the key that pairs the two.

To add a page: add a `SettingsRoute`, a `pageEntry<>` in `SettingsNavigation`, a row on
`SettingsHomeScreen` marked `morphsInto` the route, and register its ViewModel in
`PresentationModule`.

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

## Transformations

A `Transformation` is user data: an id, a name, a task prompt, an icon, an on/off switch and a
temperature (not shown to the user). `TransformationRepository` keeps them in the `transformation`
table of `grammio.db`, in the order the user arranged them.

- **Defaults.** `DefaultTransformations` holds the five that ship with the app. The repository adds
  them the first time anything reads or changes the list, then sets a flag in the
  `transformation_settings` DataStore file, so deleting them all doesn't bring them back.
  "Restore defaults" writes them again with their original content and leaves the user's own alone.
- **Ids.** The defaults keep the storage keys the old hard-coded transformations had
  (`fix_grammar`, `translate`, ...), so older history rows still match. New ones get a random UUID
  from `SaveTransformationUseCase`.
- **Target language.** A task prompt containing `{language}` (`Transformation.LANGUAGE_PLACEHOLDER`)
  makes the sheet show the language picker. `PromptBuilder` replaces the placeholder with the
  language's English name.
- **Icons.** `TransformationIcon` is a closed set. It is stored by `storageKey`, drawn through
  `presentation/common/TransformationIconVector.kt`, and an unknown key falls back to `Sparkle`.

### Prompt

`PromptBuilder` composes the system instruction from three parts:

```
<system prompt>          PromptSettingsRepository: the user's own, or DefaultSystemPrompt

Task: <task prompt>      with {language} replaced
<language line>          "Write the output in German." or "Reply in the same language as the input text."
```

The user's text always goes in the user message, wrapped in `<text></text>`. Only a system prompt
that differs from the default is stored (`prompt_settings` DataStore file), so later changes to the
default still apply to everyone who never edited it.

### Schema changes

`grammio.db` is migrated with SQLDelight `.sqm` files next to the `.sq` files. `1.sqm` added the
`transformation` table. The `.sq` files always describe the latest schema; each migration must
produce the same result, and `GrammioDatabaseMigrationTest` upgrades a version 1 database to check.

## Transformation log

Shown to the user as "history". It is opt-in: nothing is recorded until the Settings switch
(`HistorySettingsRepository`, its own `history_settings` DataStore file) is on.

While it is on, `TextTransformRepositoryImpl` records every attempt, successful or not, through
`TransformationLogLocalDataSource` into the `transformation_log` table of `grammio.db`
(SQLDelight, schema in `src/main/sqldelight`). Each row holds the input text, the cleaned-up
output or the error, the transformation's id (plus the target language when it used one), provider,
model and duration.

- Turning history off stops recording; it does not delete what was already saved. Clearing does
  that (`HistoryRepository.clear`) and leaves the switch as it was.
- The History page lists the latest `HistorySettingsViewModel.RECENT_LIMIT` (50) entries under the
  privacy note, read back through `HistoryRepository`. Each is shown with the transformation's
  current name and icon; one that has since been deleted is labelled as such.
- Transformations are stored by id; providers and errors by their `storageKey`, never the Kotlin
  name. A logged id may belong to a transformation that has since been edited or deleted. An error
  key this version doesn't know reads back as `TransformError.Unknown`.
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
