import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Conexion {

    public static Connection GetConnection(){
        Connection conexion=null;
        //Cargar el driver de MySQL
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

            //Establecer la conexión
            String servidor = "jdbc:mysql://localhost/los_troncos";
            String usuarioDB="root";
            String passwordDB="";

            //Crear la conexión a la base de datos
            conexion = DriverManager.getConnection(servidor,usuarioDB,passwordDB);

        //Manejo  de errores
        } catch(ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error: No se encontró el driver de MySQL. Asegúrese de que el JAR del conector está en el proyecto.",
                    "Error de Configuración", JOptionPane.ERROR_MESSAGE);
            conexion = null;

        //Manejo de errores
        } catch(SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error de conexión a la Base de Datos: " + ex.getMessage(),
                    "Error BD", JOptionPane.ERROR_MESSAGE);
            conexion = null;
        }

        return conexion;
    }
}

