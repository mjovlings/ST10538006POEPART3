package com.mycompany.main;

import java.util.Arrays;

/** Genuine arrays: records, IDs and hashes always share the same index. */
public class MessageArrays {
    private MessageData[] records = new MessageData[0];
    private String[] messageIds = new String[0];
    private String[] messageHashes = new String[0];
    private String[] sentMessages = new String[0];
    private String[] disregardedMessages = new String[0];
    private String[] storedMessages = new String[0];

    public void add(MessageData message) {
        if (findById(message.getMessageId()) != null) {
            throw new IllegalArgumentException("Duplicate message ID: " + message.getMessageId());
        }
        // Arrays.copyOf grows the actual array; no ArrayList backs these arrays.
        // Oracle Arrays API: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Arrays.html
        int index = records.length;
        records = Arrays.copyOf(records, index + 1);
        messageIds = Arrays.copyOf(messageIds, index + 1);
        messageHashes = Arrays.copyOf(messageHashes, index + 1);
        records[index] = message;
        messageIds[index] = message.getMessageId();
        messageHashes[index] = message.getMessageHash();
        rebuildStatusArrays();
    }

    private void rebuildStatusArrays() {
        sentMessages = textsForStatus("Sent");
        disregardedMessages = textsForStatus("Disregarded");
        storedMessages = textsForStatus("Stored");
    }

    private String[] textsForStatus(String status) {
        MessageData[] matches = forStatus(status);
        String[] texts = new String[matches.length];
        for (int i = 0; i < matches.length; i++) texts[i] = matches[i].getMessage();
        return texts;
    }

    public MessageData[] forStatus(String status) {
        MessageData[] matches = new MessageData[records.length];
        int count = 0;
        for (MessageData message : records) {
            if (message.getStatus().equals(status)) matches[count++] = message;
        }
        return Arrays.copyOf(matches, count);
    }

    public MessageData findById(String id) {
        for (int i = 0; i < messageIds.length; i++) {
            if (messageIds[i].equals(id)) return records[i];
        }
        return null;
    }

    public MessageData[] findByHash(String hash) {
        MessageData[] matches = new MessageData[records.length];
        int count = 0;
        for (int i = 0; i < messageHashes.length; i++) {
            if (messageHashes[i].equalsIgnoreCase(hash)) matches[count++] = records[i];
        }
        return Arrays.copyOf(matches, count);
    }

    public MessageData[] findByRecipient(String recipient, boolean storedOnly) {
        MessageData[] matches = new MessageData[records.length];
        int count = 0;
        // Exact stored-value lookup follows the supplied recipient-search fixture.
        for (int i = 0; i < messageIds.length; i++) {
            MessageData message = records[i];
            boolean included = message.getStatus().equals("Stored")
                    || (!storedOnly && message.getStatus().equals("Sent"));
            if (included && message.getRecipient().equals(recipient)) {
                matches[count++] = message;
            }
        }
        return Arrays.copyOf(matches, count);
    }

    public static String normaliseRecipient(String recipient) {
        if (recipient == null) return "";
        return recipient.matches("0[0-9]{9}") ? "+27" + recipient.substring(1) : recipient;
    }

    public MessageData longestStored() {
        MessageData longest = null;
        for (int i = 0; i < messageIds.length; i++) {
            MessageData message = records[i];
            if (message.getStatus().equals("Stored") && (longest == null
                    || message.getMessage().length() > longest.getMessage().length())) {
                longest = message;
            }
        }
        return longest; // First entry wins ties; null means no stored messages.
    }

    public void removeById(String id) {
        for (int i = 0; i < messageIds.length; i++) {
            if (messageIds[i].equals(id)) {
                // Shift all three parallel arrays together before shortening them.
                for (int j = i; j < records.length - 1; j++) {
                    records[j] = records[j + 1];
                    messageIds[j] = messageIds[j + 1];
                    messageHashes[j] = messageHashes[j + 1];
                }
                records = Arrays.copyOf(records, records.length - 1);
                messageIds = Arrays.copyOf(messageIds, messageIds.length - 1);
                messageHashes = Arrays.copyOf(messageHashes, messageHashes.length - 1);
                rebuildStatusArrays();
                return;
            }
        }
    }

    public String[] getSentMessages() { return sentMessages.clone(); }
    public String[] getDisregardedMessages() { return disregardedMessages.clone(); }
    public String[] getStoredMessages() { return storedMessages.clone(); }
    public String[] getMessageIds() { return messageIds.clone(); }
    public String[] getMessageHashes() { return messageHashes.clone(); }
}
