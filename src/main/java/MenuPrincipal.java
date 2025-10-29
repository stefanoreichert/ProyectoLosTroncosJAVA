import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MenuPrincipal extends JFrame {
    private final JButton[] mesas = new JButton[40];
    private JLabel lblLibres;
    private JLabel lblOcupadas;
    private int libres = 40;
    private int ocupadas = 0;

    public MenuPrincipal() {
        setTitle("Sistema Restaurante - Mesas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Crear la barra de menú
        crearBarraMenu();

        // Panel superior con título
        JPanel top = new JPanel();
        JLabel titulo = new JLabel("Sistema Restaurante - Mesas");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        top.add(titulo);
        add(top, BorderLayout.NORTH);

        // inicialización de mesas
        int libresInicial = 0;
        int ocupadasInicial = 0;

        // Panel de mesas 5x8
        JPanel center = new JPanel(new GridLayout(5, 8, 6, 6));

        // Crear las 40 mesas
        for (int i = 0; i < 40; i++) {
            final int numeroMesa = i + 1;

            mesas[i] = new JButton("Mesa " + (i + 1));

            // Inicializar el color según el estado en ModeloPedidos
            if (ModeloPedidos.tienePedido(numeroMesa)) {
                mesas[i].setBackground(Color.RED);
                ocupadasInicial++;
            } else {
                mesas[i].setBackground(Color.GREEN);
                libresInicial++;
            }

            // Configuración visual del botón de la mesa
            mesas[i].setOpaque(true);
            mesas[i].setBorderPainted(true);

            // Click simple: abrir mesa
            mesas[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    abrirVentanaMesa(numeroMesa);
                }
            });

            // Doble click: ver pedido actual
            mesas[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        verPedidoMesa(numeroMesa);
                    }
                }
            });

            center.add(mesas[i]);
        }

        libres = libresInicial;
        ocupadas = ocupadasInicial;

        add(center, BorderLayout.CENTER);

        // Panel derecho con estadísticas
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        lblLibres = new JLabel("Mesas libres: " + libres);
        lblOcupadas = new JLabel("Mesas ocupadas: " + ocupadas);

        // Etiqueta de ayuda para doble clik
        JLabel lblAyuda = new JLabel("<html><br>Doble click:<br>Ver pedido</html>");
        lblAyuda.setFont(new Font("Arial", Font.ITALIC, 11));
        lblAyuda.setForeground(Color.GRAY);

        right.add(Box.createVerticalStrut(10));
        right.add(lblLibres);
        right.add(Box.createVerticalStrut(10));
        right.add(lblOcupadas);
        right.add(Box.createVerticalStrut(20));
        right.add(lblAyuda);
        right.add(Box.createVerticalGlue());
        add(right, BorderLayout.EAST);
    }

    // Crea la barra de menú con todas las opciones
    private void crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menuArchivo.add(itemSalir);

        // Menú Reportes
        JMenu menuReportes = new JMenu("Reportes");

        JMenuItem itemResumenDia = new JMenuItem("📊 Resumen del Día");
        itemResumenDia.setFont(new Font("Arial", Font.PLAIN, 13));
        itemResumenDia.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarResumenDia();
            }
        });

        JMenuItem itemResumenMes = new JMenuItem("📅 Resumen del Mes");
        itemResumenMes.setFont(new Font("Arial", Font.PLAIN, 13));
        itemResumenMes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarResumenMensual();
            }
        });

        menuReportes.add(itemResumenDia);
        menuReportes.addSeparator(); // Línea separadora
        menuReportes.add(itemResumenMes);

        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de");
        itemAcercaDe.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextArea textArea = new JTextArea(
                        "═══════════════════════════════════════════════\n" +
                                "   SISTEMA DE GESTIÓN DE RESTAURANTE\n" +
                                "              LOS TRONCOS\n" +
                                "═══════════════════════════════════════════════\n\n" +
                                "Versión: 1.0\n" +
                                "Desarrollado para la gestión de mesas y pedidos\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "CÓMO USAR EL SISTEMA:\n" +
                                "───────────────────────────────────────────────\n\n" +
                                "GESTIÓN DE MESAS:\n" +
                                "  • Click en mesa: Abrir ventana de pedido\n" +
                                "  • Doble click: Ver resumen rápido del pedido\n" +
                                "  • Verde: Mesa disponible\n" +
                                "  • Rojo: Mesa con pedido activo\n\n" +
                                "TOMAR PEDIDOS:\n" +
                                "  • Filtrar productos por tipo y categoría\n" +
                                "  • Buscar productos por nombre\n" +
                                "  • Usar ↑ + Enter para agregar rápido\n" +
                                "  • Doble click en producto para agregar\n" +
                                "  • Editar cantidad y precio en la tabla\n\n" +
                                "GESTIÓN DE PRODUCTOS:\n" +
                                "  • Agregar nuevos productos\n" +
                                "  • Editar productos existentes\n" +
                                "  • Eliminar productos (con confirmación)\n\n" +
                                "REPORTES:\n" +
                                "  • Resumen del Día: Total diario + reinicio\n" +
                                "  • Resumen del Mes: Total mensual + reinicio\n" +
                                "  • Ambos permiten imprimir\n\n" +
                                "IMPRESIÓN:\n" +
                                "  • Imprimir ticket de mesa\n" +
                                "  • Descuenta stock automáticamente\n" +
                                "  • Imprimir resúmenes diarios/mensuales\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "⚠️  IMPORTANTE:\n" +
                                "   - Los resúmenes borran todos los pedidos\n" +
                                "   - Siempre imprimir antes de cerrar resumen\n" +
                                "   - El stock se descuenta al imprimir ticket\n" +
                                "───────────────────────────────────────────────"
                );
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
                textArea.setEditable(false);
                textArea.setBackground(new Color(245, 245, 245));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 550));

                JOptionPane.showMessageDialog(MenuPrincipal.this,
                        scrollPane,
                        "Acerca de - Los Troncos",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menuAyuda.add(itemAcercaDe);

// Agregar menús a la barra
        menuBar.add(menuArchivo);
        menuBar.add(menuReportes);
        menuBar.add(menuAyuda);

        setJMenuBar(menuBar);
    }

    // Abre la ventana de pedido para una mesa
    private void abrirVentanaMesa(int numeroMesa) {
        VentanaPedido pedido = new VentanaPedido(this, numeroMesa);
        pedido.setVisible(true);
    }

    // Muestra un resumen rápido del pedido actual de una mesa (doble click)
    private void verPedidoMesa(int numeroMesa) {
        // Verificar si la mesa tiene pedido
        if (!ModeloPedidos.tienePedido(numeroMesa)) {
            JOptionPane.showMessageDialog(this,
                    "La Mesa " + numeroMesa + " no tiene pedidos.",
                    "Mesa Vacía",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            String sql = "SELECT p.nombre, mp.cantidad, p.precio, (mp.cantidad * p.precio) as subtotal " +
                    "FROM `mesa pedido` mp " +
                    "JOIN productos p ON mp.producto_id = p.id " +
                    "WHERE mp.mesa = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numeroMesa);
            ResultSet rs = ps.executeQuery();

            StringBuilder mensaje = new StringBuilder();
            mensaje.append("═══════════════════════════════════\n");
            mensaje.append("           MESA ").append(numeroMesa).append("\n");
            mensaje.append("═══════════════════════════════════\n\n");

            double total = 0;

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                double precio = rs.getDouble("precio");
                double subtotal = rs.getDouble("subtotal");

                mensaje.append(String.format("%-20s x%d\n", nombre, cantidad));
                mensaje.append(String.format("   $%.2f c/u = $%.2f\n\n", precio, subtotal));

                total += subtotal;
            }

            mensaje.append("───────────────────────────────────\n");
            mensaje.append(String.format("TOTAL: $%.2f\n", total));
            mensaje.append("═══════════════════════════════════");

            rs.close();
            ps.close();
            con.close();

            // Mostrar diálogo con el pedido
            JTextArea textArea = new JTextArea(mensaje.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            textArea.setEditable(false);

            JOptionPane.showMessageDialog(this, textArea,
                    "Pedido Mesa " + numeroMesa, JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al consultar el pedido: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Muestra el resumen del día con el total de todas las mesas
    private void mostrarResumenDia() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

            // Consulta para obtener el resumen por mesa
            String sqlMesas = "SELECT mp.mesa, SUM(mp.cantidad * p.precio) as total " +
                    "FROM `mesa pedido` mp " +
                    "JOIN productos p ON mp.producto_id = p.id " +
                    "GROUP BY mp.mesa " +
                    "ORDER BY mp.mesa";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlMesas);

            StringBuilder mensaje = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String fecha = sdf.format(new Date());

            mensaje.append("═══════════════════════════════════════\n");
            mensaje.append("       RESUMEN DEL DÍA\n");
            mensaje.append("       ").append(fecha).append("\n");
            mensaje.append("═══════════════════════════════════════\n\n");

            double totalGeneral = 0;
            int mesasOcupadas = 0;

            while (rs.next()) {
                int mesa = rs.getInt("mesa");
                double totalMesa = rs.getDouble("total");

                mensaje.append(String.format("Mesa %2d ................. $%.2f\n", mesa, totalMesa));

                totalGeneral += totalMesa;
                mesasOcupadas++;
            }

            mensaje.append("\n───────────────────────────────────────\n");
            mensaje.append(String.format("Mesas ocupadas: %d\n", mesasOcupadas));
            mensaje.append(String.format("Mesas libres: %d\n", (40 - mesasOcupadas)));
            mensaje.append("───────────────────────────────────────\n");
            mensaje.append(String.format("TOTAL GENERAL: $%.2f\n", totalGeneral));
            mensaje.append("═══════════════════════════════════════");

            rs.close();
            st.close();
            con.close();

            // Mostrar diálogo con el resumen
            JTextArea textArea = new JTextArea(mensaje.toString());
            textArea.setFont(new Font("Monospaced", Font.BOLD, 14));
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Resumen del Día", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar el resumen: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Genera el resumen mensual y limpia la base de datos
    private void mostrarResumenMensual() {
        // Primera confirmación
        int confirmacion1 = JOptionPane.showConfirmDialog(this,
                "⚠️ ATENCIÓN ⚠️\n\n" +
                        "Esta acción generará el resumen del mes actual\n" +
                        "y BORRARÁ TODOS los pedidos de la base de datos.\n\n" +
                        "¿Desea continuar?",
                "Confirmación - Resumen Mensual",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion1 != JOptionPane.YES_OPTION) {
            return;
        }

        // Segunda confirmación
        int confirmacion2 = JOptionPane.showConfirmDialog(this,
                "⚠️ ÚLTIMA CONFIRMACIÓN ⚠️\n\n" +
                        "Esta acción NO SE PUEDE DESHACER.\n" +
                        "Se borrarán TODOS los pedidos actuales.\n\n" +
                        "¿Está completamente seguro?",
                "Confirmación Final",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

        if (confirmacion2 != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

            // Obtener resumen mensual antes de borrar
            String sqlResumen = "SELECT mp.mesa, p.nombre, mp.cantidad, p.precio, (mp.cantidad * p.precio) as subtotal " +
                    "FROM `mesa pedido` mp " +
                    "JOIN productos p ON mp.producto_id = p.id " +
                    "ORDER BY mp.mesa, p.nombre";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlResumen);

            StringBuilder resumen = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
            String mesAnio = sdf.format(new Date());

            resumen.append("═══════════════════════════════════════════════════\n");
            resumen.append("           RESUMEN MENSUAL\n");
            resumen.append("           ").append(mesAnio.toUpperCase()).append("\n");
            resumen.append("═══════════════════════════════════════════════════\n\n");

            double totalGeneral = 0;
            int mesaActual = -1;
            double totalMesa = 0;

            while (rs.next()) {
                int mesa = rs.getInt("mesa");
                String producto = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                double precio = rs.getDouble("precio");
                double subtotal = rs.getDouble("subtotal");

                // Si cambia de mesa, mostrar el total de la mesa anterior
                if (mesaActual != -1 && mesaActual != mesa) {
                    resumen.append(String.format("\n   Total Mesa %d: $%.2f\n", mesaActual, totalMesa));
                    resumen.append("───────────────────────────────────────────────────\n\n");
                    totalMesa = 0;
                }

                // Si es una nueva mesa, mostrar encabezado
                if (mesaActual != mesa) {
                    resumen.append(String.format("MESA %d:\n", mesa));
                    mesaActual = mesa;
                }

                resumen.append(String.format("  %-25s x%3d  $%8.2f\n", producto, cantidad, subtotal));

                totalMesa += subtotal;
                totalGeneral += subtotal;
            }

            // Total de la última mesa
            if (mesaActual != -1) {
                resumen.append(String.format("\n   Total Mesa %d: $%.2f\n", mesaActual, totalMesa));
                resumen.append("───────────────────────────────────────────────────\n\n");
            }

            resumen.append("═══════════════════════════════════════════════════\n");
            resumen.append(String.format("TOTAL MENSUAL: $%.2f\n", totalGeneral));
            resumen.append("═══════════════════════════════════════════════════\n");

            rs.close();
            st.close();

            // Mostrar resumen antes de borrar
            JTextArea textArea = new JTextArea(resumen.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(550, 500));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Resumen Mensual - " + mesAnio, JOptionPane.INFORMATION_MESSAGE);

            // Ahora borrar todos los pedidos
            String sqlDelete = "DELETE FROM `mesa pedido`";
            Statement stDelete = con.createStatement();
            int registrosBorrados = stDelete.executeUpdate(sqlDelete);

            stDelete.close();
            con.close();

            // Actualizar estado de todas las mesas
            for (int i = 1; i <= 40; i++) {
                actualizarEstadoMesa(i);
            }

            JOptionPane.showMessageDialog(this,
                    "Resumen mensual generado correctamente.\n" +
                            registrosBorrados + " registros eliminados.\n" +
                            "Las mesas han sido reiniciadas.",
                    "Operación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al generar el resumen mensual: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Actualiza el estado visual de la mesa (color y contadores)
    public void actualizarEstadoMesa(int numeroMesa) {
        if (numeroMesa < 1 || numeroMesa > mesas.length) return;

        boolean tienePedido = ModeloPedidos.tienePedido(numeroMesa);

        JButton btn = mesas[numeroMesa - 1];
        if (tienePedido) {
            btn.setBackground(Color.RED);
            btn.putClientProperty("estado", "ocupada");
        } else {
            btn.setBackground(Color.GREEN);
            btn.putClientProperty("estado", "libre");
        }

        // Actualizar contadores de mesas libres y ocupadas
        int totLibres = 0;
        int totOcupadas = 0;
        for (int i = 0; i < mesas.length; i++) {
            if (ModeloPedidos.tienePedido(i + 1)) totOcupadas++;
            else totLibres++;
        }
        libres = totLibres;
        ocupadas = totOcupadas;

        lblLibres.setText("Mesas libres: " + libres);
        lblOcupadas.setText("Mesas ocupadas: " + ocupadas);

        btn.repaint();
    }
    // java
    // Archivo: `src/main/java/MenuPrincipal.java`
    public void actualizarEstadoMesa(int numeroMesa, boolean ocupada) {
        if (numeroMesa < 1 || numeroMesa > mesas.length) return;

        JButton btn = mesas[numeroMesa - 1];
        if (ocupada) {
            btn.setBackground(Color.RED);
            btn.putClientProperty("estado", "ocupada");
        } else {
            btn.setBackground(Color.GREEN);
            btn.putClientProperty("estado", "libre");
        }

        // Recalcular contadores usando la propiedad 'estado'
        int totLibres = 0, totOcupadas = 0;
        for (JButton b : mesas) {
            Object est = b.getClientProperty("estado");
            if ("ocupada".equals(est)) totOcupadas++;
            else totLibres++;
        }
        libres = totLibres;
        ocupadas = totOcupadas;

        lblLibres.setText("Mesas libres: " + libres);
        lblOcupadas.setText("Mesas ocupadas: " + ocupadas);

        // Asegurar repintado en EDT
        SwingUtilities.invokeLater(btn::repaint);
    }

}
