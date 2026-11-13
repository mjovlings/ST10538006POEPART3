# Final PoE / Part 3 rubric coverage

Source: IIE (2026), PROG5121/p/w PoE, pp. 17-19 and 25-27. This is an implementation evidence map, not an awarded mark.

| Criterion | Marks | Evidence |
| --- | ---: | --- |
| Arrays correctly populated | 10 | MessageArrays maintains actual String arrays for sent, disregarded and stored text, plus parallel message-ID, hash and record arrays. Populated from user actions and JSON. Part3Test verifies the exact five-record scenario and index alignment. |
| Longest-message details | 5 | longestStored searches records through aligned indices; displayLongestStored returns the required text. Empty and equal-length cases tested. Full details are available in the stored report. |
| Search by recipient | 10 | findByRecipient loops over the parallel arrays; stored-only and sent-or-stored modes. Exact assignment output and not-found tests. ID lookup is also implemented. |
| Delete by hash | 10 | Hash-array lookup, confirmation, collision disambiguation by ID, persistent deletion then aligned array shifting. First/middle/last/only-record, cancellation and failed-write tests. |
| Read JSON into arrays and display | 10 | MessageStore.readMessages uses attributed Gson array deserialization. Message constructor populates MessageArrays. Restart, legacy format and malformed-file tests. |
| Display message report | 10 | Stored and sent reports show ID, hash, recipient, text, sender, number and status. Exact report content tests and console coverage. |
| Unit tests | 15 | Required Part 3 cases plus boundaries/failure cases, with all previous tests retained. Actual results are in TEST_RESULTS.md. |
| Automated unit tests | 5 | Updated .github/workflows/TestJava.yml runs mvn clean verify for all five test classes. A hosted run must still be completed in your repository. |
| Whole application | 15 | Registration, login protection, message limit, send/store/disregard, Part 3 submenu, persistence and quit integrated; console tests and runnable-JAR restart verification. |
| Coding standards | 10 | Separate classes, named methods, comments, shared validation, actual datatypes, bounded loops, defensive array copies and clear error messages. |
| **Total** | **100** | Subject to the marker's assessment and external submission evidence. |

## Final checks

- [ ] Open the project in your own IDE and run it.
- [ ] Run all tests locally and understand the exact fixture values.
- [ ] Demonstrate sender/recipient, longest message, ID search, recipient search, deletion and both reports.
- [ ] Demonstrate that stored data survives a restart and deleted data stays deleted.
- [ ] Push to GitHub with at least six genuine commits for Part 3.
- [ ] Check the successful GitHub Actions run on the submitted commit.
- [ ] Submit the repository link on Arc.
- [ ] Record and submit the required video using your own voice.
- [ ] Include the assessment information required by your submission portal and follow its AI-disclosure requirements.

The brief specifies a 5% penalty for absent/incorrect GitHub usage. No code package can substitute for the hosted repository and presentation evidence.
