package com.mycompany.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Stores pending messages in a JSON array, preserving previous messages. */
public class MessageStore {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MessageStore(Path file) { this.file = file.toAbsolutePath(); }

    public JsonArray read() throws IOException {
        if (!Files.exists(file)) return new JsonArray();
        try {
            JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (!root.isJsonArray()) throw new IOException("Stored messages must be a JSON array.");
            return root.getAsJsonArray();
        } catch (JsonParseException ex) {
            throw new IOException("Stored message file contains invalid JSON; it has not been overwritten.", ex);
        }
    }

    public void append(MessageData message) throws IOException {
        // Gson object conversion and pretty printing adapted from Google (n.d.),
        // Gson User Guide: https://google.github.io/gson/UserGuide.html
        // File replacement uses Java Files API; see docs/REFERENCES.md.
        readMessages();
        JsonArray messages = read();
        for (JsonElement item : messages) {
            if (item.getAsJsonObject().get("messageId").getAsString().equals(message.getMessageId())) {
                throw new IOException("Duplicate stored message ID.");
            }
        }
        JsonObject stored = gson.toJsonTree(message).getAsJsonObject();
        stored.addProperty("status", "Stored");
        messages.add(stored);
        write(messages);
    }

    /** Gson array deserialization adapted from Google, Gson User Guide, Array Examples.
     * https://google.github.io/gson/UserGuide.html (accessed 28 October 2026).
     */
    public MessageData[] readMessages() throws IOException {
        try {
            MessageData[] messages = gson.fromJson(read(), MessageData[].class);
            java.util.Set<String> ids = new java.util.HashSet<>();
            for (MessageData message : messages) {
                if (message == null || message.getMessageId() == null
                        || !message.getMessageId().matches("[0-9]{10}")
                        || !ids.add(message.getMessageId()) || message.getMessageNumber() < 0
                        || message.getMessage() == null || message.getMessage().isBlank()
                        || message.getMessage().length() > 250
                        || !Login.checkCellPhoneNumber(MessageArrays.normaliseRecipient(message.getRecipient()))
                        || message.getMessageHash() == null || message.getMessageHash().isBlank()
                        || !"Stored".equals(message.getStatus())) {
                    throw new IOException("Invalid or duplicate stored message; file has not been changed.");
                }
            }
            return messages;
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException("Cannot load stored messages; file has not been changed.", ex);
        }
    }

    /** Persist removal first, so a failed write cannot leave arrays out of sync. */
    public void removeById(String id) throws IOException {
        readMessages(); // Validate before modifying existing data.
        JsonArray messages = read();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getAsJsonObject().get("messageId").getAsString().equals(id)) {
                messages.remove(i);
                write(messages);
                return;
            }
        }
        throw new IOException("Message no longer exists in storage. Restart to reload it.");
    }

    private void write(JsonArray messages) throws IOException {
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "quickchat-", ".tmp");
        try {
            Files.writeString(temporary, gson.toJson(messages), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
