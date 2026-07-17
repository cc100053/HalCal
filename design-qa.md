# Landscape Soroban Hybrid Design QA

## Evidence

- Source visual truth path: `/Users/fatboy/.codex/generated_images/019f6d92-af47-79c0-b113-f22c2efa23b7/exec-8f45a672-8139-402a-8617-1b0af06bc2e3.png`
- Implementation screenshot path: `/private/tmp/soroban-hybrid-final-light-stable.png`
- Viewport: 2560×1600, API 35 Medium Tablet emulator, 320 dpi, landscape, light theme
- State: 13 rods, zero value, empty/default state
- Full-view comparison evidence: `/private/tmp/soroban-hybrid-comparison-final.png`
- Focused region comparison evidence: `/private/tmp/soroban-hybrid-rail-comparison-final.png`; the left control rail was isolated because the selected hybrid depends on its typography, open spacing, icon fidelity, and quiet action hierarchy.
- Additional rendered states:
  - Dark theme: `/private/tmp/soroban-hybrid-final-dark.png`
  - Active value (`4,000,000,000,000` / `四兆`): `/private/tmp/soroban-hybrid-final-active.png`
  - Cleared state: `/private/tmp/soroban-hybrid-final-cleared.png`
  - Compact-height stress pass at 560 dpi: `/private/tmp/soroban-hybrid-final-compact.png`
  - Visual guide: `/private/tmp/soroban-hybrid-final-guide.png`

## Findings

- No actionable P0, P1, or P2 findings remain.
- Fonts and typography: the selected system serif/sans hierarchy is reproduced for the brand, numeric value, Kanji reading, rod count, and action labels. The active multi-trillion value fits without clipping, and the rail remains readable at the compact-height stress density.
- Spacing and layout rhythm: the instrument occupies the same dominant right-hand field as the source, while the slim unboxed rail preserves generous negative space. Its four action rows remain 48 dp-class targets, and the soroban stays centered without colliding with system insets.
- Colors and visual tokens: pale hinoki framing, aizome-indigo beads, silver rods, warm paper, sumi text, and moss utility accents closely match the selected hybrid. The dark-theme translation retains the same hierarchy with smoked wood and restrained highlights.
- Image quality and asset fidelity: the soroban is a responsive functional Compose Canvas rather than a static raster image, so it scales from 7 to 17 rods and retains tap/drag behavior. Beads, holes, highlights, rods, beam, unit dots, frame grain, and shadow are rendered at native resolution. The implementation intentionally keeps the final rod fully visible instead of reproducing the source image's cropped edge bead.
- Copy and content: all visible Japanese copy matches the selected composition: `そろばん禅`, `13桁`, `0`, `零`, `読み上げ`, `そろばんを払う`, `共有`, and `使い方`.
- Icons: speaker and cleaning-service actions use official rounded Material Symbols vector paths. The vectors remain sharp at all tested densities and match the visual weight of the source; the existing procedural ensō remains the brand mark.
- States and interactions: bead tap, live numeric/Kanji value, clear, guide open, zero-state share availability, light/dark rendering, and compact-height rendering were exercised. Clear correctly changes from unavailable at zero to active for a non-zero value.
- Accessibility and platform fit: action rows retain practical touch targets, and the existing adjustable per-rod semantics remain intact. Physical sound, haptic, TTS, accelerometer reset, and Android share delivery remain device-only verification gaps.

## Open Questions

- None for visual handoff. A real device is still required to judge sound, haptic character, TTS delivery, accelerometer reset, and end-to-end Android sharing.

## Implementation Checklist

- [x] Combine design 3's soroban materials with design 1's quiet control rail.
- [x] Preserve the interactive 7–17 rod Compose Canvas and existing state model.
- [x] Match the pale hinoki, indigo bead, silver rod, and warm paper palette.
- [x] Replace card-based rail controls with unboxed 48 dp-class action rows.
- [x] Use licensed open-source vector icons for speaker and cleaning actions.
- [x] Align the guide preview and generated share card with the new soroban materials.
- [x] Verify light, dark, active, cleared, guide, and compact-height states.
- [x] Run `./gradlew test assembleDebug lint`.

## Follow-up Polish

- [P3] Confirm sound, haptic, TTS, shake-reset, and share delivery on physical hardware; these platform sensations cannot be judged from emulator screenshots.
- [P3] The generated source has stronger paper grain and slightly more irregular bead shading. The implementation keeps those details restrained to preserve legibility and stable real-time Canvas performance across 7–17 rods.

## Comparison History

1. P2 — The first implementation render made the instrument too tall and visually dense, with a darker frame than the selected source. The content height was reduced, the frame/beam were thickened, and the hinoki tones, rods, and bead scale were adjusted. Post-fix evidence: `/private/tmp/soroban-hybrid-comparison-final.png`.
2. P2 — The first rail pass used generic play/delete icons, drifting from the selected speaker/cleaning visual language. They were replaced with official rounded Material Symbols vectors and documented in `THIRD_PARTY_NOTICES.md`. Post-fix evidence: `/private/tmp/soroban-hybrid-rail-comparison-final.png`.
3. P2 — Sharing was disabled at zero even though the selected source presents it as an available utility. Zero-state sharing was enabled while clear remains correctly disabled until a bead moves. Post-fix evidence: `/private/tmp/soroban-hybrid-final-light-stable.png` and `/private/tmp/soroban-hybrid-final-active.png`.

final result: passed
