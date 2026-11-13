# Part 3 verification results

Verified on 28 October 2026 with Java 17 and Maven 3.9.9.

| Test class | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| LoginTest | 5 | 0 | 0 | 0 |
| MainTest | 9 | 0 | 0 | 0 |
| MessageTest | 21 | 0 | 0 | 0 |
| Part3ConsoleTest | 5 | 0 | 0 | 0 |
| Part3Test | 23 | 0 | 0 | 0 |
| **Total** | **63** | **0** | **0** | **0** |

**Maven result: BUILD SUCCESS.** The 35 existing tests and 28 Part 3 tests passed. Normal command: `mvn clean verify`. Build dependencies were downloaded with runtime-specific proxy settings, excluded from the project; the final verification also succeeded offline after dependencies were cached.

The packaged JAR was separately run three times:

1. Entered all five assignment messages through the real console: 2 sent, 2 stored, 1 disregarded. Exercised participants, longest message, recipient search and both reports. Python independently parsed the JSON and verified both stored texts and sender fields.
2. Restarted, searched by the actual generated ID and deleted the first stored message by its actual hash with confirmation. Python verified that only the second stored record remained.
3. Restarted again: the remaining message displayed and the deleted ID returned not found.

All three processes exited with status 0. The actual output is in CONSOLE_RUN.txt; Maven's per-class test summaries are in junit-results/.

Tests also cover array alignment after first/middle/last deletion, failed disk writes, malformed/duplicate JSON, old Part 2 records, hash collisions, empty/not-found results, login gating, cancelled deletion and message limits.

**External steps still required:** opening in your own NetBeans installation, pushing the repository, six genuine Part 3 commits, a successful hosted Actions run, your own-voice video and Arc submission. No hosted-run or submission evidence is claimed by this local test report.
