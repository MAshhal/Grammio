# Grammio

Fix, rewrite or translate text in any Android app, right from the text-selection menu.

Select some text, open the selection menu (⋮ or **More**), tap **Grammio**, and pick what to do with
it. Copy the result or replace the selected text in place.

## Features

- **Transformations:** fix grammar, rephrase, make it professional or casual, shorten, expand,
  summarize, or translate.
- **Works everywhere:** anywhere Android lets you select text, with no keyboard or accessibility
  service to set up.
- **Bring your own key:** Google Gemini, OpenAI, Anthropic, or any OpenAI-compatible endpoint
  (OpenRouter, a local server, and so on). Pick the model you want.
- **Private by default:** no backend and no account. Your API keys are encrypted on the device, and
  text goes straight to the provider you chose.
- **Optional history:** keep a local log of your transformations. It's off until you turn it on.

## Getting started

1. Install the app (Android 8.0 or newer).
2. Open Grammio, choose a provider and paste your API key. Gemini has a free tier at
   [aistudio.google.com](https://aistudio.google.com).
3. Select text in any app and tap **Grammio** in the selection menu.

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
