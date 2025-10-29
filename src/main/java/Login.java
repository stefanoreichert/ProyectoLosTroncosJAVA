// java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Login extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JButton btnIngresar;
    private JButton btnSalir;

    public Login() {
        setTitle("Login - Gestion Los Troncos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Fuente y dimensiones más grandes para mejor lectura
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);
        Dimension fieldDim = new Dimension(260, 34);
        Dimension btnDim = new Dimension(140, 36);

        // Layout flexible
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Usuario
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(labelFont);
        content.add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtUsuario = new JTextField();
        txtUsuario.setFont(fieldFont);
        txtUsuario.setPreferredSize(fieldDim);
        content.add(txtUsuario, gbc);

        // Clave
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel lblPass = new JLabel("Clave:");
        lblPass.setFont(labelFont);
        content.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtClave = new JPasswordField();
        txtClave.setFont(fieldFont);
        txtClave.setPreferredSize(fieldDim);
        content.add(txtClave, gbc);

        // Botones (alineados a la derecha)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setPreferredSize(btnDim);
        btnIngresar.setFont(fieldFont);
        btnSalir = new JButton("Salir");
        btnSalir.setPreferredSize(btnDim);
        btnSalir.setFont(fieldFont);
        btnPanel.add(btnIngresar);
        btnPanel.add(btnSalir);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        content.add(btnPanel, gbc);

        setContentPane(content);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        // Acciones
        btnIngresar.addActionListener((ActionEvent e) -> doLogin());
        btnSalir.addActionListener(e -> System.exit(0));

        // Enter en los campos activa el login
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void doLogin() {
        String user = txtUsuario.getText().trim();
        String pass = new String(txtClave.getPassword());
        if ("".equals(user) && "".equals(pass)) {
            MenuPrincipal menu = new MenuPrincipal();
            menu.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Para pruebas rápidos
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
}