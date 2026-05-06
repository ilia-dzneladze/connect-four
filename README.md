# Connect Four (Compose HTML)

Two-player local Connect Four with a configurable board (6–15) and win length
(3–10). Built with Kotlin/JS + Compose HTML.

## Run

Requires JDK 17+. The first build downloads Gradle via the wrapper.

```bash
gradle wrapper            # one-time, if ./gradlew is missing
./gradlew jsBrowserDevelopmentRun   # serves on http://localhost:8080
./gradlew jsBrowserTest             # runs unit tests (needs Chrome)
```

If you don't have Chrome, run tests via Node by adding `nodejs()` to the `js`
block in `build.gradle.kts` and using `./gradlew jsNodeTest`.

## Project layout

```
src/jsMain/kotlin/
  Main.kt                   entrypoint
  game/GameLogic.kt         pure rules: state, drop, checkWin, applyMove
  game/Persistence.kt       localStorage save/load (kotlinx.serialization)
  ui/App.kt                 top-level composable + Controls + Status
  ui/Board.kt               grid + cell + drop-animation hookup
src/jsMain/resources/
  index.html                HTML shell
  styles.css                ALL visuals — palette, frame, drop keyframes
  wooden_table.jpg          background
src/jsTest/kotlin/
  GameLogicTest.kt          drop / win / draw tests
```

## Tweak points

- **Colors / sizing** — `:root` block at the top of `styles.css`.
- **Drop animation** — `@keyframes drop` + `.disc.dropping` in `styles.css`,
  duration set per-disc in `Board.kt` (`260 + row * 50` ms).
- **Background blur / brightness** — `body::before` in `styles.css`.
- **Board limits (6–15, 3–10)** — `newGame()` in `GameLogic.kt`.
- **Storage key** — `KEY` constant in `Persistence.kt`.

## Design choices

- **Square boards only** — keeps the responsive `--cell` calc one-dimensional.
- **Game logic is pure** — `applyMove` returns a new `GameState`; the UI just
  renders it. Easy to test, easy to swap UI.
- **All styling in CSS, not Kotlin** — single tweak surface for visuals; Kotlin
  only writes the `--cols`/`--rows`/`--drop-from`/`--drop-duration` custom
  properties that the CSS consumes.
- **`lastDropRow/Col` in state** drive the `.dropping` class on exactly the
  most recent disc. They're cleared on `loadState()` so a refresh doesn't
  replay every drop.
- **localStorage + kotlinx.serialization** — single key, JSON-encoded
  `GameState`. Restoring is `loadState() ?: GameState()`.

## Known limitations

- Discs don't render *behind* the board frame's holes (the classic CF effect).
  Doable with an SVG mask overlay; skipped for minimalism.
- `wooden_table.jpg` is ~18 MB. Compress it before shipping.
