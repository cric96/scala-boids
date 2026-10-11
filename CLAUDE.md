# CLAUDE.md

## Workflow

- **TDD as much as possible**: for each feature write the failing test first, then the minimal code to make it pass, then refactor (red → green → refactor).
  - Tests use `munit-cats-effect` (`munit.CatsEffectSuite`, test bodies can return `IO`); put them in `boids/shared/src/test/scala` (JVM/JS-specific ones under `boids/{jvm,js}/src/test/scala`).
  - Run tests with `sbt test` (or `sbt boids/test` for a faster JVM-only loop).
  - **Write meaningful tests**: a test must fail when the behaviour breaks and keep passing when the code is refactored.
    - Test observable behaviour through the public API, not implementation details (private helpers, call order, internal state).
    - Assert general properties and invariants (e.g. speed never exceeds the max, boids stay within bounds) over a range
      of inputs, or compare against a simple brute-force reference, instead of a single hand-picked example.
    - Every test traces back to a requirement: its name states the behaviour (`"separation pushes overlapping boids apart"`),
      not the method under test (`"test separation"`).
    - One behaviour per test, Arrange/Act/Assert, no logic that could itself be wrong (no loops computing the expected value
      the same way the code does).
    - Derive fixtures from the domain (bounds, `Parameter` defaults, named constants), never arbitrary literals.
    - No tautological tests (asserting what the code trivially returns) and no tests that only check something doesn't throw.
- **Commit often**, one small logical step per commit (e.g. test + implementation of a single behaviour).
- **Semantic commits** (Conventional Commits): `feat:`, `fix:`, `refactor:`, `test:`, `chore:`, `docs:`, `ci:`, `perf:`, optional scope (`feat(dynamics): ...`). Subject in English, imperative, lowercase.
- **No `Co-Authored-By` trailer** (and no other AI attribution) in commit messages or PR descriptions.

## Scala style

Keep the style already in the codebase (Scala 3, formatted with `.scalafmt.conf`, max 120 columns):

- Functional: immutable data (`case class`, `opaque type`), no `var`, pure functions; effects wrapped in `cats.effect.IO`; updates via Monocle (`focus(...).modify(...)`).
- Readable: descriptive names, small private helpers, `for` comprehensions over nested `map`/`flatMap` when clearer.
- **No magic numbers, ever** (production code *and* tests): every literal other than `0`, `1` and `2` in obvious arithmetic
  gets a name, as a constant in the companion `object` or by reusing the `Parameter` defaults. Repeated or derived values are
  computed from those names, never copied. If you need a comment to explain what a number is, it needs a name instead.
- **Clean code, few comments**: the code explains itself through names and small functions. No comments that restate the
  code, no commented-out code, no section banners or changelog notes. Comment only the *why* that the code can't express
  (a non-obvious invariant, a numerical trick, a workaround) and keep it to one line. Prefer renaming or extracting a helper
  over adding a comment.
- One-liner methods whenever possible (`def norm: Double = math.sqrt(x * x + y * y)`).
- Scala 3 syntax: significant indentation, `extension`, `given`/`using`, companion `object` for constructors and types.
- Run `sbt scalafmtAll` before committing.
