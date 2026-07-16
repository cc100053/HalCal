# Project Brief

## Product

Soroban Zen (`そろばん禅`) is a minimalist Android calculator inspired by Japanese wabi-sabi aesthetics. It combines a familiar portrait calculator with a tactile landscape soroban and a small set of Japan-focused learning and utility tools.

## Core promise

- Offer fast everyday arithmetic without visual clutter.
- Make a traditional 1:5 Japanese soroban approachable through touch, animation, readings, audio, and sharing.
- Support Japanese context through consumption-tax calculations, traditional unit conversion, and timed mental-math practice.
- Feel calm and crafted through warm colors, simple typography, haptics, bead sounds, and smooth orientation transitions.

## Current scope

- Portrait calculator with precedence-aware `+`, `-`, `×`, and `÷`, decimal input, clear controls, and result history.
- Landscape soroban with 7–17 rods, per-rod values from 0–9, spring-animated beads, tap/drag input, alignment dots, sound, haptics, shake-to-clear, Japanese readings, TTS, and PNG sharing.
- Consumption tax add/remove flows for Japan's 10% standard and 8% reduced rates.
- Metric-to-traditional conversions for length, area, volume, and weight.
- A 60-second addition/subtraction practice session.
- Persistent history in Room and persistent settings in SharedPreferences.
- English and Japanese Android resources plus system-driven light/dark themes.

## Out of current scope

- Accounts, cloud sync, network services, analytics, or backend infrastructure.
- Multi-module architecture or cross-platform clients.
- User-selected dark mode; the app follows the system theme.
- A general screen navigation graph; current navigation is orientation- and state-driven.
- Tatami room planning. Earlier tracked Tatami planner files are removed in the current working tree; the unit converter still shows an approximate tatami (`畳`) area result.

## Success criteria

- Core calculations are correct and protected by deterministic JVM tests.
- Rotation switches naturally between calculator and soroban without losing ViewModel state.
- Soroban interactions remain responsive and faithfully encode each rod as one decimal digit.
- History and preferences survive app restarts.
- Platform integrations clean up resources and fail gracefully.
- English and Japanese users can understand the main flows.
