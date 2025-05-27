package javaapplication10;

import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.security.MessageDigest;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;

public class SimpleChat {
    private static final String USERS_FILE = "users.txt";
    private static final String CHATS_FILE = "chats.txt";
    private static final String CONTACTS_FILE = "contacts.txt";
    private static final String REPORTS_FILE = "reports.txt";
    private static final String ADMIN_USERNAME = "O_fentse";
    private static final String ADMIN_PASSWORD = "@Rtmoepi7319";
    private static String currentUser = null;
    private static final int MESSAGE_LIMIT = 250;

    public static void main(String[] args) {
        initializeFiles();
        showMainMenu();
        
    }

    private static void initializeFiles() {
        String[] files = {USERS_FILE, CHATS_FILE, CONTACTS_FILE, REPORTS_FILE};
        for (String file : files) {
            try {
                Path path = Paths.get(file);
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
            } catch (IOException e) {
                showError("Error initializing " + file);
            }
        }
    }

    private static void showMainMenu() {
        while (true) {
            String[] options = {"Login", "Register", "Admin Login", "Exit"};
            int choice = JOptionPane.showOptionDialog(null, "Welcome to SimpleChat", "Main Menu",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            switch (choice) {
                case 0 -> login();
                case 1 -> register();
                case 2 -> adminLogin();
                case 3 -> System.exit(0);
                default -> { return; }
            }
        }
    }

   


private static void register() {
    String username = JOptionPane.showInputDialog("Enter username (must contain '_'):");
    if (username == null || !username.contains("_")) {
        showError("Username must contain '_'");
        return;
    }

    if (userExists(username)) {
        showError("Username already exists");
        return;
    }

    String fullName = JOptionPane.showInputDialog("Enter full name:");
    if (fullName == null || fullName.trim().isEmpty()) {
        showError("Full name is required");
        return;
    }
    
    String phoneNumber = JOptionPane.showInputDialog("Enter South African phone number (+27 format):");
    String formattedPhone = formatSAPhoneNumber(phoneNumber);
    if (formattedPhone == null) {
        showError("Invalid South African phone number format");
        return;
    }
    
    // Verify if phone number is already registered
    if (isPhoneNumberRegistered(formattedPhone)) {
        showError("This phone number is already registered");
        return;
    }
    
    String ageStr = JOptionPane.showInputDialog("Enter age:");
    int age;
    try {
        age = Integer.parseInt(ageStr);
        if (age < 13) {
            showError("Must be 13 or older to register");
            return;
        }
    } catch (NumberFormatException e) {
        showError("Invalid age");
        return;
    }

    String password = JOptionPane.showInputDialog(
        "Enter password\n(8+ chars, 1+ uppercase, 1+ number, 1+ special char):");
    if (!isValidPassword(password)) {
        showError("Invalid password format");
        return;
    }

    try {
        Files.write(
            Paths.get(USERS_FILE),
            (String.format("%s|%s|%s|%d|%s|%s|%s\n",
                username,
                hashPassword(password),
                fullName,
                age,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "user",
                formattedPhone
            )).getBytes(),
            StandardOpenOption.APPEND
        );
        JOptionPane.showMessageDialog(null, "Registration successful!");
    } catch (IOException e) {
        showError("Error saving user data");
    }
}

private static boolean isPhoneNumberRegistered(String phoneNumber) {
    try {
        List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 7 && parts[6].equals(phoneNumber)) {
                return true;
            }
        }
    } catch (IOException e) {
        showError("Error checking phone number");
    }
    return false;
}



// Validates if a phone number is a valid South African number




private static boolean verifyPhoneNumber(String phoneNumber) {
    // Generate a random 6-digit verification code
    String verificationCode = String.format("%06d", new Random().nextInt(1000000));
    
    // In a real application, you would send this code via SMS
    // For this demo, we'll show it in a dialog
    JOptionPane.showMessageDialog(null, 
        "Verification code sent to " + phoneNumber + "\n" +
        "Demo code: " + verificationCode);
    
    // Ask user to enter the code
    String enteredCode = JOptionPane.showInputDialog("Enter the verification code:");
    
    return verificationCode.equals(enteredCode);
}


// Formats a phone number to SA format


private static String formatSAPhoneNumber(String phoneNumber) {
// Remove all non-digit characters except +
phoneNumber = phoneNumber.replaceAll("[^\\d+]", "");
// If number starts with 0, replace with +27
if (phoneNumber.startsWith("0")) {
    phoneNumber = "+27" + phoneNumber.substring(1);
}

return isValidSAPhoneNumber(phoneNumber) ? phoneNumber : null;
}
private static void updatePhoneNumber() {
    String newPhone = JOptionPane.showInputDialog("Enter new South African phone number (+27 format):");
    String formattedPhone = formatSAPhoneNumber(newPhone);
    
    if (formattedPhone == null) {
        showError("Invalid South African phone number format");
        return;
    }
    
    if (isPhoneNumberRegistered(formattedPhone)) {
        showError("This phone number is already registered");
        return;
    }
    
    if (verifyPhoneNumber(formattedPhone)) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
            List<String> newLines = new ArrayList<>();
            
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(currentUser)) {
                    // Update phone number
                    parts[6] = formattedPhone;
                    newLines.add(String.join("|", parts));
                } else {
                    newLines.add(line);
                }
            }
            
            Files.write(Paths.get(USERS_FILE), newLines);
            JOptionPane.showMessageDialog(null, "Phone number updated successfully!");
        } catch (IOException e) {
            showError("Error updating phone number");
        }
    } else {
        showError("Phone verification failed");
    }
}

private static boolean isValidPassword(String password) {
if (password == null || password.length() < 8) {
    return false;
}
    return password.matches(".*[A-Z].*") &&
    password.matches(".*[0-9].*") &&
    password.matches(".*[^A-Za-z0-9].*");
}

private static String hashPassword(String password) {
try {
     MessageDigest digest = MessageDigest.getInstance("SHA-256");
     byte[] hash = digest.digest(password.getBytes());
     StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
           String hex = Integer.toHexString(0xff & b);
             if (hex.length() == 1) {
                 hexString.append('0');
                                    }
                 hexString.append(hex);
                          }
                 return hexString.toString();
     } catch (NoSuchAlgorithmException e) {
             return password;
                                          }
                                                   }

private static void login() {
String username = JOptionPane.showInputDialog("Enter username:");
if (username == null) {
return;
}

String password = JOptionPane.showInputDialog("Enter password:");
if (password == null) {
     return;
                      }

if (validateLogin(username, password)) {
     currentUser = username;
     userMenu();
                                       }
    else {
          showError("Invalid username or password");
          }
                            }

private static boolean validateLogin(String username, String password) {
try {
     List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
      for (String line : lines) {
          String[] parts = line.split("\\|");
          if (parts[0].equals(username) && parts[1].equals(hashPassword(password))) {
               return true;           
            } 
    }
    } catch (IOException e) {
showError("Error validating login");
}
return false;
}

private static void userMenu() {
while (true) {
String[] options = {
"Send Message",
"View Messages",
"Manage Contacts",
"View Profile",
"Report User",
"Logout"
};

int choice = JOptionPane.showOptionDialog(
null,
"Welcome " + currentUser,
"User Menu",
JOptionPane.DEFAULT_OPTION,
JOptionPane.INFORMATION_MESSAGE,
null,
options,
options[0]
);

switch (choice) {
case 0: sendMessage(); break;
case 1: viewMessages(); break;
case 2: manageContacts(); break;
case 3: viewProfile(); break;
case 4: reportUser(); break;
case 5: logout(); return;
default: return;
}
}
}

private static void sendMessage() {
String recipient = JOptionPane.showInputDialog("Enter recipient username:");
if (!userExists(recipient)) {
showError("Recipient not found");
return;
}

String message = JOptionPane.showInputDialog("Enter message (max " + MESSAGE_LIMIT + " chars):");
if (message == null || message.length() > MESSAGE_LIMIT) {
showError("Message must be between 1-" + MESSAGE_LIMIT + " characters");
return;
}

try {
String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
String messageHash = generateMessageHash(message);

Files.write(
Paths.get(CHATS_FILE),
(String.format("%s|%s|%s|%s|%s\n",
currentUser,
recipient,
message,
timestamp,
messageHash
)).getBytes(),
StandardOpenOption.APPEND
);
addContact(currentUser, recipient); 

JOptionPane.showMessageDialog(null, "Message sent successfully!");
} catch (IOException e) {
showError("Error sending message");
}
}

private static void viewMessages() {
String contact = chooseContact();
if (contact == null) {
return;
}

String countInput = JOptionPane.showInputDialog("How many messages to display?");
int count = 10;
try {
count = Integer.parseInt(countInput);
} catch (NumberFormatException e) {
// Use default count
}

try {
List<String> messages = Files.readAllLines(Paths.get(CHATS_FILE));
List<String> relevantMessages = new ArrayList<>();

for (String line : messages) {
String[] parts = line.split("\\|");
if ((parts[0].equals(currentUser) && parts[1].equals(contact)) ||
(parts[0].equals(contact) && parts[1].equals(currentUser))) {
relevantMessages.add(String.format("[%s] %s: %s",
parts[3], parts[0], parts[2]));
}
}

Collections.reverse(relevantMessages);
StringBuilder display = new StringBuilder();
for (int i = 0; i < Math.min(count, relevantMessages.size()); i++) {
display.append(relevantMessages.get(i)).append("\n");
}

JOptionPane.showMessageDialog(null,
display.toString().isEmpty() ? "No messages" : display.toString(),
"Chat with " + contact,
JOptionPane.INFORMATION_MESSAGE);
} catch (IOException e) {
showError("Error reading messages");
}
}

private static String chooseContact() {
    try {
        List<String> contacts = getContacts(currentUser);
        if (contacts.isEmpty()) {
            showError("No contacts found");
            return null;
        }

        String selected = (String) JOptionPane.showInputDialog(
            null,
            "Choose contact",
            "Select Contact",
            JOptionPane.QUESTION_MESSAGE,
            null,
            contacts.toArray(),
            contacts.get(0)
        );

        if (selected == null) {
            return null;
        }

        // Extract username from "username (+27...)"
        int index = selected.indexOf(" ");
        if (index > 0) {
            return selected.substring(0, index);  // returns just the username
        } else {
            return selected; // fallback: if no space found, return entire string
        }

    } catch (IOException e) {
        showError("Error loading contacts");
        return null;
    }
}

private static List<String> getContacts(String username) throws IOException {
    List<String> contacts = new ArrayList<>();
    List<String> lines = Files.readAllLines(Paths.get(CONTACTS_FILE));
    List<String> userLines = Files.readAllLines(Paths.get(USERS_FILE));

    for (String line : lines) {
        String[] parts = line.split("\\|");
        if (parts[0].equals(username)) {
            String contactUsername = parts[1];
            String phone = "Unknown";

            for (String userLine : userLines) {
                String[] userParts = userLine.split("\\|");
                if (userParts[0].equals(contactUsername)) {
                    if (userParts.length >= 7) {
                        phone = userParts[6];
                    }
                    break;
                }
            }

            contacts.add(contactUsername + " (" + phone + ")");
        }
    }

    return contacts;
}


private static void manageContacts() {
String[] options = {"View Contacts", "Add Contact", "Remove Contact", "Back"};
int choice = JOptionPane.showOptionDialog(
null,
"Contact Management",
"Contacts",
JOptionPane.DEFAULT_OPTION,
JOptionPane.INFORMATION_MESSAGE,
null,
options,
options[0]
);

switch (choice) {
case 0: viewContacts(); break;
case 1: addNewContact(); break;
case 2: removeContact(); break;
}
}

private static void viewContacts() {
try {
List<String> contacts = getContacts(currentUser);
if (contacts.isEmpty()) {
showError("No contacts found");
return;
}

StringBuilder display = new StringBuilder("Your contacts:\n\n");
for (String contact : contacts) {
display.append(contact).append("\n");
}
JOptionPane.showMessageDialog(null, display.toString());
} catch (IOException e) {

showError("Error loading contacts");
}
}

private static void addNewContact() {
String contact = JOptionPane.showInputDialog("Enter username to add:");
if (contact == null || !userExists(contact)) {
showError("User not found");
return;
}

if (addContact(currentUser, contact)) {
JOptionPane.showMessageDialog(null, "Contact added successfully!");
} else {
showError("Error adding contact");
}
}

private static boolean addContact(String user, String contact) {
try {
// Check if contact already exists
List<String> contacts = getContacts(user);
if (contacts.contains(contact)) {
return true; // Already exists
}

Files.write(
Paths.get(CONTACTS_FILE),
(user + "|" + contact + "\n").getBytes(),
StandardOpenOption.APPEND
);
return true;
} catch (IOException e) {
return false;
}
}

private static void removeContact() {
String contact = chooseContact();
if (contact == null) {
return;
}

try {
List<String> lines = Files.readAllLines(Paths.get(CONTACTS_FILE));
List<String> newLines = new ArrayList<>();

for (String line : lines) {
String[] parts = line.split("\\|");
if (!(parts[0].equals(currentUser) && parts[1].equals(contact))) {
newLines.add(line);
}
}

Files.write(Paths.get(CONTACTS_FILE), newLines);
JOptionPane.showMessageDialog(null, "Contact removed successfully!");
} catch (IOException e) {
showError("Error removing contact");
}
}

private static void viewProfile() {
    try {
        List<String> userLines = Files.readAllLines(Paths.get(USERS_FILE));
        StringBuilder profile = new StringBuilder();

        // Load current user info
        for (String line : userLines) {
            String[] parts = line.split("\\|");
            if (parts[0].equals(currentUser)) {
                profile.append(String.format(
                    "Username: %s\nFull Name: %s\nAge: %s\nRegistered: %s\nPhone: %s\n\n",
                    parts[0], parts[2], parts[3], parts[4], parts.length >= 7 ? parts[6] : "N/A"
                ));
                break;
            }
        }

        // Load and append contact info
        List<String> contactLines = Files.readAllLines(Paths.get(CONTACTS_FILE));
        List<String> userContactUsernames = new ArrayList<>();
        for (String contactLine : contactLines) {
            String[] parts = contactLine.split("\\|");
            if (parts[0].equals(currentUser)) {
                userContactUsernames.add(parts[1]);
            }
        }

        profile.append("Contacts:\n");
        for (String contactUsername : userContactUsernames) {
            // Find contact details
            String phone = "Unknown";
            for (String userLine : userLines) {
                String[] userParts = userLine.split("\\|");
                if (userParts[0].equals(contactUsername)) {
                    if (userParts.length >= 7) {
                        phone = userParts[6];
                    }
                    break;
                }
            }
            profile.append(String.format("- %s (%s)\n", contactUsername, phone));
        }

        JOptionPane.showMessageDialog(null, profile.toString(), "Profile",
            JOptionPane.INFORMATION_MESSAGE);

    } catch (IOException e) {
        showError("Error loading profile");
    }
}


private static void reportUser() {
String reportedUser = JOptionPane.showInputDialog("Enter username to report(This will delete the user from your contact list):");
if (!userExists(reportedUser)) {
showError("User not found(report user whom exist)");
return;
}

String reason = JOptionPane.showInputDialog("Enter reason for report(Must Be Valid):");
if (reason == null || reason.trim().isEmpty()) {
showError("Reason is required");
return;
}

try {
String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
Files.write(
Paths.get(REPORTS_FILE),
(String.format("%s|%s|%s|%s\n",
currentUser,
reportedUser,
reason,
timestamp
)).getBytes(),
StandardOpenOption.APPEND
);
JOptionPane.showMessageDialog(null, "User reported successfully");
} catch (IOException e) {
showError("Error submitting report");
}
}

private static void adminLogin() {
String username = JOptionPane.showInputDialog("Enter admin username:");
String password = JOptionPane.showInputDialog("Enter admin password:");

if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
showAdminMenu();
} else {
showError("Invalid admin credentials (Do not take chances)");
}
}

private static void showAdminMenu() {
while (true) {
String[] options = {
"View All Users",
"View All Messages",
"View Reports",
"Delete User",
"Run Tests",
"Logout"
};

int choice = JOptionPane.showOptionDialog(
null,
"Admin Panel(GOAT Level Abilities here)",
"Admin Menu",
JOptionPane.DEFAULT_OPTION,
JOptionPane.INFORMATION_MESSAGE,
null,
options,
options[0]
);

switch (choice) {
case 0: viewAllUsers(); break;
case 1: viewAllMessages(); break;
case 2: viewReports(); break;
case 3: deleteUser(); break;
case 4: runTests(); break;
case 5: return;
default: return;
}
}
}

private static void viewAllUsers() {
try {
List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
StringBuilder display = new StringBuilder("All Users:\n\n");

for (String line : lines) {
String[] parts = line.split("\\|");
display.append(String.format("Username: %s, Name: %s, Age: %s\n",
parts[0], parts[2], parts[3]));
}

JOptionPane.showMessageDialog(null, display.toString());
} catch (IOException e) {
showError("Error loading users");
}
}

private static void viewAllMessages() {
try {
List<String> lines = Files.readAllLines(Paths.get(CHATS_FILE));
StringBuilder display = new StringBuilder("All Messages:\n\n");

for (String line : lines) {
String[] parts = line.split("\\|");
display.append(String.format("[%s] %s -> %s: %s\n",
parts[3], parts[0], parts[1], parts[2]));
}

JOptionPane.showMessageDialog(null, display.toString());
} catch (IOException e) {
showError("Error loading messages");
}
}

private static void viewReports() {
try {
List<String> lines = Files.readAllLines(Paths.get(REPORTS_FILE));
StringBuilder display = new StringBuilder("All Reports:\n\n");

for (String line : lines) {
String[] parts = line.split("\\|");
display.append(String.format("[%s] %s reported %s\nReason: %s\n\n",
parts[3], parts[0], parts[1], parts[2]));
}

JOptionPane.showMessageDialog(null, display.toString());
} catch (IOException e) {
showError("Error loading reports");
}
}

private static void deleteUser() {
String username = JOptionPane.showInputDialog("Enter username to delete:");
if (!userExists(username)) {
showError("User not found");
return;
}

try {
// Delete user from users file
List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
List<String> newLines = new ArrayList<>();

for (String line : lines) {
if (!line.split("\\|")[0].equals(username)) {
newLines.add(line);
}
}

Files.write(Paths.get(USERS_FILE), newLines);

// Clean up contacts
lines = Files.readAllLines(Paths.get(CONTACTS_FILE));
newLines = new ArrayList<>();

for (String line : lines) {
String[] parts = line.split("\\|");
if (!parts[0].equals(username) && !parts[1].equals(username)) {
newLines.add(line);
}
}

Files.write(Paths.get(CONTACTS_FILE), newLines);
JOptionPane.showMessageDialog(null, "User deleted successfully");
} catch (IOException e) {
showError("Error deleting user");
}
}

private static void runTests() {
boolean allPassed = true;
StringBuilder results = new StringBuilder("Test Results:\n\n");

// Test password validation
results.append("Password Validation Tests:\n");
allPassed &= testPasswordValidation(results);

// Test message handling
results.append("\nMessage Handling Tests:\n");
allPassed &= testMessageHandling(results);

// Test user validation
results.append("\nUser Validation Tests:\n");
allPassed &= testUserValidation(results);

results.append("\nOverall Result: ").append(allPassed ? "PASSED" : "FAILED");
JOptionPane.showMessageDialog(null, results.toString());
}

private static boolean testPasswordValidation(StringBuilder results) {
boolean passed = true;

// Test valid password
passed &= isValidPassword("Test123!");
results.append("Valid password test: ").append(passed ? "PASSED" : "FAILED").append("\n");

// Test invalid passwords
passed &= !isValidPassword("test123!"); // No uppercase
passed &= !isValidPassword("Test123"); // No special char
passed &= !isValidPassword("Test!"); // No number
passed &= !isValidPassword("Te1!"); // Too short

results.append("Invalid password tests: ").append(passed ? "PASSED" : "FAILED").append("\n");
return passed;
}

private static boolean testMessageHandling(StringBuilder results) {
boolean passed = true;

// Test message length validation
String longMessage = "a".repeat(MESSAGE_LIMIT + 1);
passed &= longMessage.length() > MESSAGE_LIMIT;
results.append("Message length validation: ").append(passed ? "PASSED" : "FAILED").append("\n");

return passed;
}

private static boolean testUserValidation(StringBuilder results) {
boolean passed = true;
// Test username validation
passed &= !isValidUsername("test"); // Should fail
passed &= isValidUsername("test_user"); // Should pass
results.append("Username validation: ").append(passed ? "PASSED" : "FAILED").append("\n");

return passed;
}

private static boolean userExists(String username) {
try {
return Files.lines(Paths.get(USERS_FILE))
.anyMatch(line -> line.split("\\|")[0].equals(username));
} catch (IOException e) {
return false;
}
}
private static boolean isValidUsername(String username) {
    return username != null && username.contains("_");
}

private static String generateMessageHash(String message) {
String[] words = message.split("\\s+");
return hashPassword(words[0] + words[words.length-1]).substring(0, 8);
}

private static void logout() {
currentUser = null;
JOptionPane.showMessageDialog(null, "Logged out successfully");
}

private static void showError(String message) {
JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
}

    private static boolean isValidSAPhoneNumber(String phoneNumber) {
       return phoneNumber != null && phoneNumber.matches("\\+27\\d{9}");  // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}