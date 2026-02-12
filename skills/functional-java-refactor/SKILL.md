---
name: functional-java-refactor
description: Functional programming guidance for Java 17 codebases; use when analyzing or applying patterns from Saumont’s “Functional Programming in Java” book, refactoring imperative flows into pure/stream-based designs, creating reusable functional templates, or writing/adjusting tests in the fp-discount-engine project.
---

# Purpose
Equip Codex to reshape Java code (Java 17) into functional style, guided by Pierre‑Yves Saumont’s book and the `fp-discount-engine` codebase.

# Quick-start flow
1) **Clarify ask**: capture target classes/methods, desired behavior, side effects to isolate, and test expectations.  
2) **Scan code**: use `rg <term> src` to find entry points; check `pom.xml` for deps (JUnit 5).  
3) **Pick pattern** (see references/patterns.md): favor immutability, pure functions, `Optional`, `Stream`, higher-order funcs, algebraic data modeling with `record`/sealed types, explicit side-effect boundaries.  
4) **Refactor loop**: outline before/after steps; convert loops to `Stream` pipelines; push branching into `map/filter/flatMap`; replace nulls with `Optional`.  
5) **Tests**: keep fast feedback; add/adjust JUnit 5 tests before and after refactor; property-style tests for edge cases.  
6) **Explain**: return minimal diff or snippet, rationale, and how to run tests (`mvn -q test`).

# Using the book
- File: `./Pierre-Yves Saumont - Functional Programming in Java_ How functional techniques improve your Java programs (2017, Manning Publications) - libgen.li.pdf`.
- Cite chapter/page ranges when pulling patterns; avoid loading entire PDF—target sections (e.g., Chapter 2 for immutability, Chapter 4 for higher-order functions, Chapter 8 for Option/Either).

# Project context
- Active code lives in `fp-discount-engine` (this repo). Use it directly instead of copying. Helper asset note in `assets/PROJECT_NOTE.txt`.
- Java 17; JUnit Jupiter 5.10.2. No Vavr—prefer stdlib (`Optional`, `Stream`, `record`, `Function`, `Predicate`, `Supplier`).

# Response pattern
- Start with **goal + constraints**; propose 2–3 pattern options if trade-offs exist.
- Provide **snippet-first** answers; keep snippets standalone and Java 17 compliant.
- Include **tests** or assertions with each behavioral change.
- When larger changes: supply a short **plan + diff outline** before code.

# Safety & style
- Keep side effects at the edges (I/O, time, random); inject via interfaces or suppliers.
- Use total functions where possible; make illegal states unrepresentable with `record`/sealed hierarchies.
- Prefer `Optional.orElseThrow` over null; use `Collectors.toUnmodifiableList()` for outputs.
- Keep functions small; avoid shared mutable state; favor method references over lambdas when readable.

# Resources
- `references/patterns.md`: ready-to-use templates (Optional/Either-like flow, stream refactors, composition helpers, JUnit 5 property-ish tests).
- `assets/PROJECT_NOTE.txt`: reminder on leveraging the live project as the asset instead of bundling a copy.
