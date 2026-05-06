# Connect Four (Compose HTML)

Two-player local Connect Four with a configurable board (6–15) and win length
(3–10). Built with Kotlin/JS + Compose HTML, persisted via `localStorage`.

## Prerequisites

- **JDK 17+** — required by Gradle 8.10.2 and the Kotlin 2.0.21 toolchain.
- **A browser binary on `PATH`** — Karma needs one to run the unit tests
  headlessly. This project is configured for **Firefox** (`/usr/bin/firefox`
  on most Linux distros). If you want Chrome instead, see
  *Switching the test browser* below.
- No need to install Gradle, Node, or Yarn separately — the Gradle wrapper
  pulls everything into `~/.gradle` and `build/js/` on first run.

## Run

```bash
git clone <this-repo> connect-four
cd connect-four

# Serve the app at http://localhost:8080 (auto-rebuilds on save with -t)
./gradlew jsBrowserDevelopmentRun -t

# Run the unit tests (headless Firefox via Karma)
./gradlew jsTest

# Production bundle → build/dist/js/productionExecutable/
./gradlew jsBrowserProductionWebpack
```

Open **http://localhost:8080** after `jsBrowserDevelopmentRun` boots. The
DevTools Console will show `[cf] saved` / `[cf] loaded` lines confirming
persistence on every move and refresh.

### Switching the test browser

`build.gradle.kts` currently sets:

```kotlin
testTask { useKarma { useFirefoxHeadless() } }
```

Swap to `useChromeHeadless()` if Chrome is on `PATH`, or `useChromiumHeadless()`
for Chromium. After changing, the yarn lock may need refreshing once:

```bash
./gradlew kotlinUpgradeYarnLock
```

## Recreating from scratch

If you nuke `build/`, `.gradle/`, or `kotlin-js-store/yarn.lock`, the next
build re-fetches everything. Useful incantations:

```bash
./gradlew clean                  # remove build outputs
./gradlew kotlinUpgradeYarnLock  # refresh kotlin-js-store/yarn.lock
./gradlew --stop                 # kill the Gradle daemon (if it gets weird)
```

## Project layout

```
src/jsMain/kotlin/
  Main.kt                   entrypoint, mounts <App/>
  game/GameLogic.kt         pure rules: drop, checkWin, applyMove, newGame
  game/Persistence.kt       localStorage save/load (kotlinx.serialization)
  ui/App.kt                 root composable + Controls + Status
  ui/Board.kt               two-layer grid (.disc-layer + .frame-layer)
src/jsMain/resources/
  index.html                HTML shell
  styles.css                ALL visuals — palette, frame mask, animations
  wooden_table.jpg          blurred background
src/jsTest/kotlin/
  GameLogicTest.kt          drop / win / draw / lockout tests
```

## Tweak points

- **Colors / sizing** — `:root` block at the top of `styles.css`.
- **Drop animation** — `@keyframes drop` + `.disc.dropping` in `styles.css`;
  per-disc duration set in `Board.kt` (`280 + row * 55` ms).
- **Frame hole radius** — `--hole-r` in the `.frame-layer` block.
- **Win highlight glow** — `.disc.win.p1` / `.disc.win.p2` and `.status.win.*`.
- **Board limits (6–15, 3–10)** — `newGame()` in `GameLogic.kt`.
- **Storage key** — `KEY` constant in `Persistence.kt`.

## Design choices

- **Square boards only** — keeps the responsive `--cell` calc one-dimensional.
- **Pure game logic** — `applyMove` returns a new `GameState`; the UI just
  renders it. Trivial to test, trivial to swap UI.
- **All styling in CSS** — Kotlin only writes the `--cols`/`--rows`/
  `--drop-from`/`--drop-duration` custom properties; everything else lives
  in `styles.css` for a single tweak surface.
- **Discs render behind the frame** — `.frame-layer` is a solid-blue overlay
  with a tiled `radial-gradient` mask producing one circular cutout per cell.
  The dropping disc is hidden by the surrounding frame and only visible as
  it crosses each cutout — the classic Connect Four look.
- **Synchronous persistence** — `saveState` runs inline with each state
  mutation (not via `SideEffect`), so an immediate refresh always sees the
  latest move. `lastDropRow/Col` are cleared on load so refreshes don't
  replay the drop animation.
- **Pending-input pattern** — board-size and win-length inputs hold local
  state until the user clicks **New Game**, so typing a two-digit number
  doesn't trigger a clamp on every keystroke.

## Known limitations

- `wooden_table.jpg` is large (~18 MB). Compress before shipping:
  `convert wooden_table.jpg -resize 1920x1920\> -quality 78 wooden_table.jpg`
  (ImageMagick) typically gets it under 400 KB with no visible loss after
  the 3-px blur.
- Karma + headless Firefox is the test path; Node-based testing isn't
  configured (the `js` target is browser-only).
