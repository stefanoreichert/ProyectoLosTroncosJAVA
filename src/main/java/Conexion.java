import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {

    public static Connection GetConnection(){
        Connection conexion=null;
        try{
            // CORRECCIÓN CLAVE: Usar el driver moderno
            Class.forName("com.mysql.cj.jdbc.Driver");

            String servidor = "jdbc:mysql://localhost/los_troncos";
            String usuarioDB="root";
            String passwordDB="";

            conexion = DriverManager.getConnection(servidor,usuarioDB,passwordDB);

        } catch(ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error: No se encontró el driver de MySQL. Asegúrese de que el JAR del conector está en el proyecto.",
                    "Error de Configuración", JOptionPane.ERROR_MESSAGE);
            conexion = null;
        } catch(SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error de conexión a la Base de Datos: " + ex.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
            conexion = null;
        }

        return conexion;
    }
}

