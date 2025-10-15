package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; // ¡Importa la clase de error SQL!
import javax.swing.JOptionPane;

public class Conexion {

    public static Connection GetConnection() {
        Connection conexion = null;
        try {
            // NOTA: com.mysql.jdbc.Driver es antiguo. Es mejor usar com.mysql.cj.jdbc.Driver
            Class.forName("com.mysql.jdbc.Driver");

            String servidor = "jdbc:mysql://localhost/los_troncos";
            String usuarioDB = "root";
            String passwordDB = ""; // Usando la contraseña proporcionada

            conexion = DriverManager.getConnection(servidor, usuarioDB, passwordDB);

            // Si la conexión es exitosa, se devuelve.

        } catch (ClassNotFoundException ex) {
            // Este error ocurre si no tienes el conector MySQL en tu proyecto
            JOptionPane.showMessageDialog(null, "Error: No se encontró el driver JDBC de MySQL.",
                    "Error de Driver", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            conexion = null;

        } catch (SQLException ex) { // <-- ¡ESTE ES EL BLOQUE QUE FALTABA Y CAUSA TU PROBLEMA!
            // Este error ocurre si las credenciales fallan, la BD no existe, o MySQL está apagado
            JOptionPane.showMessageDialog(null,
                    "Error al conectar con la base de datos 'los_troncos'.\n" +
                            "Verifica tu servidor MySQL (XAMPP/WAMP/etc.) y credenciales.\nDetalle: " + ex.getMessage(),
                    "Error de Conexión SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            conexion = null;
        }

        // El bloque finally es opcional aquí, solo devolvemos la conexión
        return conexion;
    }
}