# Soroban and Guide Design QA

Last verified: 2026-09-03

## Current surface

- The interactive instrument is a fixed seven-rod Compose Canvas. Each rod represents one decimal digit and supports bead taps and drags.
- The frame uses the 黒檀と骨 palette: ebony lacquer and seeded grain, brass inlay and rods, a bone reckoning field, and black-lacquer bi-conical beads.
- The control rail contains `電卓`, `そろばんを払う`, `使い方`, and `設定`. Clear is disabled at zero and clear/shake reset offers `元に戻す`.
- `使い方` is an inline, fixed-size guide overlay with a read-only five-rod board, animated step states, optional notes/formulas, six chapter tabs, step dots, and cross-chapter navigation.
- The window remains portrait; sideways content is turned inside it. Soroban mode hides the system bars.

## Verification

- `./gradlew test assembleDebug lint` passed on 2026-09-03.
- An API 36 Pixel 7 emulator pass checked the six guide chapters, chapter reset, step dots, cross-chapter `次へ`/`戻る`, focused rod wash, optional notes/formulas, final-step `もう一度`, close/back handling, both system themes, and the turned layout.
- The same emulator pass checked both sideways sensor directions, bead hit testing through the turn, the calculator/soroban buttons, the orientation dead band, settings, and hidden system bars.
- A compact-height API 35 tablet pass covered zero/non-zero soroban states, bead interaction, clear, guide rendering, light/dark themes, and no clipping.

## Findings

No actionable P0, P1, or P2 visual findings remain. The current layout preserves comfortable rail targets, keeps the seven-rod instrument readable, and scales the guide as one piece so its board and footer do not reflow between steps.

## Remaining hardware checks

Physical hardware is still useful for the character of accelerometer shake reset, bead sound, haptic feedback, and the mode transition while the phone is actually being turned. Text-to-speech and Android sharing are not current features and are not QA gaps.
