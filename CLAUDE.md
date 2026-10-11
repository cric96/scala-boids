# CLAUDE.md

## Workflow

- **TDD as much as possible**: for each feature write the failing test first, then the minimal code to make it pass, then refactor (red → green → refactor).
  - Tests use `munit-cats-effect` (`munit.CatsEffectSuite`, test bodies can return `IO`); put them in `boids/shared/src/test/scala` (JVM/JS-specific ones under `boids/{jvm,js}/src/test/scala`).
  - Run tests with `sbt test` (or `sbt boids/test` for a faster JVM-only loop).
- **Commit often**, one small logical step per commit (e.g. test + implementation of a single behaviour).
- **Semantic commits** (Conventional Commits): `feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `docs:`, `ci:`, `perf:`, optional scope (`feat(dynamics): ...`). Subject in English, imperative, lowercase.
- **No `Co-Authored-By` trailer** (and no other AI attribution) in commit messages or PR descriptions.

## Scala style

Keep the style already in the codebase (Scala 3, formatted with `.scalafmt.conf`, max 120 columns):

- Functional: immutable data (`case class`, `opaque type`), no `var`, pure functions; effects wrapped in `cats.effect.IO`; updates via Monocle (`focus(...).modify(...)`).
- Readable: descriptive names, small private helpers, `for` comprehensions over nested `map`/`flatMap` when clearer.
- One-liner methods whenever possible (`def norm: Double = math.sqrt(x * x + y * y)`).
- Scala 3 syntax: significant indentation, `extension`, `given`/`using`, companion `object` for constructors and types.
- Run `sbt scalafmtAll` before committing.
