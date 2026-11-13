package com.mycompany.main;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Part 1-3 messaging service: validation, actions, arrays and reports. */
public class Message {
    public static final String RECIPIENT_OK = "Cell phone number successfully captured.";
    public static final String RECIPIENT_ERROR = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    private final Login login;
    private final MessageStore store;
    private final SecureRandom random = new SecureRandom();
    private final Set<String> usedIds = new HashSet<>();
    private final MessageArrays arrays = new MessageArrays();
    private int totalSent;

    public Message(Login login, MessageStore store) throws IOException {
        this.login = login;
        this.store = store;
        for (MessageData message : store.readMessages()) {
            arrays.add(message);
            usedIds.add(message.getMessageId());
        }
    }

    public boolean checkMessageID(String id) {
        return id != null && id.matches("[0-9]{10}");
    }

    public String checkRecipientCell(String recipient) {
        return Login.checkCellPhoneNumber(MessageArrays.normaliseRecipient(recipient)) ? RECIPIENT_OK : RECIPIENT_ERROR;
    }

    public String checkMessageLength(String text) {
        if (text == null || text.isBlank()) return "Please enter a non-empty message.";
        if (text.length() > 250) {
            return "Message exceeds 250 characters by " + (text.length() - 250)
                    + "; please reduce the size.";
        }
        return "Message ready to send.";
    }

    public String generateMessageID() {
        String id;
        do {
            StringBuilder digits = new StringBuilder();
            // A loop generates all ten random digits, preserving leading zeroes.
            for (int digit = 0; digit < 10; digit++) digits.append(random.nextInt(10));
            id = digits.toString().substring(0, 10);
        } while (!usedIds.add(id));
        return id;
    }

    public String createMessageHash(String id, int number, String text) {
        if (!checkMessageID(id) || number < 0 || text == null || text.isBlank()) {
            throw new IllegalArgumentException("A ten-digit ID, non-negative number and message are required.");
        }
        // Remove punctuation to reproduce HITONIGHT, not HITONIGHT?.
        String wordsOnly = text.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
        String[] words = wordsOnly.isEmpty() ? new String[]{""} : wordsOnly.split("\\s+");
        return (id.substring(0, 2) + ":" + number + ":" + words[0]
                + words[words.length - 1]).toUpperCase(Locale.ROOT);
    }

    public MessageData createMessage(int number, String recipient, String text) {
        requireLogin();
        if (!"Message ready to send.".equals(checkMessageLength(text))) {
            throw new IllegalArgumentException(checkMessageLength(text));
        }
        String id = generateMessageID();
        MessageData message = new MessageData(id, number, recipient, text, createMessageHash(id, number, text));
        message.setSender(login.getRegisteredCellPhone());
        return message;
    }

    // Capitalisation and the spelling returnTotalMessagess follow the brief.
    public String SentMessage(MessageData message, int choice) throws IOException {
        requireLogin();
        if (!"Pending".equals(message.getStatus())) return "This message has already been processed.";
        if (choice == 2) return "Press 0 to delete the message.";
        if (choice != 1 && choice != 3) return "Invalid option. Please choose 1, 2 or 3.";
        if (!Login.checkCellPhoneNumber(MessageArrays.normaliseRecipient(message.getRecipient()))) return RECIPIENT_ERROR;
        String validation = checkMessageLength(message.getMessage());
        if (!"Message ready to send.".equals(validation)) return validation;
        if (choice == 3) {
            storeMessage(message);
            return "Message successfully stored.";
        }
        message.setStatus("Sent");
        arrays.add(message);
        totalSent++;
        return "Message successfully sent.";
    }

    public String disregardMessage(MessageData message, int confirmation) {
        requireLogin();
        if (!"Pending".equals(message.getStatus())) return "This message has already been processed.";
        if (confirmation != 0) return "Deletion cancelled.";
        message.setStatus("Disregarded");
        arrays.add(message);
        return "Message disregarded.";
    }

    public void storeMessage(MessageData message) throws IOException {
        requireLogin();
        if (!"Pending".equals(message.getStatus())
                || !Login.checkCellPhoneNumber(MessageArrays.normaliseRecipient(message.getRecipient()))
                || !"Message ready to send.".equals(checkMessageLength(message.getMessage()))) {
            throw new IllegalArgumentException("Only a valid pending message can be stored.");
        }
        store.append(message); // Change status only after the write succeeds.
        message.setStatus("Stored");
        arrays.add(message);
    }

    public String printMessages() {
        StringBuilder report = new StringBuilder();
        for (MessageData message : arrays.forStatus("Sent")) {
            if (report.length() > 0) report.append("\n\n");
            report.append(message.details());
        }
        return report.length() == 0 ? "No messages sent." : report.toString();
    }

    public int returnTotalMessagess() { return totalSent; }

    public MessageArrays getArrays() { return arrays; }

    public String displayStoredParticipants() {
        requireLogin();
        StringBuilder output = new StringBuilder();
        for (MessageData message : arrays.forStatus("Stored")) {
            output.append("Sender: ").append(message.getSender())
                    .append(" | Recipient: ").append(message.getRecipient()).append("\n");
        }
        return output.length() == 0 ? "No stored messages." : output.toString().stripTrailing();
    }

    public String displayLongestStored() {
        requireLogin();
        MessageData longest = arrays.longestStored();
        return longest == null ? "No stored messages." : longest.getMessage();
    }

    public String searchMessageId(String id) {
        requireLogin();
        MessageData found = arrays.findById(id);
        return found == null ? "Message ID not found." : "Recipient: " + found.getRecipient()
                + "\nMessage: " + found.getMessage();
    }

    public String searchRecipient(String recipient, boolean storedOnly) {
        requireLogin();
        return report(arrays.findByRecipient(recipient, storedOnly), "No messages found for this recipient.");
    }

    public String deleteByHash(String hash) throws IOException {
        return deleteByHash(hash, null);
    }

    // An ID disambiguates collisions in the assignment's non-unique hash format.
    public String deleteByHash(String hash, String selectedId) throws IOException {
        requireLogin();
        MessageData[] matches = arrays.findByHash(hash);
        if (matches.length == 0) return "Message hash not found.";
        MessageData selected = matches[0];
        if (matches.length > 1 && selectedId == null) {
            return "Multiple messages share this hash. Specify a message ID.\n"
                    + report(matches, "");
        }
        if (selectedId != null) {
            selected = null;
            for (MessageData match : matches) {
                if (match.getMessageId().equals(selectedId)) selected = match;
            }
            if (selected == null) return "Message ID does not match this hash.";
        }
        if (selected.getStatus().equals("Stored")) store.removeById(selected.getMessageId());
        arrays.removeById(selected.getMessageId());
        return "Message: \"" + selected.getMessage() + "\" successfully deleted.";
    }

    public String storedReport() {
        requireLogin();
        return report(arrays.forStatus("Stored"), "No stored messages.");
    }

    public String sentReport() {
        requireLogin();
        return report(arrays.forStatus("Sent"), "No messages sent.");
    }

    private String report(MessageData[] messages, String emptyMessage) {
        StringBuilder output = new StringBuilder();
        for (MessageData message : messages) {
            if (output.length() > 0) output.append("\n\n");
            output.append(message.fullDetails());
        }
        return output.length() == 0 ? emptyMessage : output.toString();
    }

    private void requireLogin() {
        if (!login.isLoggedIn()) throw new IllegalStateException("Please log in before sending messages.");
    }
}
