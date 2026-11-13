# Part 3 presentation preparation

Record the real application and explain it using your own voice, as required by the assignment. These notes are a plan, not a completed video.

| Approximate time | Show | Explain |
| --- | --- | --- |
| 0:00-0:45 | Maven project and classes | How Parts 1-3 work together and why console input, message operations, arrays and JSON are separated. |
| 0:45-1:30 | Register; fail then succeed at login | Messaging is protected by successful login; new records use the registered phone as sender. |
| 1:30-3:00 | Enter the five README test messages | Demonstrate 2 sent, 2 stored, 1 disregarded and the limit. IDs are generated at runtime. |
| 3:00-4:00 | MessageArrays.java | Show the actual arrays and explain how ID, hash and record indices correspond. Grouped text arrays are populated from message status. |
| 4:00-5:00 | Stored Messages: participants and longest | Show both stored records and explain the loop that compares lengths; first record wins ties. |
| 5:00-6:00 | ID and recipient searches | Copy a real ID from the report. Query +27838884567. Explain exact recorded recipient matching and the two search scopes. |
| 6:00-7:00 | Hash deletion; inspect JSON | Preview the record, confirm with 0, show it disappears from the report and file. Explain that disk success comes before array removal. |
| 7:00-8:00 | Quit, restart, log in with limit 0 | Show the remaining stored message reloads and the deleted one stays absent. Sender remains the saved value. |
| 8:00-9:00 | Stored and sent reports; tests | Explain all fields and the distinction between current sent records and the historical send count. Run the JUnit suite and show required assertions. |
| 9:00-10:00 | TestJava.yml and your GitHub evidence | Show the actual hosted passing workflow and genuine commits after pushing. Explain push/pull-request triggers and mvn clean verify. |

Use your own explanations rather than reading unfamiliar code aloud. Discuss the documented test-data inconsistencies if asked. Submit the unlisted video and repository links according to your portal's instructions.
