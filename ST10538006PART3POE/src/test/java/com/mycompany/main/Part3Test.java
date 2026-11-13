package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The five supplied Part 3 records are fixtures only, never runtime seed data. */
class Part3Test {
    @TempDir Path folder;
    private Login login;
    private MessageStore store;
    private Message messages;
    private MessageData[] fixtures;
    private static final String LONG = "Where are you? You are late! I have asked you to be on time.";
    private static final String LEAVING = "Ok, I am leaving without you.";

    @BeforeEach void setUp() throws IOException {
        login = new Login();
        login.registerUser("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        login.loginUser("kyl_1", "Ch&&sec@ke99!");
        store = new MessageStore(folder.resolve("stored_messages.json"));
        messages = new Message(login, store);
    }

    private MessageData fixture(String id, int number, String recipient, String text) {
        MessageData record = new MessageData(id, number, recipient, text,
                messages.createMessageHash(id, number, text));
        record.setSender(login.getRegisteredCellPhone());
        return record;
    }

    private void populate() throws IOException {
        fixtures = new MessageData[]{
            fixture("1000000001", 0, "+27834557896", "Did you get the cake?"),
            fixture("2000000002", 1, "+27838884567", LONG),
            fixture("3000000003", 2, "+27834484567", "Yohoooo, I am at your gate."),
            // Page 19 uses this value as the message-ID fixture. Real IDs stay random.
            fixture("0838884567", 3, "0838884567", "It is dinner time!"),
            fixture("5000000005", 4, "+27838884567", LEAVING)
        };
        messages.SentMessage(fixtures[0], 1);
        messages.SentMessage(fixtures[1], 3);
        messages.SentMessage(fixtures[2], 2);
        messages.disregardMessage(fixtures[2], 0);
        messages.SentMessage(fixtures[3], 1);
        messages.SentMessage(fixtures[4], 3);
    }

    @Test void requiredSentArrayContainsExactMessages() throws IOException {
        populate();
        assertArrayEquals(new String[]{"Did you get the cake?", "It is dinner time!"},
                messages.getArrays().getSentMessages());
        assertEquals("Did you get the cake?", messages.getArrays().getSentMessages()[0]);
        assertEquals("It is dinner time!", messages.getArrays().getSentMessages()[1]);
    }
    @Test void allFiveArraysArePopulatedAndAligned() throws IOException {
        populate();
        MessageArrays arrays = messages.getArrays();
        assertArrayEquals(new String[]{LONG, LEAVING}, arrays.getStoredMessages());
        assertArrayEquals(new String[]{"Yohoooo, I am at your gate."}, arrays.getDisregardedMessages());
        assertEquals(5, arrays.getMessageIds().length);
        for (int i = 0; i < fixtures.length; i++) {
            assertEquals(fixtures[i].getMessageId(), arrays.getMessageIds()[i]);
            assertEquals(fixtures[i].getMessageHash(), arrays.getMessageHashes()[i]);
            assertEquals(fixtures[i].getMessage(), arrays.findById(arrays.getMessageIds()[i]).getMessage());
        }
    }
    @Test void requiredLongestMessage() throws IOException {
        populate();
        assertEquals(LONG, messages.displayLongestStored());
    }
    @Test void requiredIdSearchReturnsRecipientAndMessage() throws IOException {
        populate();
        assertEquals("Recipient: 0838884567\nMessage: It is dinner time!", messages.searchMessageId("0838884567"));
    }
    @Test void requiredRecipientReturnsBothStoredMessages() throws IOException {
        populate();
        MessageData[] matches = messages.getArrays().findByRecipient("+27838884567", false);
        assertEquals(2, matches.length);
        assertEquals(LONG, matches[0].getMessage());
        assertEquals(LEAVING, matches[1].getMessage());
        assertEquals(fixtures[1].fullDetails() + "\n\n" + fixtures[4].fullDetails(),
                messages.searchRecipient("+27838884567", true));
    }
    @Test void recipientSearchUsesExactRecordedValue() throws IOException {
        populate();
        assertEquals(1, messages.getArrays().findByRecipient("0838884567", false).length);
        assertEquals(0, messages.getArrays().findByRecipient("0838884567", true).length);
        assertEquals(2, messages.getArrays().findByRecipient("+27838884567", false).length);
    }
    @Test void requiredDeleteByHashRemovesExactMessage() throws IOException {
        populate();
        assertEquals("Message: \"" + LONG + "\" successfully deleted.", messages.deleteByHash(fixtures[1].getMessageHash()));
        assertEquals("Message ID not found.", messages.searchMessageId(fixtures[1].getMessageId()));
        assertArrayEquals(new String[]{LEAVING}, messages.getArrays().getStoredMessages());
        assertEquals(1, store.readMessages().length);
        assertEquals(LEAVING, store.readMessages()[0].getMessage());
    }
    @Test void requiredSentReportIncludesHashRecipientAndMessage() throws IOException {
        populate();
        assertEquals(fixtures[0].fullDetails() + "\n\n" + fixtures[3].fullDetails(), messages.sentReport());
        assertTrue(messages.sentReport().contains("Message Hash: " + fixtures[0].getMessageHash()));
        assertFalse(messages.sentReport().contains(LONG));
    }
    @Test void storedReportContainsAllFieldsAndOnlyStoredRecords() throws IOException {
        populate();
        assertEquals(fixtures[1].fullDetails() + "\n\n" + fixtures[4].fullDetails(), messages.storedReport());
        for (String label : new String[]{"Message ID:", "Message Hash:", "Recipient:", "Message:", "Sender:", "Message Number:", "Status: Stored"}) {
            assertTrue(messages.storedReport().contains(label));
        }
    }
    @Test void storedSenderAndRecipientShownForEveryRecord() throws IOException {
        populate();
        String line = "Sender: +27838968976 | Recipient: +27838884567";
        assertEquals(line + "\n" + line, messages.displayStoredParticipants());
    }
    @Test void senderIsTakenFromLoggedInAccount() throws IOException {
        MessageData message = messages.createMessage(0, "+27838884567", "Sender test");
        messages.storeMessage(message);
        assertEquals("+27838968976", store.readMessages()[0].getSender());
    }
    @Test void restartLoadsJsonIntoArraysAndRetainsDeletion() throws IOException {
        populate();
        messages.deleteByHash(fixtures[1].getMessageHash());
        Message restart = new Message(login, new MessageStore(folder.resolve("stored_messages.json")));
        assertArrayEquals(new String[]{LEAVING}, restart.getArrays().getStoredMessages());
        assertEquals(1, restart.getArrays().getMessageIds().length);
        assertEquals(0, restart.getArrays().getSentMessages().length);
        assertEquals(0, restart.returnTotalMessagess());
        assertTrue(restart.displayStoredParticipants().contains("+27838968976"));
        assertEquals("Message ID not found.", restart.searchMessageId(fixtures[1].getMessageId()));
    }
    @Test void legacyPart2FileWithoutSenderIsReadable() throws IOException {
        MessageData legacy = fixture("1234567890", 0, "+27838884567", "Legacy text");
        legacy.setSender(null);
        messages.storeMessage(legacy);
        Message restart = new Message(login, store);
        assertTrue(restart.displayStoredParticipants().contains("Unknown (Part 2 record)"));
        assertEquals("Legacy text", restart.displayLongestStored());
    }
    @Test void emptyAndNotFoundResultsAreClear() throws IOException {
        assertEquals("No stored messages.", messages.storedReport());
        assertEquals("No stored messages.", messages.displayLongestStored());
        assertEquals("No stored messages.", messages.displayStoredParticipants());
        assertEquals("No messages sent.", messages.sentReport());
        assertEquals("Message ID not found.", messages.searchMessageId("0000000000"));
        assertEquals("Message hash not found.", messages.deleteByHash("XX"));
        assertEquals("No messages found for this recipient.", messages.searchRecipient("+27838884567", false));
    }
    @Test void tiesReturnFirstStoredRecordAndIgnoreLongerSentMessages() throws IOException {
        messages.SentMessage(messages.createMessage(0, "+27838884567", "a very long sent message"), 1);
        MessageData first = messages.createMessage(1, "+27838884567", "First");
        messages.storeMessage(first);
        messages.storeMessage(messages.createMessage(2, "+27838884567", "Other"));
        assertEquals("First", messages.displayLongestStored());
    }
    @Test void deleteFirstMiddleLastAndOnlyRecordKeepsParallelArraysAligned() throws IOException {
        populate();
        for (int index : new int[]{0, 2, 4, 1, 3}) {
            messages.deleteByHash(fixtures[index].getMessageHash());
            MessageArrays arrays = messages.getArrays();
            assertEquals(arrays.getMessageIds().length, arrays.getMessageHashes().length);
            for (int i = 0; i < arrays.getMessageIds().length; i++) {
                assertEquals(arrays.findById(arrays.getMessageIds()[i]).getMessageHash(), arrays.getMessageHashes()[i]);
            }
        }
        assertEquals(0, messages.getArrays().getMessageIds().length);
        assertEquals(0, messages.getArrays().getSentMessages().length);
        assertEquals(0, messages.getArrays().getDisregardedMessages().length);
        assertEquals(0, messages.getArrays().getStoredMessages().length);
        assertEquals(0, store.readMessages().length);
        assertEquals(2, messages.returnTotalMessagess()); // Historical sends do not become unsent.
    }
    @Test void failedDiskDeletionRetainsAllArrays() throws IOException {
        populate();
        Files.writeString(folder.resolve("stored_messages.json"), "{broken");
        assertThrows(IOException.class, () -> messages.deleteByHash(fixtures[1].getMessageHash()));
        assertArrayEquals(new String[]{LONG, LEAVING}, messages.getArrays().getStoredMessages());
        assertEquals(5, messages.getArrays().getMessageIds().length);
        assertEquals("{broken", Files.readString(folder.resolve("stored_messages.json")));
    }
    @Test void duplicateHashRequiresIdAndDeletesOnlySelectedRecord() throws IOException {
        MessageData first = fixture("1100000001", 0, "+27838884567", "Hi one end");
        MessageData second = fixture("1100000002", 0, "+27838884567", "Hi two end");
        messages.storeMessage(first);
        messages.storeMessage(second);
        assertTrue(messages.deleteByHash(first.getMessageHash()).startsWith("Multiple messages"));
        assertEquals(2, store.readMessages().length);
        assertEquals("Message ID does not match this hash.", messages.deleteByHash(first.getMessageHash(), "0000000000"));
        assertEquals("Message: \"Hi two end\" successfully deleted.", messages.deleteByHash(first.getMessageHash(), second.getMessageId()));
        assertEquals(first.getMessageId(), store.readMessages()[0].getMessageId());
    }
    @Test void invalidJsonRecordsAreRejectedWithoutOverwrite() throws IOException {
        Path file = folder.resolve("stored_messages.json");
        for (String json : new String[]{"[null]", "[{}]", "[5]", "[\"bad\"]", "null", "{broken"}) {
            Files.writeString(file, json);
            assertThrows(IOException.class, () -> new Message(login, store), json);
            assertEquals(json, Files.readString(file));
        }
    }
    @Test void duplicateIdsInJsonAreRejected() throws IOException {
        populate();
        var json = store.read();
        json.add(json.get(0).deepCopy());
        Files.writeString(folder.resolve("stored_messages.json"), json.toString());
        assertThrows(IOException.class, store::readMessages);
    }
    @Test void arrayGettersDoNotAllowCallerToOverwriteIndex() throws IOException {
        populate();
        String original = messages.getArrays().getMessageIds()[0];
        messages.getArrays().getMessageIds()[0] = "changed";
        messages.getArrays().getSentMessages()[0] = "changed";
        assertEquals(original, messages.getArrays().getMessageIds()[0]);
        assertEquals("Did you get the cake?", messages.getArrays().getSentMessages()[0]);
    }
    @Test void sourceTableSpacingIsPreservedRatherThanSilentlyRewritten() throws IOException {
        MessageData raw = messages.createMessage(0, "0838884567", "It is dinner time !");
        assertEquals("Message successfully sent.", messages.SentMessage(raw, 1));
        assertEquals("It is dinner time !", messages.getArrays().getSentMessages()[0]);
    }
    @Test void newReportsAndDeletionRequireAuthentication() throws IOException {
        populate();
        login.loginUser("kyl_1", "wrong");
        assertThrows(IllegalStateException.class, messages::storedReport);
        assertThrows(IllegalStateException.class, messages::sentReport);
        assertThrows(IllegalStateException.class, messages::displayLongestStored);
        assertThrows(IllegalStateException.class, () -> messages.searchMessageId("0838884567"));
        assertThrows(IllegalStateException.class, () -> messages.deleteByHash(fixtures[1].getMessageHash()));
    }
}
