import java.io.IOException;
import java.net.*;
import java.util.function.Consumer;

// Sköter all kommunikation via UDP och Multicast
public class MulticastService {
    private static final String MULTICAST_ADDRESS = "230.0.0.0"; // Multicast-adress
    private static final int PORT = 4446; // Port som alla chattinstanser använder

    private final String userName;
    private final MulticastSocket socket;
    private final InetAddress group;
    private volatile boolean running = true;

    // Konstruktor: ansluter till multicast-grupp och startar lyssnar-tråd
    public MulticastService(String userName, Consumer<String> messageConsumer) throws IOException {
        this.userName = userName;
        this.socket = new MulticastSocket(PORT);
        this.group = InetAddress.getByName(MULTICAST_ADDRESS);
        socket.joinGroup(group);

        // Lyssnar på inkommande meddelanden i en bakgrundstråd
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    messageConsumer.accept(received); // Skickar meddelandet till GUI
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        }).start();
    }

    // Skickar ett vanligt chattmeddelande
    public void sendMessage(String message) {
        try {
            byte[] bytes = message.getBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Skickar meddelande att användaren gått med
    public void sendJoinMessage() {
        sendMessage("JOIN|" + userName);
    }

    // Skickar meddelande att användaren lämnar chatten
    public void sendLeaveMessage() {
        sendMessage("LEAVE|" + userName);
    }

    // Ny metod: Skickar begäran om lista över aktiva användare
    public void requestUserList() {
        sendMessage("REQUEST_USERS|" + userName);
    }

    // Stänger ner socket och lämnar gruppen
    public void shutdown() {
        running = false;
        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
