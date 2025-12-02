import javax.swing.SwingUtilities; // Para manejar la interfaz gráfica de usuario
import javax.swing.JOptionPane; // Para mostrar cuadros de diálogo

public class Main {
    public static void main(String[] args) {
    
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Login login = new Login();
                login.setVisible(true);
            }
        });
    }
}

