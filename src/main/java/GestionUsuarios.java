import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestionUsuarios extends JDialog {
    private DefaultTableModel modeloTabla;  // Modelo que contiene los datos de la tabla
    private JTable tabla; // Componente visual que muestra los datos
    private JButton btnNuevo, btnEditar, btnEliminar, btnCerrar;  // Botones de acción


    // Constructor
    public GestionUsuarios(Frame parent) {
        super(parent, "Gestión de Usuarios", true);  // Crea JDialog modal (bloquea ventana padre)
        setSize(800, 500); // Dimensiones de la ventana
        setLocationRelativeTo(parent); // Centra respecto a la ventana padre
        setLayout(new BorderLayout(10, 10)); // Layout con márgenes de 10px

        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(33, 150, 243));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título con ícono
        JLabel lblTitulo = new JLabel("👥 Gestión de Usuarios");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        add(panelSuperior, BorderLayout.NORTH);

        // Tabla para mostrar usuarios
        String[] columnas = {"ID", "Usuario", "Nombre", "Rol"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Crear la tabla con el modelo
        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Arial", Font.PLAIN, 14));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(tabla); // Barra de desplazamiento para la tabla
        add(scroll, BorderLayout.CENTER); // Agrega la tabla al centro

        // Panel botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Layout centrado con espacio entre botones
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen interno

        // Botones con estilos personalizados
        btnNuevo = new JButton("➕ Nuevo Usuario");
        btnNuevo.setFont(new Font("Arial", Font.BOLD, 14));
        btnNuevo.setBackground(new Color(76, 175, 80));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> nuevoUsuario());

        // Botón Editar
        btnEditar = new JButton("✏️ Editar");
        btnEditar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEditar.setBackground(new Color(255, 152, 0));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(e -> editarUsuario());

        // Botón Eliminar
        btnEliminar = new JButton("🗑️ Eliminar");
        btnEliminar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEliminar.setBackground(new Color(244, 67, 54));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarUsuario());

        // Botón Cerrar
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCerrar.addActionListener(e -> dispose());

        // Agregar botones al panel
        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.SOUTH); // Agrega el panel de botones al sur

        cargarUsuarios(); // Carga los usuarios al iniciar
    }

    // Método para cargar usuarios desde la base de datos
    private void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        // Conexión a la base de datos y consulta
        try {
            Connection con = Conexion.GetConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id_usuario, nombre, nivel FROM usuario ORDER BY id_usuario");

            // Agrega cada usuario al modelo de la tabla
            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("nombre"), // nombre sirve como usuario
                    rs.getString("nivel")
                });
            }

            rs.close(); // Cierra el ResultSet
            st.close(); // Cierra el Statement
            con.close(); // Cierra la conexión

        // Manejo de errores
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar usuarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Metodo para crear un nuevo usuario
    private void nuevoUsuario() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField txtNombre = new JTextField();
        JPasswordField txtContrasena = new JPasswordField();
        JComboBox<String> cmbNivel = new JComboBox<>(new String[]{"MOZO", "COCINA", "ADMIN"});

        // Agregar componentes al panel
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Contraseña:"));
        panel.add(txtContrasena);
        panel.add(new JLabel("Nivel:"));
        panel.add(cmbNivel);

        // Mostrar diálogo de entrada
        int result = JOptionPane.showConfirmDialog(this, panel, "Nuevo Usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Si el usuario presiona OK el formulario se procesa
        if (result == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String contrasena = new String(txtContrasena.getPassword()).trim();
            String nivel = (String) cmbNivel.getSelectedItem();

            // Validar campos obligatorios
            if (nombre.isEmpty() || contrasena.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Todos los campos son obligatorios",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insertar nuevo usuario en la base de datos
            try {
                Connection con = Conexion.GetConnection();
                String sql = "INSERT INTO usuario (nombre, contraseña, nivel) VALUES (?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setString(2, contrasena);
                ps.setString(3, nivel);
                ps.executeUpdate(); // Ejecuta la inserción
                ps.close(); // Cierra el PreparedStatement
                con.close(); // Cierra la conexión

                JOptionPane.showMessageDialog(this,
                        "Usuario creado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();

                // Manejo de errores
                } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error al crear usuario: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // Metodo para editar un usuario existente
    private void editarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un usuario para editar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Obtener datos actuales del usuario seleccionado
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombreActual = (String) modeloTabla.getValueAt(fila, 2);
        String nivelActual = (String) modeloTabla.getValueAt(fila, 3);

        // Crear formulario de edición
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField txtNombre = new JTextField(nombreActual);
        JPasswordField txtContrasena = new JPasswordField();
        JComboBox<String> cmbNivel = new JComboBox<>(new String[]{"MOZO", "COCINA", "ADMIN"});
        cmbNivel.setSelectedItem(nivelActual);

        // Agregar componentes al panel
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Nueva Contraseña (dejar vacío para no cambiar):"));
        panel.add(txtContrasena);
        panel.add(new JLabel("Nivel:"));
        panel.add(cmbNivel);

        // Mostrar diálogo de edición
        int result = JOptionPane.showConfirmDialog(this, panel, "Editar Usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Si el usuario presiona OK el formulario se procesa
        if (result == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            String contrasena = new String(txtContrasena.getPassword()).trim();
            String nivel = (String) cmbNivel.getSelectedItem();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre es obligatorio",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Actualizar usuario en la base de datos
            try {
                Connection con = Conexion.GetConnection();
                String sql;
                PreparedStatement ps;

                // Actualizar usuario con o sin cambio de contraseña
                if (contrasena.isEmpty()) {
                    sql = "UPDATE usuario SET nombre = ?, nivel = ? WHERE id_usuario = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, nombre);
                    ps.setString(2, nivel);
                    ps.setInt(3, id);

                } else {
                    sql = "UPDATE usuario SET nombre = ?, contraseña = ?, nivel = ? WHERE id_usuario = ?";
                    ps = con.prepareStatement(sql);
                    ps.setString(1, nombre);
                    ps.setString(2, contrasena);
                    ps.setString(3, nivel);
                    ps.setInt(4, id);
                }

                ps.executeUpdate();
                ps.close();
                con.close();

                JOptionPane.showMessageDialog(this,
                        "Usuario actualizado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error al actualizar usuario: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // Metodo para eliminar un usuario
    private void eliminarUsuario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un usuario para eliminar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener datos del usuario seleccionado
        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = (String) modeloTabla.getValueAt(fila, 2);

        // Confirmar eliminación
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al usuario: " + nombre + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        // Si el usuario confirma la eliminación
        if (confirm == JOptionPane.YES_OPTION) {
            // Eliminar usuario de la base de datos
            try {
                Connection con = Conexion.GetConnection();
                String sql = "DELETE FROM usuario WHERE id_usuario = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
                con.close();

                JOptionPane.showMessageDialog(this,
                        "Usuario eliminado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                cargarUsuarios();

            // Manejo  de errores
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar usuario: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

