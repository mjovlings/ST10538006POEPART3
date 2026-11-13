# Part 3 design decisions

## Real arrays and parallel indexing

MessageArrays owns six real arrays: String[] sentMessages, disregardedMessages, storedMessages, messageIds and messageHashes, and MessageData[] records. The first three contain text grouped by final status. The last three are parallel: the same index always refers to one message. Lookup methods use loops over these arrays. Deletion shifts every parallel array together and rebuilds the grouped text arrays.

Arrays.copyOf grows/shortens arrays (Oracle, n.d.c); no ArrayList is used for message storage. Test fixtures are restricted to test classes. Runtime records originate from user input and the JSON file. A draft enters the arrays when it is sent, stored or confirmed disregarded.

## JSON loading, deletion and sender

Gson converts JSON into MessageData[] (Google, n.d.). Every record is validated before loading; duplicate IDs, missing essential fields, invalid recipients or non-Stored status are rejected. Old Part 2 JSON is accepted without a sender field and displays Unknown (Part 2 record). New messages record the authenticated account's phone.

Deletion of a stored message writes the updated JSON successfully before changing arrays. A write failure retains the in-memory record. This prevents the report from claiming deletion that was not saved. A temporary file and replacement reduce the risk of a partial JSON write (Oracle, n.d.b). Sent/disregarded history remains session-only; stored history persists. The cumulative sent total records how many send actions occurred this session, so deleting a sent record does not reverse that historical count.

This remains a single-process, single-user coursework application. Simultaneous writers and cross-account access isolation are not implemented. A malformed stored file produces a clear startup error and is not overwritten. Back up and correct that file before restarting.

## Inconsistencies in the brief

| Wording/data | Implemented decision |
| --- | --- |
| Page 17 asks for stored reports; page 19 asks for sent reports | Both reports exist in the Part 3 submenu, with separate filters. |
| Page 19 refers to messages sent or stored for a recipient | Submenu option 8 searches both, while option 4 searches stored messages only. Disregarded records are excluded. |
| Message 4 has “Developer: 0838884567”, although Part 2 requires +27 | Treat Developer as the recipient label. Part 3 accepts a valid ten-digit local recipient as well as +27 format. Validation converts a local number temporarily to +27 form; the entered value remains unchanged. Registration still requires +27. The invalid eleven-digit Part 2 example is still rejected. |
| Local 0838884567 represents the same number as +27838884567, but the expected recipient query returns only two stored records | Recipient lookup compares the exact recorded string, matching the supplied expected output. Query local-format records using their local value. It does not silently merge phone-format aliases. |
| Page 19 supplies 0838884567 in an ID-search case without a generated ID | Unit tests assign this ten-digit string as a deterministic ID for message 4. Runtime IDs are always generated; users copy actual IDs from a report. |
| Page 18 says “It is dinner time !”; page 19 expects “It is dinner time!” | Required-output fixtures use the page 19 spelling. An additional test verifies that user input containing the page 18 space is preserved. |
| The deletion expectation omits the original message's final period | The confirmation includes the actual original message unchanged inside quotation marks. It does not truncate the stored text. |
| The rubric refers to a “Task Class” | The workflow runs the actual messaging application's complete test suite; no unrelated Task class is invented. |

## Retained Part 2 choices

- Ten-digit IDs are Strings, preserving leading zeroes. Random digits and substring meet the generation/manipulation requirements; IDs already present in the session or stored JSON are excluded.
- The hash uses the first two ID digits, zero-based message number, and punctuation-stripped first/last words in uppercase. Text itself remains unchanged. A deterministic test ID gives the required 00:0:HITONIGHT fixture.
- The prescribed hash is not globally unique. When two records share a hash, deletion requires the matching message ID instead of deleting an arbitrary record.
- Exactly 250 UTF-16 code units are accepted, following the stated maximum. Detailed validation messages follow the expected-response table.
- Sent, stored and disregarded drafts consume the requested entry limit; only send actions increase the sent total.
- Part 2 main option 2 retains Coming Soon. Part 3 exposes the working sent report through main option 4.
- Password input uses Scanner and is visible, as in the starting exercise. This is an educational console simulation, not a production authentication or messaging service.

See REFERENCES.md for source details and assistance disclosure.
