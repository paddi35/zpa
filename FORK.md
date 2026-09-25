# Modified version of ZPA

This repository (`paddi35/zpa`) is a modified fork of [felipebz/zpa](https://github.com/felipebz/zpa)
by Felipe Zorzo. It is distributed under the same license, the
[GNU Lesser General Public License v3.0](LICENSE).

Modifications by Patrick Völkel, since 2026-09-25 (the full list is the git history on top of
the upstream `main` branch):

- NOSONAR comments are detected case-insensitively and in the first line of multiline comments.
- `InequalityUsageCheck` requires `!=` instead of `<>` and is tagged as "convention".
- Issues can offer quick fixes (`QuickFix` and `TextEdit` in the check API, `PlSqlCheckVerifier.verifyQuickFixes`
  in the testkit). Built-in fixes: `InequalityUsage`, `ComparisonWithNull`, `UselessParenthesis`,
  `VariableInitializationWithNull`, `DeclareSectionWithoutDeclarations`, `ExplicitInParameter` and
  `EmptyStringAssignment`. The SonarQube plugin marks those issues as having a quick fix.

Artifacts built from this fork use versions ending in `-local-SNAPSHOT` so that they are never
confused with official ZPA releases.
