import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuPrincipal extends JFrame {

    private final JButton[] mesas = new JButton[30];
    private JLabel lblLibres;
    private JLabel lblOcupadas;
    private int libres = 30;
    private int ocupadas = 0;

    public MenuPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema de Restaurante");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        // Panel superior
        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(50, 100, 200));
        JLabel titulo = new JLabel("Bienvenido al Sistema de Restaurante");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        panelSuperior.add(titulo);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel central para mesas
        JPanel panelMesas = new JPanel();
        panelMesas.setLayout(new GridLayout(5, 6, 10, 10));

        for(int i = 0; i < 30; i++) {
            mesas[i] = new JButton("Mesa " + (i + 1));
            mesas[i].setBackground(Color.GREEN);
            mesas[i].setOpaque(true);
            mesas[i].setBorderPainted(false);
            int index = i;
            mesas[i].addActionListener((ActionEvent e) -> toggleMesa(index));
            panelMesas.add(mesas[i]);
        }
        add(panelMesas, BorderLayout.CENTER);

        // Panel lateral con estadísticas
        JPanel panelLateral = new JPanel();
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        panelLateral.setPreferredSize(new Dimension(200, 0));

        lblLibres = new JLabel("Mesas libres: 30");
        lblLibres.setFont(new Font("Arial", Font.BOLD, 16));
        lblOcupadas = new JLabel("Mesas ocupadas: 0");
        lblOcupadas.setFont(new Font("Arial", Font.BOLD, 16));

        panelLateral.add(Box.createRigidArea(new Dimension(0,20)));
        panelLateral.add(lblLibres);
        panelLateral.add(Box.createRigidArea(new Dimension(0,10)));
        panelLateral.add(lblOcupadas);
        panelLateral.add(Box.createVerticalGlue());

        add(panelLateral, BorderLayout.EAST);

        // Barra de menú
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> System.exit(0));
        menuArchivo.add(salir);

        JMenu menuMesas = new JMenu("Mesas");
        JMenuItem verMesas = new JMenuItem("Ver estado mesas");
        verMesas.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Mesas libres: " + libres + "\nMesas ocupadas: " + ocupadas));
        menuMesas.add(verMesas);

        JMenu menuPedidos = new JMenu("Pedidos");
        JMenuItem verPedidos = new JMenuItem("Pedidos activos");
        verPedidos.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Esta función se puede ampliar para gestionar pedidos"));
        menuPedidos.add(verPedidos);

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem acercaDe = new JMenuItem("Acerca de");
        acercaDe.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Restaurante v1.0\nCreado por Stefano"));
        menuAyuda.add(acercaDe);

        menuBar.add(menuArchivo);
        menuBar.add(menuMesas);
        menuBar.add(menuPedidos);
        menuBar.add(menuAyuda);
        setJMenuBar(menuBar);
    }

    // ... código anterior de MenuPrincipal ...

    // En MenuPrincipal.java

    private void toggleMesa(int index) {
        // Si la mesa está VERDE (libre)
        if(mesas[index].getBackground() == Color.GREEN) {

            // 1. Cambiar estado a OCUPADA (ROJO)
            mesas[index].setBackground(Color.RED);
            libres--;
            ocupadas++;

            // 2. OBTENER EL NÚMERO DE MESA y ABRIR LA VENTANA DE PEDIDO
            int numeroMesa = index + 1;

            // Crea una nueva instancia de la ventana de pedido, haciéndola modal.
            VentanaPedido ventanaPedido = new VentanaPedido(this, numeroMesa);
            ventanaPedido.setVisible(true);

            // Opcional: Puedes quitar el mensaje si la ventana de pedido es suficiente
            // JOptionPane.showMessageDialog(this, "Mesa " + numeroMesa + " ahora está ocupada. Abriendo pedido.");

        } else {
            // Si la mesa está ROJA (ocupada), la liberamos.

            // Lógica para liberar la mesa
            mesas[index].setBackground(Color.GREEN);
            libres++;
            ocupadas--;
            JOptionPane.showMessageDialog(this, "Mesa " + (index + 1) + " ahora está libre");
        }

        // 3. Actualizar estadísticas en el panel lateral
        lblLibres.setText("Mesas libres: " + libres);
        lblOcupadas.setText("Mesas ocupadas: " + ocupadas);
    }
}
