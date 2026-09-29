# Grammio

Fix, rewrite or translate text in any Android app, right from the text-selection menu.

Select some text, open the selection menu (⋮ or **More**), tap **Grammio**, and pick what to do with
it. Copy the result or replace the selected text in place.

## Features

- **Your own transformations:** comes with fix grammar, professional, summarize, rephrase and
  translate. Edit their prompts, reorder or turn them off, or add your own with a name, a task and an
  icon. Write `{language}` in a task and Grammio lets you pick the target language.
- **Editable system prompt:** see and change the instructions sent with every transformation, and
  reset them to the default at any time.
- **Works everywhere:** anywhere Android lets you select text, with no keyboard to set up. For apps
  whose selection menu doesn't list Grammio, turn on the optional **Keyboard button** (Grammio's
  accessibility service): a Grammio button then appears above the keyboard while you type.
- **Bring your own key:** Google Gemini, OpenAI, Anthropic, or any OpenAI-compatible endpoint
  (OpenRouter, a local server, and so on). Pick the model you want.
- **Private by default:** no backend and no account. Your API keys are encrypted on the device, and
  text goes straight to the provider you chose.
- **Optional history:** keep a local log of your transformations and look back at the latest ones in Settings. It's off until you turn it on.

## Getting started

1. Install the app (Android 8.0 or newer).
2. Open Grammio, go to **Provider and model**, choose a provider and paste your API key. Gemini has
   a free tier at [aistudio.google.com](https://aistudio.google.com).
3. Select text in any app and tap **Grammio** in the selection menu.
4. Optional: if an app's menu doesn't show Grammio, tap **Keyboard button** in Grammio's settings and
   turn Grammio on in the Accessibility page it opens. While you type, press the Grammio button above
   the keyboard. It works on the selected text, or on everything in the field if nothing is selected.

## Building

Requires a recent Android Studio. Gradle provisions the JDK it needs.

```
git clone https://github.com/MAshhal/Grammio.git
cd Grammio
./gradlew assembleDebug
```

Run the checks before opening a pull request:

```
./gradlew spotlessCheck test
```

## Tech stack

Kotlin, Jetpack Compose, Koin, Ktor, SQLDelight and DataStore, in a single module laid out with
Clean Architecture. See [ARCHITECTURE.md](ARCHITECTURE.md) for how the code is organized and how to
add a provider.

## License

Grammio is released under the [MIT License](LICENSE).
