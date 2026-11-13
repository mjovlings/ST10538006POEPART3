package com.mycompany.main;

/** The details of one message; identity and loop number remain unchanged. */
public class MessageData {
    private final String messageId;
    private final int messageNumber;
    private String recipient;
    private final String message;
    private final String messageHash;
    private String status = "Pending";
    private String sender;

    MessageData(String id, int number, String recipient, String text, String hash) {
        messageId = id;
        messageNumber = number;
        this.recipient = recipient;
        message = text;
        messageHash = hash;
    }

    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public String getMessageHash() { return messageHash; }
    public String getSender() {
        return sender == null || sender.isBlank() ? "Unknown (Part 2 record)" : sender;
    }
    void setSender(String sender) { this.sender = sender; }

    public String fullDetails() {
        return details() + "\nSender: " + getSender() + "\nMessage Number: "
                + messageNumber + "\nStatus: " + status;
    }

    public String getStatus() { return status; }
    void setRecipient(String recipient) { this.recipient = recipient; }
    void setStatus(String status) { this.status = status; }

    public String details() {
        return "Message ID: " + messageId + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient + "\nMessage: " + message;
    }
}
