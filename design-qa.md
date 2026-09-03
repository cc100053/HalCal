# Soroban and Guide Design QA

Last verified: 2026-09-03

## Current surface

- The interactive instrument is a fixed seven-rod Compose Canvas. Each rod represents one decimal digit and supports bead taps and drags.
- The frame uses the 黒檀と骨 palette: ebony lacquer and seeded grain, brass inlay and rods, a bone reckoning field, and black-lacquer bi-conical beads.
- The control rail contains `電卓`, `そろばんを払う`, `使い方`, and `設定`. Clear is disabled at zero and clear/shake reset offers `元に戻す`.
- `使い方` is an inline guide overlay with a read-only five-rod board, animated step states, optional notes/formulas, six chapter tabs, step dots, and cross-chapter navigation. It is composed in a fixed 1060 x 580 design space and fitted to the turned frame by scaling the density.
- The window remains portrait; sideways content is turned inside it. Soroban mode hides the system bars.

## Verification

- `./gradlew test assembleDebug lint` passed on 2026-09-03.
- An API 36 Pixel 7 emulator pass checked the six guide chapters, chapter reset, step dots, cross-chapter `次へ`/`戻る`, focused rod wash, optional notes/formulas, final-step `もう一度`, close/back handling, both system themes, and the turned layout.
- The same emulator pass checked both sideways sensor directions, bead hit testing through the turn, the calculator/soroban buttons, the orientation dead band, settings, and hidden system bars.
- A physical-device pass on 2026-09-03 (Android 16, 1080 x 2400, 440dpi) re-checked `使い方` at its enlarged type and target sizes and captured the entry transition at 60fps to confirm the sheet arrives in one piece.
- A compact-height API 35 tablet pass covered zero/non-zero soroban states, bead interaction, clear, guide rendering, light/dark themes, and no clipping.

## Findings

No actionable P0, P1, or P2 visual findings remain. The current layout preserves comfortable rail targets, keeps the seven-rod instrument readable, and scales the guide as one piece so its board and footer do not reflow between steps.

Two guide findings were fixed on 2026-09-03. Type, board, and control targets inside `使い方` were too small to read comfortably in the turned frame and were enlarged (body 17 to 20sp, step title 24 to 29sp, chapter tabs 19 to 22sp, sheet title 36 to 42sp, footer controls 16 to 19sp with taller targets, preview board 360 to 400dp). Separately, the sheet used to come up in two stages: a 60fps capture of the entry showed the middle of the sheet drawn alone for about 225ms before the header and the footer appeared, because the `requiredSize` sheet overflowed the frame and drew clipped to it. Fitting by scaled density instead removed the stage; a re-capture shows the whole sheet in its first visible frame.

## Remaining hardware checks

Physical hardware is still useful for the character of accelerometer shake reset, haptic feedback, and the mode transition while the phone is actually being turned. Text-to-speech and Android sharing are not current features and are not QA gaps.
