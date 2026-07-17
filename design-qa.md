# Landscape Soroban Design QA

## Evidence

- Source visual truth path: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-audit/01-empty.png`
- Implementation screenshot path: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/01-empty.png`
- Viewport: 2560×1600, API 35 Medium Tablet emulator, 320 dpi, landscape, light theme
- State: 13 rods, zero value, empty/default state
- Full-view comparison evidence: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/design-qa-empty-comparison.png`
- Focused region comparison evidence: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/design-qa-rail-comparison.png`; the left control rail was isolated because its typography, action hierarchy, button states, spacing, and instructional copy were the main polish target.
- Additional rendered states:
  - Guide, standard density: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/03-guide.png`
  - Dark theme: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/05-dark.png`
  - Compact-height default state: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/06-compact-height.png`
  - Compact-height guide: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/07-compact-guide.png`

## Findings

- No actionable P0, P1, or P2 findings remain.
- Fonts and typography: the existing serif/sans hierarchy is preserved. The value, Kanji reading, guide title, numbered guide labels, button labels, and helper text have distinct optical weights; no truncation or unintended wrapping is visible at either tested density.
- Spacing and layout rhythm: the rail now groups identity, value, primary action, and utilities into a clearer vertical hierarchy. Outer margins, rail width, 48 dp controls, card radii, soroban frame, and bounded snackbar remain aligned without collisions.
- Colors and visual tokens: the implementation continues to use the app's washi, charcoal, moss, and wood tokens. The outlined read-aloud action is clearly distinct from disabled filled actions in both light and dark themes, with no new off-palette color.
- Image quality and asset fidelity: no external raster assets or logos are part of this screen. The existing procedural ensō and soroban rendering remain sharp at native resolution. The guide preview reuses the product's soroban bead drawing rather than introducing a mismatched illustration style.
- Copy and content: empty-state guidance now names the discoverable tap/drag interaction; shake-reset guidance appears after a value exists. Shortened utility copy stays legible at compact height, and the guide explains the five bead, one beads, unit dot, and digit value with a concrete `7 = 5 + 2` example.
- Icons: Material icons remain consistent in size and stroke family, are aligned with their labels, and do not duplicate spoken accessibility labels.
- States and interactions: rod tap/drag, clear, undo, guide open/close, light/dark rendering, disabled empty-state actions, and compact-height rendering were exercised. UI Automator exposed all 13 rods as adjustable `SeekBar` semantics with a current value and per-rod clear action.
- Accessibility: interactive controls meet the 48 dp target used by this screen. The canvas has an overall value description, individual rods expose 0–9 range semantics, and guide imagery has a Japanese description. Physical sound, haptic, TTS, and shake feel remain device-only verification gaps.

## Open Questions

- None for visual handoff. A real device is still required to judge haptic character, audible feedback, TTS delivery, accelerometer reset, and end-to-end Android sharing.

## Implementation Checklist

- [x] Preserve the established wabi-sabi theme and layered wooden soroban.
- [x] Clarify the rail's information and action hierarchy.
- [x] Make the value typography responsive to long numbers.
- [x] Keep primary and utility actions at practical touch sizes.
- [x] Replace the paragraph-only guide with a visual worked example.
- [x] Bound snackbar width on tablet.
- [x] Add adjustable per-rod accessibility semantics.
- [x] Verify light, dark, standard-density, and compact-height states.
- [x] Run `./gradlew test assembleDebug lint`.

## Follow-up Polish

- [P3] Confirm sound, haptic, TTS, shake-reset, and share delivery on physical hardware; these platform sensations cannot be judged from emulator screenshots.

## Comparison History

1. P2 — Compact rail copy wrapped and weakened the utility-row hierarchy. The share label was shortened to `共有`, and the empty-state hint was tightened to `珠をタップ／ドラッグして動かす`. Post-fix evidence: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/06-compact-height.png`.
2. P2 — The first compact guide placed its fourth explanation below the visible fold. The dialog content was widened and the teaching copy compacted while retaining all four concepts. Post-fix evidence: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/07-compact-guide.png`.
3. P2 — The first width fix made the guide over-expand on the standard tablet viewport. The dialog modifier order was corrected to cap width at 560 dp while filling only the available smaller width. Post-fix evidence: `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/03-guide.png` and `/Users/fatboy/.codex/visualizations/2026/07/16/019f6d4e-edca-78f1-a2d2-a6ebe71693a3/soroban-ui-polished/07-compact-guide.png`.

final result: passed
