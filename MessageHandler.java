import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

// Hanterar inkommande meddelanden: JOIN, LEAVE och MSG
public class MessageHandler {
    private static final Set<String> activeUsers = new HashSet<>();

    public static void process(String message, String localUser, JTextArea chatArea, DefaultListModel<String> userListModel) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = message.split("\\|", 3);
            if (parts.length < 2) return;

            String type = parts[0];
            String sender = parts[1];

            switch (type) {
                case "JOIN":
                    // Ny användare gick med
                    if (!sender.equals(localUser)) {
                        chatArea.append(sender + " gick med i chatten.\n");
                    }
                    if (!activeUsers.contains(sender)) {
                        activeUsers.add(sender);
                        userListModel.addElement(sender);
                    }
                    break;

                case "LEAVE":
                    // Användare lämnar chatten
                    chatArea.append(sender + " har lämnat chatten.\n");
                    activeUsers.remove(sender);
                    userListModel.removeElement(sender);
                    break;

                case "MSG":
                    // Vanligt meddelande visas i chattfönstret
                    if (parts.length == 3) {
                        String text = parts[2];
                        chatArea.append(sender + ": " + text + "\n");
                    }
                    break;
            }
        });
    }
}
