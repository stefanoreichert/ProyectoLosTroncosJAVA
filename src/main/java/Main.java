import javax.swing.JOptionPane;

/**
 *
 * @author stefa
 */

public class Main {

    public static void main(String[] args) {
        if( Conexion.GetConnection()!= null)

            JOptionPane.showMessageDialog(null,  "conectado!!!");

        java.awt.EventQueue.invokeLater(() -> {
            Login login = new Login();
            login.setVisible(true);
        });
    }
}

