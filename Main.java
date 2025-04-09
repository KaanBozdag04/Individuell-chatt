// Startpunkt för programmet – frågar användaren om namn och startar chatten
public class Main {
    public static void main(String[] args) {
        String name = javax.swing.JOptionPane.showInputDialog("Ange ditt användarnamn:");
        if (name != null && !name.trim().isEmpty()) {
            try {
                new ChatClient(name.trim());
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null, "Kunde inte starta chatten.");
            }
        }
    }
}
