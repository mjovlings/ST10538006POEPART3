package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs the actual Part 3 menu, including confirmation and returning to main. */
class Part3ConsoleTest {
    @TempDir Path folder;
    private static final String START = "Kyle\nSmith\nkyl_1\nCh&&sec@ke99!\n+27838968976\nkyl_1\nCh&&sec@ke99!\n";
    private String run(String input) {
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        new Main(new Scanner(input), new PrintStream(capture, true, StandardCharsets.UTF_8),
                folder.resolve("stored_messages.json")).run();
        return capture.toString(StandardCharsets.UTF_8);
    }
    private void seed() throws Exception {
        MessageData record = new MessageData("0012345678", 0, "+27838884567", "Hi there", "00:0:HITHERE");
        record.setSender("+27838968976");
        new MessageStore(folder.resolve("stored_messages.json")).append(record);
    }
    @Test void mainOptionFourAndEveryReadOptionWork() throws Exception {
        seed();
        String output = run(START + "0\n4\n1\n2\n3\n0012345678\n4\n+27838884567\n6\n7\n8\n+27838884567\n0\n3\n");
        assertTrue(output.contains("4) Stored Messages"));
        assertTrue(output.contains("Sender: +27838968976 | Recipient: +27838884567"));
        assertTrue(output.contains("Message Hash: 00:0:HITHERE"));
        assertTrue(output.contains("Message: Hi there"));
        assertTrue(output.contains("No messages sent."));
        assertTrue(output.contains("Goodbye."));
    }
    @Test void confirmedDeletionIsPersistent() throws Exception {
        seed();
        String output = run(START + "0\n4\n5\n00:0:HITHERE\n0\n6\n0\n3\n");
        assertTrue(output.contains("Message: \"Hi there\" successfully deleted."));
        assertTrue(output.contains("No stored messages."));
        assertEquals(0, new MessageStore(folder.resolve("stored_messages.json")).readMessages().length);
    }
    @Test void cancelledDeletionKeepsStoredRecord() throws Exception {
        seed();
        assertTrue(run(START + "0\n4\n5\n00:0:HITHERE\n1\n0\n3\n").contains("Deletion cancelled."));
        assertEquals(1, new MessageStore(folder.resolve("stored_messages.json")).readMessages().length);
    }
    @Test void emptyMissingAndInvalidChoicesDoNotCrash() {
        String output = run(START + "0\n4\nx\n9\n1\n2\n3\nmissing\n4\nmissing\n5\nmissing\n6\n7\n8\nmissing\n0\n3\n");
        assertTrue(output.contains("Please enter a whole number from 0 to 8."));
        assertTrue(output.contains("Message ID not found."));
        assertTrue(output.contains("Message hash not found."));
        assertTrue(output.contains("No stored messages."));
        assertTrue(output.contains("Goodbye."));
    }
    @Test void newStoredMessageImmediatelyAppearsInReports() {
        String output = run(START + "1\n1\n+27838884567\nNewly stored text\n3\n4\n6\n0\n3\n");
        assertTrue(output.contains("Message successfully stored."));
        assertTrue(output.contains("Message: Newly stored text"));
        assertTrue(output.contains("Sender: +27838968976"));
    }
}
