import javax.swing.*;        // Componentes gráficos (JFrame, JButton, etc.)
import java.awt.*;           // Layouts y componentes básicos de AWT
import java.awt.event.ActionEvent; // Eventos de botones
import java.sql.Connection;  // Conexión a base de datos
import java.sql.PreparedStatement; // Consultas SQL preparadas
import java.sql.ResultSet;   // Resultado de consultas SQL

public class Login extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JButton btnIngresar;
    private JButton btnSalir;

    public Login() { // Constructor de la clase Login
        setTitle("Login - Gestión Los Troncos"); // Título de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana

        // Fuente y dimensiones
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font titleFont = new Font("Segoe UI", Font.BOLD, 20);
        Dimension fieldDim = new Dimension(260, 34);
        Dimension btnDim = new Dimension(140, 36);

        // Layout flexible con GridBagLayout para mejor alineación
        JPanel content = new JPanel(new GridBagLayout());  // Panel con layout flexible
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18)); // Margen interno
        GridBagConstraints gbc = new GridBagConstraints(); // Objeto para posicionar componentes
        gbc.insets = new Insets(8, 8, 8, 8);  // Espaciado entre componentes
        gbc.anchor = GridBagConstraints.WEST; // Alinear a la izquierda

        // Título centrado
        gbc.gridx = 0;                    // Columna 0
        gbc.gridy = 0;                    // Fila 0
        gbc.gridwidth = 2;                // Ocupa 2 columnas
        gbc.anchor = GridBagConstraints.CENTER; // Centrado

        JLabel lblTitulo = new JLabel("🍽️ LOS TRONCOS"); // Crear label
        lblTitulo.setFont(titleFont);     // Aplicar fuente grande
        content.add(lblTitulo, gbc);      // Agregar al panel

        // Usuariog
        gbc.gridy = 1;                    // Fila 1
        gbc.gridwidth = 1;                // Solo 1 columna
        gbc.anchor = GridBagConstraints.WEST; // Alinear izquierda
        gbc.gridx = 0;                    // Columna 0

        JLabel lblUser = new JLabel("Usuario:"); // Crear label
        lblUser.setFont(labelFont);       // Fuente mediana
        content.add(lblUser, gbc);        // Agregar al panel

        // Campo usuario
        gbc.gridx = 1;                    // Columna 1 (al lado del label)
        gbc.fill = GridBagConstraints.HORIZONTAL; // Se expande horizontalmente
        gbc.weightx = 1.0;                // Toma espacio disponible

        txtUsuario = new JTextField();    // Crear campo de texto
        txtUsuario.setFont(fieldFont);    // Fuente
        txtUsuario.setPreferredSize(fieldDim); // Tamaño 260x34
        content.add(txtUsuario, gbc);     // Agregar al panel

        // Contraseña
        gbc.gridx = 0;                    // Columna 0
        gbc.gridy = 2;                    // Fila 2
        gbc.fill = GridBagConstraints.NONE; // No expandir
        gbc.weightx = 0;                  // Sin peso extra

        JLabel lblPass = new JLabel("Contraseña:"); // Crear label
        lblPass.setFont(labelFont); // Fuente mediana
        content.add(lblPass, gbc); // Agregar al panel

        // Campo contraseña
        gbc.gridx = 1;                    // Columna 1
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtClave = new JPasswordField();  // Campo de contraseña (oculta texto)
        txtClave.setFont(fieldFont); // Fuente mediana
        txtClave.setPreferredSize(fieldDim); // Tamaño 260x34
        content.add(txtClave, gbc); // Agregar al panel

        // Botones ingresar y salir
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        // Botón Ingresar
        btnIngresar = new JButton("🔐 Ingresar"); // Crear botón Ingresar
        btnIngresar.setPreferredSize(new Dimension(160, 36)); // Tamaño
        btnIngresar.setFont(fieldFont); // Fuente mediana
        btnIngresar.setBackground(new Color(76, 175, 80)); // Verde
        btnIngresar.setForeground(Color.WHITE);  // Texto blanco
        btnIngresar.setFocusPainted(false);      // Sin borde al hacer foco

        // Botón Salir
        btnSalir = new JButton("Salir"); // Crear botón Salir
        btnSalir.setPreferredSize(btnDim);// Tamaño 140x36
        btnSalir.setFont(fieldFont); // Fuente mediana

        btnPanel.add(btnIngresar); // Agregar botón Ingresar al panel
        btnPanel.add(btnSalir);    // Agregar botón Salir al panel

        // Añadir panel de botones
        gbc.gridx = 0;
        gbc.gridy = 3; // Fila 3
        gbc.gridwidth = 2; // Ocupa 2 columnas
        gbc.fill = GridBagConstraints.HORIZONTAL; // Expandir horizontalmente
        gbc.weightx = 0; // Sin peso extra
        content.add(btnPanel, gbc); // Agregar panel de botones

        // Nota informativa con html para formato
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 8, 8, 8);
        JLabel lblInfo = new JLabel("<html><center><i>El sistema detectará automáticamente tu rol</i></center></html>"); // Crear label informativo con HTML
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Fuente pequeña
        lblInfo.setForeground(Color.GRAY); // Color gris
        content.add(lblInfo, gbc); // Agregar al panel

        setContentPane(content);  // Establecer el panel como contenido de la ventana
        pack();                   // Ajustar tamaño automáticamente
        setResizable(false);      // No permitir cambiar tamaño
        setLocationRelativeTo(null); // Centrar en pantalla

        // Acciones para botones
        btnIngresar.addActionListener((ActionEvent e) -> doLogin()); // Al hacer clik → ejecutar doLogin()
        btnSalir.addActionListener(e -> System.exit(0));  // Al hacer clik para cerrar aplicación

        getRootPane().setDefaultButton(btnIngresar); // Enter = click en Ingresar
    }

    // metodo para manejar el login
    private void doLogin() {
        String user = txtUsuario.getText().trim();  // Obtener texto del campo usuario (sin espacios)
        String pass = new String(txtClave.getPassword()).trim(); // Obtener contraseña (sin espacios)

        if (user.isEmpty() || pass.isEmpty()) { // Validar campos vacíos
            JOptionPane.showMessageDialog(this,
                    "Complete todos los campos",
                    "Campos vacíos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection con = Conexion.GetConnection();
            // Consulta que busca por nombre y contraseña, obtiene el nivel (rol)
            String sql = "SELECT * FROM usuario WHERE nombre = ? AND contraseña = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, user);  // Reemplaza primer ? con el usuario
            ps.setString(2, pass);  // Reemplaza segundo ? con la contraseña
            ResultSet rs = ps.executeQuery(); // Ejecutar consulta SQL

            if (rs.next()) {  // Sí hay al menos un resultado (usuario encontrado)
                // Obtener el nivel (rol) de la base de datos
                String nivel = rs.getString("nivel"); // Leer columna "nivel" de la BD
                String rol;

                // Mapear nivel a rol
                if (nivel.equalsIgnoreCase("ADMIN") || nivel.equals("1")) {
                    rol = "ADMIN";
                } else if (nivel.equalsIgnoreCase("COCINA") || nivel.equals("3")) {
                    rol = "COCINA";
                } else {
                    rol = "MOZO"; // Por defecto
                }

                // Crear objeto Usuario
                Usuario usuarioObj = new Usuario(
                    rs.getInt("id_usuario"),        // ID del usuario
                    rs.getString("nombre"),         // Nombre
                    rs.getString("nombre"),         // Usuario (mismo que nombre)
                    rs.getString("contraseña"),     // Contraseña
                    rol // Rol mapeado (ADMIN/MOZO/COCINA)
                );

                SesionUsuario.getInstancia().iniciarSesion(usuarioObj); // guardar en sesión el usuario logueado
                // Mostrar mensaje de bienvenida
                JOptionPane.showMessageDialog(this,
                        "¡Bienvenido " + usuarioObj.getNombre() + "!\nRol: " + rol,
                        "Login Exitoso",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose();  // Cerrar ventana de Login
                abrirVentanaSegunRol(usuarioObj); // Abrir MenuPrincipal o VistaCocina

            } else {  // Usuario/contraseña incorrectos
                JOptionPane.showMessageDialog(this,
                        "Usuario o contraseña incorrectos",
                        "Error de Login",
                        JOptionPane.ERROR_MESSAGE);
                txtClave.setText("");       // Limpiar campo contraseña
                txtUsuario.requestFocus();  // Poner cursor en campo usuario
            }

           rs.close();   // Cerrar ResultSet
            ps.close();   // Cerrar PreparedStatement
            con.close();  // Cerrar Conexión

       } catch (Exception e) {
            e.printStackTrace();  // Imprimir error en consola para depuración
            JOptionPane.showMessageDialog(this,
                    "Error BD: " + e.getMessage() +
                    "\n\nVerifica que exista la tabla 'usuario' con columnas:" +
                    "\n- id_usuario" +
                    "\n- nombre (usuario)" +
                    "\n- contraseña" +
                    "\n- nivel (rol)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

   private void abrirVentanaSegunRol(Usuario usuario) {
        if (usuario.esCocina()) {  // Si es rol COCINA
            VistaCocina cocina = new VistaCocina();
            cocina.setVisible(true);
        } else {  // Si es ADMIN o MOZO
            MenuPrincipal menu = new MenuPrincipal();
            menu.setVisible(true);
        }
    }

    // Metodo principal para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // Ejecutar en el hilo de eventos de Swing
            new Login().setVisible(true); // Crear y mostrar ventana de Login(ejecuta el constructor)
        });
    }
}