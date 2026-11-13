package com.mycompany.main;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Console entry point extending the user's Part 1 registration/login flow. */
public class Main {
    private final Scanner input;
    private final PrintStream output;
    private final Login login = new Login();
    private final Path storagePath;

    public Main(Scanner input, PrintStream output, Path storagePath) {
        this.input = input;
        this.output = output;
        this.storagePath = storagePath;
    }

    public static void main(String[] args) {
        new Main(new Scanner(System.in), System.out, Path.of("stored_messages.json")).run();
    }

    public void run() {
        try {
            registerUser();
            while (!login.isLoggedIn()) {
                String username = read("Enter your username to login (or /quit): ");
                if (username.equals("/quit")) return;
                String password = read("Enter your password to login: ");
                output.println(login.returnLoginStatus(login.loginUser(username, password)));
            }
            runChat();
        } catch (NoSuchElementException ex) {
            output.println("Input closed. Goodbye.");
        } catch (IOException ex) {
            output.println("Unable to open message storage: " + ex.getMessage());
        }
    }

    private void registerUser() {
        output.println("QuickChat registration");
        String first = readNonBlank("Enter your first name: ");
        String last = readNonBlank("Enter your last name: ");
        String username = read("Enter your username: ");
        while (!Login.checkUserName(username)) {
            output.println(Login.USER_ERROR);
            username = read("Enter your username: ");
        }
        output.println("Username successfully captured.");
        String password = read("Enter your password: ");
        while (!Login.checkPasswordComplexity(password)) {
            output.println(Login.PASSWORD_ERROR);
            password = read("Enter your password: ");
        }
        output.println("Password successfully captured.");
        String phone = read("Enter your cellphone number (+27): ");
        while (!Login.checkCellPhoneNumber(phone)) {
            output.println(Message.RECIPIENT_ERROR);
            phone = read("Enter your cellphone number (+27): ");
        }
        output.println("Cell phone number successfully added.");
        output.println(login.registerUser(first, last, username, password, phone));
    }

    private void runChat() throws IOException {
        Message messages = new Message(login, new MessageStore(storagePath));
        output.println("Welcome to QuickChat.");
        int messageLimit = readNumber("How many messages would you like to enter? ", 0, Integer.MAX_VALUE);
        int entered = 0;
        boolean running = true;
        // This while loop keeps the menu running until option 3 is selected.
        while (running) {
            output.println("\n1) Send Messages\n2) Show recently sent messages\n3) Quit\n4) Stored Messages");
            int choice = readNumber("Choose an option: ", 1, 4);
            switch (choice) {
                case 1:
                    if (entered == messageLimit) {
                        output.println("You have reached your message limit.");
                    } else {
                        // The counter supplies message numbers 0, 1, 2 ... exactly once.
                        for (int messageNumber = entered; messageNumber < messageLimit; messageNumber++) {
                            enterMessage(messages, messageNumber);
                            entered++;
                        }
                    }
                    output.println("Total messages sent: " + messages.returnTotalMessagess());
                    break;
                case 2:
                    output.println("Coming Soon.");
                    break;
                case 3:
                    output.println("Total messages sent: " + messages.returnTotalMessagess());
                    output.println("Goodbye.");
                    running = false;
                    break;
                case 4:
                    storedMenu(messages);
                    break;
                default:
                    break;
            }
        }
    }

    private void enterMessage(Message messages, int number) {
        output.println("\nMessage number: " + number);
        String recipient = read("Enter recipient number (+27 or local 0 format): ");
        output.println(messages.checkRecipientCell(recipient));
        String text = read("Enter message (maximum 250 characters): ");
        while (!"Message ready to send.".equals(messages.checkMessageLength(text))) {
            output.println(messages.checkMessageLength(text));
            text = read("Enter message (maximum 250 characters): ");
        }
        output.println(messages.checkMessageLength(text));
        MessageData message = messages.createMessage(number, recipient, text);
        output.println("Message ID generated: " + message.getMessageId());
        while (message.getStatus().equals("Pending")) {
            output.println("1) Send Message\n2) Disregard Message\n3) Store Message to send later");
            int action = readNumber("Choose an action: ", 1, 3);
            // An invalid recipient may be discarded but cannot be sent/stored.
            if (action != 2 && !Message.RECIPIENT_OK.equals(messages.checkRecipientCell(message.getRecipient()))) {
                output.println(Message.RECIPIENT_ERROR);
                message.setRecipient(read("Enter a valid recipient (+27 or local 0 format), or choose disregard next: "));
                if (!Message.RECIPIENT_OK.equals(messages.checkRecipientCell(message.getRecipient()))) continue;
                output.println(Message.RECIPIENT_OK);
            }
            try {
                output.println(messages.SentMessage(message, action));
                if (action == 1) output.println(message.details());
                if (action == 2) {
                    int confirm = readNumber("Enter 0 to delete, or 1 to keep editing: ", 0, 1);
                    output.println(messages.disregardMessage(message, confirm));
                }
            } catch (IOException ex) {
                output.println("Message was not stored: " + ex.getMessage());
                output.println("Please retry, send or disregard this message.");
            }
        }
    }

    private void storedMenu(Message messages) {
        boolean browsing = true;
        while (browsing) {
            output.println("\nStored Messages\n1) Display senders and recipients"
                    + "\n2) Longest stored message\n3) Search by message ID"
                    + "\n4) Search stored messages by recipient\n5) Delete by message hash"
                    + "\n6) Full stored-message report\n7) Full sent-message report"
                    + "\n8) Search sent or stored messages by recipient\n0) Back");
            int choice = readNumber("Choose a stored-message option: ", 0, 8);
            switch (choice) {
                case 0: browsing = false; break;
                case 1: output.println(messages.displayStoredParticipants()); break;
                case 2: output.println(messages.displayLongestStored()); break;
                case 3: output.println(messages.searchMessageId(read("Message ID: ").trim())); break;
                case 4: output.println(messages.searchRecipient(read("Recipient: ").trim(), true)); break;
                case 5: deleteMessage(messages); break;
                case 6: output.println(messages.storedReport()); break;
                case 7: output.println(messages.sentReport()); break;
                case 8: output.println(messages.searchRecipient(read("Recipient: ").trim(), false)); break;
                default: break;
            }
        }
    }

    private void deleteMessage(Message messages) {
        String hash = read("Message hash: ").trim();
        MessageData[] matches = messages.getArrays().findByHash(hash);
        if (matches.length == 0) { output.println("Message hash not found."); return; }
        String id = null;
        for (MessageData match : matches) output.println(match.fullDetails());
        if (matches.length > 1) id = read("Several messages match. Enter the ID to delete: ").trim();
        int confirm = readNumber("Enter 0 to confirm deletion, or 1 to cancel: ", 0, 1);
        if (confirm != 0) { output.println("Deletion cancelled."); return; }
        try {
            output.println(messages.deleteByHash(hash, id));
        } catch (IOException ex) {
            output.println("Deletion failed; message retained: " + ex.getMessage());
        }
    }

    private String read(String prompt) {
        output.print(prompt);
        return input.nextLine();
    }

    private String readNonBlank(String prompt) {
        String value = read(prompt).trim();
        while (value.isEmpty()) value = read(prompt).trim();
        return value;
    }

    private int readNumber(String prompt, int minimum, int maximum) {
        while (true) {
            try {
                int number = Integer.parseInt(read(prompt).trim());
                if (number >= minimum && number <= maximum) return number;
            } catch (NumberFormatException ex) {
                // Read a whole line again; invalid input never remains in Scanner.
            }
            output.println("Please enter a whole number from " + minimum + " to " + maximum + ".");
        }
    }
}
