import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

// Sköter grafiska gränssnittet och kopplar ihop det med multicast nätverket
public class ChatClient extends JFrame {
    private final String userName;
    private final JTextArea chatArea = new JTextArea(); // Visar chattmeddelanden
    private final JTextField inputField = new JTextField(); // Där man skriver meddelanden
    private final DefaultListModel<String> userListModel = new DefaultListModel<>(); // Lista över aktiva användare
    private final JList<String> userList = new JList<>(userListModel);
    private final MulticastService multicastService;

    public ChatClient(String userName) throws IOException {
        this.userName = userName;

        // Startar Multicast tjänsten och lyssnar på meddelanden
        this.multicastService = new MulticastService(userName, this::handleMessage);

        setupGUI(); // Bygger GUI:t
        multicastService.sendJoinMessage(); // Meddelar andra att man har gått med

        // När man stänger fönstret skickas ett "LEAVE" meddelande
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                multicastService.sendLeaveMessage();
                multicastService.shutdown();
                dispose();
            }
        });
    }

    // Skapar GUI-komponenterna
    private void setupGUI() {
        setTitle("Gruppchatt - " + userName);
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea.setEditable(false); // Man kan inte skriva i chatthistoriken
        JScrollPane chatScroll = new JScrollPane(chatArea);
        JScrollPane userScroll = new JScrollPane(userList);
        JButton disconnectButton = new JButton("Koppla ner");

        // När man trycker på Enter skickas meddelandet
        inputField.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                multicastService.sendMessage("MSG|" + userName + "|" + text);
                inputField.setText("");
            }
        });

        // Koppla ner-knappen stänger anslutningen
        disconnectButton.addActionListener(e -> {
            multicastService.sendLeaveMessage();
            multicastService.shutdown();
            dispose();
            System.exit(0);
        });

        // Layout för GUI:t
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(disconnectButton, BorderLayout.EAST);

        add(chatScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(userScroll, BorderLayout.EAST);

        setVisible(true);
    }

    // Tar emot inkommande meddelanden och skickar dem till MessageHandler
    private void handleMessage(String msg) {
        MessageHandler.process(msg, userName, chatArea, userListModel);
    }
}
