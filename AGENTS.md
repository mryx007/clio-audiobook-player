# AGENTS.md

## Scope

- This file is the root agent contract for the whole repository. `CLAUDE.md` is a symlink to it; keep `AGENTS.md` canonical.
- Keep it short, current, and repo-specific. Do not add task-only prompts, secrets, generic Kotlin/Android advice, or rules already enforced
  by ktlint/lint.
- If a rule only applies under one subtree, prefer a nested `AGENTS.md` there instead of growing this file.

## Source Of Truth

- Architecture and module boundaries: `ARCHITECTURE.md`. Read it before changing dependencies, DI wiring, navigation, playback,
  scanning, data, or module structure.
- Module inventory: `settings.gradle.kts`.
- Dependency versions and plugin aliases: `gradle/libs.versions.toml`.
- Build conventions: `plugins/src/main/kotlin`.

## Commands

- Assemble the free debug app: `./gradlew :app:assembleFreeDebug`.
- Run all unit tests: `./gradlew voiceUnitTest`.
- Run unit tests for a library module: `./gradlew :<moduleName>:testDebugUnitTest`.
- Run app unit tests: `./gradlew :app:testFreeDebugUnitTest`.
- Create and register a new module: `./scripts/new_module.main.kts :features:<name>`.
- Discover available Gradle tasks with `./gradlew tasks --all` when unsure.

## Architecture Rules

- Put user-facing screens and flows in `:features:*`.
- Put reusable services, data contracts, playback, scanning, logging, strings, and shared UI in `:core:*`.
- Keep `:app` and `:navigation` focused on app wiring and navigation integration.
- Do not introduce feature-to-feature dependencies. Feature modules should depend on core modules and infrastructure abstractions.
- Core modules must not depend on feature modules.
- Define project dependencies in Gradle files using version catalog entries; do not hardcode dependency versions outside
  `gradle/libs.versions.toml`.

## Implementation Rules

- Make the smallest change that satisfies the request and fits the existing module boundary.
- Prefer existing project patterns, fakes, stores, dispatchers, DI modules, and UI components over new abstractions.
- Keep functions small and names clear. Avoid comments unless they explain a non-obvious reason, workaround, or side effect.
- Formatting is handled by the git hook. Ignore formatting.
- Do not touch signing, release, Fastlane, CI, dependency upgrades, or large generated assets unless the task requires it.

## Testing

- Add or update focused tests for changed behavior, especially domain logic, state reducers/view models, persistence, navigation, and bug
  fixes.
- Prefer lightweight in-memory fakes such as `MemoryFeatureFlag` and `MemoryDataStore` when available.
- For Compose view state tests, use Molecule plus Turbine:

  ```kotlin
  backgroundScope.launchMolecule(RecompositionMode.Immediate) {
    viewModel.viewState()
  }.test {
    awaitItem()
  }
  ```

- Run the narrowest meaningful test first. Broaden to `./gradlew voiceUnitTest` when touching shared behavior, cross-module contracts, or
  risky refactors.
- If tests cannot be run, state the concrete reason and the command that should be run.

## Done Criteria

- Relevant tests or build checks have passed, or the reason they were not run is explicit.
- The diff is scoped to the requested behavior and does not rewrite unrelated code.
- New public behavior is covered by tests or clearly justified if not.
- Instructions in this file stay accurate. Fix stale commands or misleading guidance when found.

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **clio-audiobook-player** (8577 symbols, 15140 relationships, 481 execution flows).

> Index stale? Run `node .gitnexus/run.cjs analyze --index-only` from the project root — it auto-selects an available runner. No `.gitnexus/run.cjs` yet? Bootstrap with `npx`, `bunx`, or `pnpm dlx` — e.g. `bunx gitnexus@latest analyze` (npm 11 npx crash; #1939).

## Always Do

- **MUST run impact before editing.** Use `impact({target: "symbolName", direction: "upstream"})` or `node .gitnexus/run.cjs impact "symbolName" --direction upstream --repo .`; report callers, processes, and risk. Never substitute grep for graph analysis.
- **MUST analyze graph changes before committing.** Use `detect_changes({scope: "all"})` (MCP) or `node .gitnexus/run.cjs detect-changes --scope all --repo .` (CLI fallback). `partial: true` or `truncated: true` is not a clean check — a zero means unseen, not unaffected; re-run it. For regression review: `detect_changes({scope: "compare", base_ref: "main"})` or `node .gitnexus/run.cjs detect-changes --scope compare --base-ref "main" --repo .`.
- MUST warn on HIGH/CRITICAL `risk` pre-edit; never use `riskSharedAxes` to waive a HIGH/CRITICAL `risk` warning. Compare File/symbol: MCP File omits axes; Graph-RAG expands File.
- **MUST treat `risk: UNKNOWN` as unresolved, not as low.** An empty caller set is not evidence the symbol is unused — it can also mean the callers are not resolvable by the index (plain-object property access, dynamic dispatch, cross-language calls). `impact` pairs `UNKNOWN` with a `riskNote` saying so. Confirm with a text search before treating the symbol as safe to change or delete; do not proceed on the strength of a zero.
- **MUST use `query({search_query: "concept"})` for concepts/flows, `context({name: "symbolName"})` for a named symbol, or `impact` for blast radius, on read-only callers, dependencies, imports, or execution flow.** Graph first; text search only for empty/`UNKNOWN`/literals.
- For security review, `explain({target: "fileOrSymbol"})` lists taint findings (source→sink flows; needs `analyze --pdg`).

## Never Do

- NEVER edit a function, class, or method before MCP/CLI impact analysis.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis, and never read `UNKNOWN` as an all-clear — it means the walk could not answer, which is the one verdict that requires confirming by other means.
- NEVER rename symbols with find-and-replace — use `rename` which understands the call graph.
- NEVER commit before MCP/CLI graph change analysis.

## Resources

| Resource | Use for |
| --- | --- |
| `gitnexus://repo/clio-audiobook-player/context` | Codebase overview, check index freshness |
| `gitnexus://repo/clio-audiobook-player/clusters` | All functional areas |
| `gitnexus://repo/clio-audiobook-player/processes` | All execution flows |
| `gitnexus://repo/clio-audiobook-player/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
| --- | --- |
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
