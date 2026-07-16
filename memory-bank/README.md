# Soroban Zen Memory Bank

This folder is the durable project context for humans and AI agents. It complements `AGENTS.md`: that file explains how to work; these files explain what the project is, how it fits together, and what is happening now.

## Reading order

1. [`projectbrief.md`](projectbrief.md) — mission, scope, and success criteria.
2. [`productContext.md`](productContext.md) — user experience, feature behavior, and design intent.
3. [`systemPatterns.md`](systemPatterns.md) — architecture, ownership, data flow, and invariants.
4. [`techContext.md`](techContext.md) — toolchain, dependencies, commands, and platform integration.
5. [`activeContext.md`](activeContext.md) — current working-tree context, recent verification, and immediate risks.
6. [`progress.md`](progress.md) — implemented capabilities, test coverage, and remaining gaps.

## Update protocol

- Update `activeContext.md` after meaningful work so the next agent can resume without reconstructing the repository history.
- Update `progress.md` when feature or verification status changes.
- Update the other files only when durable product, architecture, or toolchain facts change.
- Date time-sensitive entries. Do not put secrets, credentials, personal data, transient logs, or generated output here.
- Verify statements against the current working tree. If plans are uncertain, label them as proposals rather than facts.
