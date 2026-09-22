# Base calculator implementation

Design: [design-proposal.md](design-proposal.md), refined by the user's implementation request.

- [ ] Create one Android module and official Gradle Wrapper; pin stable compatible versions.
- [ ] Write engine behavior tests, observe failure, implement BigDecimal state transitions, pass tests.
- [ ] Connect a small ViewModel to accessible Material 3 keypad and responsive result display.
- [ ] Document setup, behavior, dependencies, unsigned build outputs and independent feature branches.
- [ ] Run test, lint, assembleDebug, assembleRelease and bundleRelease; inspect reports and artifacts.
- [ ] Review final changes and commit only this project, including Wrapper.

Scope decisions: work in the user's requested Caculatorapp directory. No unrelated Obsidian changes. Keep the existing stable AGP 9.3.2 / Gradle 9.5.0 / built-in Kotlin 2.2.10 toolchain and compatible Compose BOM 2025.08.01. This replaces the proposal's tentative AGP 9.4.0. No release-engineering demo code or monitoring scaffolding.
