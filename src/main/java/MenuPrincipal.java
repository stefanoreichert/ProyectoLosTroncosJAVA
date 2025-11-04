import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MenuPrincipal extends JFrame {
    private final JButton[] mesas = new JButton[40];
    private JLabel lblLibres;
    private JLabel lblOcupadas;
    private int libres = 40;
    private int ocupadas = 0;
    private java.time.LocalDateTime[] horaPrimerPedido = new java.time.LocalDateTime[40];
    private java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

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

            // Obtener hora del primer pedido desde la BD
            String horaTexto = "";
            try {
                LocalDateTime horaDesdeDB = ModeloPedidos.getHoraPrimerPedido(numeroMesa);
                if (horaDesdeDB != null) {
                    horaPrimerPedido[i] = horaDesdeDB;
                    horaTexto = "\n" + horaDesdeDB.format(formato);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Crear botón con HTML para mostrar texto en dos líneas
            String textoBoton = "<html><center>Mesa " + numeroMesa + horaTexto + "</center></html>";
            mesas[i] = new JButton(textoBoton);

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
            mesas[i].setFont(new Font("Arial", Font.BOLD, 14));

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

        // Etiqueta de ayuda para doble click
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
                mostrarResumenMes();
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

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaHoy = new Date();
            String fecha = sdf.format(fechaHoy);
            String fechaDB = sdfDB.format(fechaHoy);

            // Consultar resumenes_diarios de hoy
            String sqlDia = "SELECT mesa, total, productos FROM resumenes_diarios WHERE fecha = ? ORDER BY mesa";
            PreparedStatement ps = con.prepareStatement(sqlDia);
            ps.setString(1, fechaDB);
            ResultSet rs = ps.executeQuery();

            StringBuilder mensaje = new StringBuilder();
            mensaje.append("═══════════════════════════════════════\n");
            mensaje.append("       RESUMEN DEL DÍA\n");
            mensaje.append("       ").append(fecha).append("\n");
            mensaje.append("═══════════════════════════════════════\n\n");

            double totalGeneral = 0;
            int mesasAtendidas = 0;
            List<String> datosMesas = new ArrayList<>();

            while (rs.next()) {
                int mesa = rs.getInt("mesa");
                double totalMesa = rs.getDouble("total");
                String productos = rs.getString("productos");

                String lineaMesa = String.format("Mesa %2d ................. $%.2f\n", mesa, totalMesa);
                mensaje.append(lineaMesa);
                datosMesas.add(lineaMesa);

                totalGeneral += totalMesa;
                mesasAtendidas++;
            }

            if (mesasAtendidas == 0) {
                mensaje.append("\n⚠️  No hay ventas registradas hoy\n\n");
            }

            mensaje.append("\n───────────────────────────────────────\n");
            mensaje.append(String.format("Mesas atendidas: %d\n", mesasAtendidas));
            mensaje.append("───────────────────────────────────────\n");
            mensaje.append(String.format("TOTAL DEL DÍA: $%.2f\n", totalGeneral));
            mensaje.append("═══════════════════════════════════════");

            rs.close();
            ps.close();

            // Variables finales
            final double totalFinal = totalGeneral;
            final int mesasAtendidasFinal = mesasAtendidas;
            final String fechaFinal = fecha;
            final List<String> datosMesasFinal = new ArrayList<>(datosMesas);

            // Panel con botones
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JTextArea textArea = new JTextArea(mensaje.toString());
            textArea.setFont(new Font("Monospaced", Font.BOLD, 14));
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 400));
            panel.add(scrollPane, BorderLayout.CENTER);

            // Botones
            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

            JButton btnImprimir = new JButton("🖨 Imprimir");
            btnImprimir.setEnabled(mesasAtendidas > 0);
            btnImprimir.addActionListener(e -> {
                imprimirResumenDia(fechaFinal, datosMesasFinal, mesasAtendidasFinal, totalFinal);
            });

            JButton btnCerrarDia = new JButton("💾 Cerrar Día");
            btnCerrarDia.setBackground(new Color(76, 175, 80));
            btnCerrarDia.setForeground(Color.WHITE);
            btnCerrarDia.setEnabled(mesasAtendidas > 0);
            btnCerrarDia.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Cerrar el día?\n" +
                                "Se guardará el total en resumen mensual\n" +
                                "y se limpiarán los tickets del día.",
                        "Confirmar Cierre de Día",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        int dia = cal.get(Calendar.DAY_OF_MONTH);
                        int mes = cal.get(Calendar.MONTH) + 1;
                        int anio = cal.get(Calendar.YEAR);

                        // Guardar en resumenes_mensuales
                        String sqlInsertMensual = "INSERT INTO resumenes_mensuales (fecha, dia, mes, anio, total_dia, mesas_atendidas) " +
                                "VALUES (?, ?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE total_dia = total_dia + ?, mesas_atendidas = mesas_atendidas + ?";
                        PreparedStatement psInsertMensual = con.prepareStatement(sqlInsertMensual);
                        psInsertMensual.setString(1, fechaDB);
                        psInsertMensual.setInt(2, dia);
                        psInsertMensual.setInt(3, mes);
                        psInsertMensual.setInt(4, anio);
                        psInsertMensual.setDouble(5, totalFinal);
                        psInsertMensual.setInt(6, mesasAtendidasFinal);
                        psInsertMensual.setDouble(7, totalFinal);
                        psInsertMensual.setInt(8, mesasAtendidasFinal);
                        psInsertMensual.executeUpdate();
                        psInsertMensual.close();

                        // Borrar resumenes_diarios de hoy
                        String sqlDelete = "DELETE FROM resumenes_diarios WHERE fecha = ?";
                        PreparedStatement psDelete = con.prepareStatement(sqlDelete);
                        psDelete.setString(1, fechaDB);
                        int filasEliminadas = psDelete.executeUpdate();
                        psDelete.close();

                        con.close();

                        JOptionPane.showMessageDialog(this,
                                "Día cerrado correctamente.\n" +
                                        "Total guardado: $" + String.format("%.2f", totalFinal) + "\n" +
                                        filasEliminadas + " tickets eliminados.",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);

                        Window window = SwingUtilities.getWindowAncestor((Component) e.getSource());
                        window.dispose();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "Error: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            panelBotones.add(btnImprimir);
            panelBotones.add(btnCerrarDia);
            panel.add(panelBotones, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, panel,
                    "Resumen del Día", JOptionPane.INFORMATION_MESSAGE);

            if (con != null && !con.isClosed()) {
                con.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para imprimir el resumen del día
    private void imprimirResumenDia(String fecha, List<String> datosMesas, int mesasOcupadas, double totalGeneral) {
        PrinterJob job = PrinterJob.getPrinterJob();

        // Configurar formato de página para 48mm
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();

        double width = 48 * 2.834645669;  // 48mm
        double height = 842;

        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable(new ResumenDiaPrintable(fecha, datosMesas, mesasOcupadas, totalGeneral), pf);

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "Resumen impreso correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al imprimir: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Clase interna para imprimir el resumen
    private class ResumenDiaPrintable implements Printable {
        private String fecha;
        private List<String> datosMesas;
        private int mesasOcupadas;
        private double totalGeneral;

        public ResumenDiaPrintable(String fecha, List<String> datosMesas, int mesasOcupadas, double totalGeneral) {
            this.fecha = fecha;
            this.datosMesas = datosMesas;
            this.mesasOcupadas = mesasOcupadas;
            this.totalGeneral = totalGeneral;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            // Fuentes optimizadas para 48mm (igual que TicketPrintable)
            Font fTitulo = new Font("Courier New", Font.BOLD, 10);
            Font fNormal = new Font("Courier New", Font.PLAIN, 8);
            Font fPequena = new Font("Courier New", Font.PLAIN, 7);
            Font fTotal = new Font("Courier New", Font.BOLD, 10);

            int y = 10;
            int lineHeight = 11;
            int margen = 3;
            int anchoTicket = (int) pf.getImageableWidth();

            // === ENCABEZADO ===
            g2d.setFont(fTitulo);
            String titulo = "LOS TRONCOS RESTO BAR";
            int anchoTitulo = g2d.getFontMetrics().stringWidth(titulo);
            g2d.drawString(titulo, (anchoTicket - anchoTitulo) / 2, y);
            y += lineHeight;

            // Subtítulo
            g2d.setFont(fNormal);
            String subtitulo = "RESUMEN DEL DIA";
            int anchoSubtitulo = g2d.getFontMetrics().stringWidth(subtitulo);
            g2d.drawString(subtitulo, (anchoTicket - anchoSubtitulo) / 2, y);
            y += lineHeight;

            // Fecha
            g2d.setFont(fPequena);
            g2d.drawString(fecha, margen, y);
            y += lineHeight;

            // Línea separadora
            g2d.setFont(fNormal);
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === DATOS DE LAS MESAS ===
            for (String linea : datosMesas) {
                // Parsear la línea para obtener mesa y total
                String[] partes = linea.split("\\.");
                if (partes.length >= 2) {
                    String mesaStr = partes[0].trim(); // "Mesa X"
                    String totalStr = partes[partes.length - 1].trim().replace("\n", ""); // "$XX.XX"

                    g2d.drawString(mesaStr, margen, y);

                    // Alinear total a la derecha
                    int anchoTotal = g2d.getFontMetrics().stringWidth(totalStr);
                    g2d.drawString(totalStr, anchoTicket - anchoTotal - margen, y);

                    y += lineHeight;
                }
            }

            // Línea separadora antes del resumen
            y += 2;
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === RESUMEN ===
            g2d.drawString("Mesas ocupadas: " + mesasOcupadas, margen, y);
            y += lineHeight;
            g2d.drawString("Mesas libres: " + (40 - mesasOcupadas), margen, y);
            y += lineHeight + 2;

            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === TOTAL GENERAL ===
            g2d.setFont(fTotal);
            String textoTotal = "TOTAL GENERAL:";
            String valorTotal = String.format("$%.2f", totalGeneral);

            g2d.drawString(textoTotal, margen, y);
            y += lineHeight;

            int anchoValorTotal = g2d.getFontMetrics().stringWidth(valorTotal);
            g2d.drawString(valorTotal, (anchoTicket - anchoValorTotal) / 2, y);
            y += lineHeight + 5;

            // Línea separadora final
            g2d.setFont(fNormal);
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === PIE DE PÁGINA ===
            g2d.setFont(fPequena);
            String gracias = "Gracias por su visita";
            int anchoGracias = g2d.getFontMetrics().stringWidth(gracias);
            g2d.drawString(gracias, (anchoTicket - anchoGracias) / 2, y);
            y += lineHeight - 2;

            String vuelva = "Vuelva pronto!";
            int anchoVuelva = g2d.getFontMetrics().stringWidth(vuelva);
            g2d.drawString(vuelva, (anchoTicket - anchoVuelva) / 2, y);

            return PAGE_EXISTS;
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

            // Lista para guardar datos de impresión
            List<DatosMesa> datosMesas = new ArrayList<>();
            List<String> itemsMesaActual = new ArrayList<>();

            while (rs.next()) {
                int mesa = rs.getInt("mesa");
                String producto = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                double precio = rs.getDouble("precio");
                double subtotal = rs.getDouble("subtotal");

                // Si cambia de mesa, guardar datos de la mesa anterior
                if (mesaActual != -1 && mesaActual != mesa) {
                    resumen.append(String.format("\n   Total Mesa %d: $%.2f\n", mesaActual, totalMesa));
                    resumen.append("───────────────────────────────────────────────────\n\n");

                    // Guardar datos de la mesa para imprimir
                    datosMesas.add(new DatosMesa(mesaActual, new ArrayList<>(itemsMesaActual), totalMesa));
                    itemsMesaActual.clear();
                    totalMesa = 0;
                }

                // Si es una nueva mesa, mostrar encabezado
                if (mesaActual != mesa) {
                    resumen.append(String.format("MESA %d:\n", mesa));
                    mesaActual = mesa;
                }

                String lineaItem = String.format("  %-25s x%3d  $%8.2f\n", producto, cantidad, subtotal);
                resumen.append(lineaItem);
                itemsMesaActual.add(String.format("%-18s x%d $%.2f",
                        producto.length() > 18 ? producto.substring(0, 18) : producto,
                        cantidad, subtotal));

                totalMesa += subtotal;
                totalGeneral += subtotal;
            }

            // Total de la última mesa
            if (mesaActual != -1) {
                resumen.append(String.format("\n   Total Mesa %d: $%.2f\n", mesaActual, totalMesa));
                resumen.append("───────────────────────────────────────────────────\n\n");

                // Guardar datos de la última mesa
                datosMesas.add(new DatosMesa(mesaActual, new ArrayList<>(itemsMesaActual), totalMesa));
            }

            resumen.append("═══════════════════════════════════════════════════\n");
            resumen.append(String.format("TOTAL MENSUAL: $%.2f\n", totalGeneral));
            resumen.append("═══════════════════════════════════════════════════\n");

            rs.close();
            st.close();

            // Crear panel con botones
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            // TextArea con el resumen
            JTextArea textArea = new JTextArea(resumen.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(550, 500));

            panel.add(scrollPane, BorderLayout.CENTER);

            // Panel de botones
            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

            JButton btnImprimir = new JButton("🖨 Imprimir Resumen");
            btnImprimir.setFont(new Font("Arial", Font.BOLD, 12));

            // Variables finales para usar en el listener
            final double totalFinal = totalGeneral;
            final String mesAnioFinal = mesAnio;
            final List<DatosMesa> datosMesasFinal = new ArrayList<>(datosMesas);

            btnImprimir.addActionListener(e -> {
                imprimirResumenMensual(mesAnioFinal, datosMesasFinal, totalFinal);
            });

            panelBotones.add(btnImprimir);
            panel.add(panelBotones, BorderLayout.SOUTH);

            // Mostrar resumen antes de borrar
            JOptionPane.showMessageDialog(this, panel,
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

    private void mostrarResumenMes() {
        JFrame frame = new JFrame("Resumen del Mes");
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Selector de mes y año
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        JComboBox<String> comboMes = new JComboBox<>(meses);

        JComboBox<Integer> comboAnio = new JComboBox<>();
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = anioActual; i >= anioActual - 3; i--) {
            comboAnio.addItem(i);
        }

        Calendar cal = Calendar.getInstance();
        comboMes.setSelectedIndex(cal.get(Calendar.MONTH));

        JButton btnBuscar = new JButton("🔍 Buscar");

        panelSuperior.add(new JLabel("Mes:"));
        panelSuperior.add(comboMes);
        panelSuperior.add(new JLabel("Año:"));
        panelSuperior.add(comboAnio);
        panelSuperior.add(btnBuscar);

        panel.add(panelSuperior, BorderLayout.NORTH);

        // Área de texto
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.BOLD, 13));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelInferior = new JPanel(new BorderLayout());
        JLabel lblTotal = new JLabel("TOTAL DEL MES: $0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnCerrarMes = new JButton("💾 Cerrar Mes");
        btnCerrarMes.setBackground(new Color(244, 67, 54));
        btnCerrarMes.setForeground(Color.WHITE);

        panelInferior.add(lblTotal, BorderLayout.CENTER);
        panelInferior.add(btnCerrarMes, BorderLayout.SOUTH);
        panel.add(panelInferior, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

                int mesSeleccionado = comboMes.getSelectedIndex() + 1;
                int anioSeleccionado = (Integer) comboAnio.getSelectedItem();

                String sql = "SELECT fecha, total_dia, mesas_atendidas FROM resumenes_mensuales " +
                        "WHERE mes = ? AND anio = ? ORDER BY fecha";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, mesSeleccionado);
                ps.setInt(2, anioSeleccionado);
                ResultSet rs = ps.executeQuery();

                StringBuilder resumen = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                resumen.append("═══════════════════════════════════════\n");
                resumen.append("    RESUMEN MENSUAL\n");
                resumen.append("    ").append(comboMes.getSelectedItem()).append(" ").append(anioSeleccionado).append("\n");
                resumen.append("═══════════════════════════════════════\n\n");

                double totalMes = 0;
                int diasConVentas = 0;
                int totalMesasAtendidas = 0;

                while (rs.next()) {
                    Date fecha = rs.getDate("fecha");
                    double totalDia = rs.getDouble("total_dia");
                    int mesasAtendidas = rs.getInt("mesas_atendidas");

                    resumen.append(String.format("%s ........ $%.2f (%d mesas)\n",
                            sdf.format(fecha), totalDia, mesasAtendidas));

                    totalMes += totalDia;
                    diasConVentas++;
                    totalMesasAtendidas += mesasAtendidas;
                }

                if (diasConVentas == 0) {
                    resumen.append("\n⚠️  No hay ventas en este mes\n\n");
                }

                resumen.append("\n───────────────────────────────────────\n");
                resumen.append(String.format("Días con ventas: %d\n", diasConVentas));
                resumen.append(String.format("Total mesas: %d\n", totalMesasAtendidas));
                resumen.append(String.format("Promedio diario: $%.2f\n", diasConVentas > 0 ? totalMes / diasConVentas : 0));
                resumen.append("═══════════════════════════════════════");

                textArea.setText(resumen.toString());
                lblTotal.setText(String.format("TOTAL DEL MES: $%.2f", totalMes));

                btnCerrarMes.setEnabled(diasConVentas > 0);

                final double totalMesFinal = totalMes;
                final int mesSeleccionadoFinal = mesSeleccionado;
                final int anioSeleccionadoFinal = anioSeleccionado;

                btnCerrarMes.addActionListener(ev -> {
                    int confirm = JOptionPane.showConfirmDialog(frame,
                            "¿Cerrar el mes?\n" +
                                    "Se eliminarán todos los resúmenes diarios de este mes.\n" +
                                    "Total del mes: $" + String.format("%.2f", totalMesFinal),
                            "Confirmar Cierre de Mes",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            Connection conDelete = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

                            String sqlDelete = "DELETE FROM resumenes_mensuales WHERE mes = ? AND anio = ?";
                            PreparedStatement psDelete = conDelete.prepareStatement(sqlDelete);
                            psDelete.setInt(1, mesSeleccionadoFinal);
                            psDelete.setInt(2, anioSeleccionadoFinal);
                            int filasEliminadas = psDelete.executeUpdate();
                            psDelete.close();
                            conDelete.close();

                            JOptionPane.showMessageDialog(frame,
                                    "Mes cerrado correctamente.\n" +
                                            filasEliminadas + " días eliminados.\n" +
                                            "Total del mes: $" + String.format("%.2f", totalMesFinal),
                                    "Éxito",
                                    JOptionPane.INFORMATION_MESSAGE);

                            frame.dispose();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame,
                                    "Error: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                rs.close();
                ps.close();
                con.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame,
                        "Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(panel);
        frame.setVisible(true);

        btnBuscar.doClick();
    }

    // Clase auxiliar para almacenar datos de cada mesa
    private class DatosMesa {
        int numeroMesa;
        List<String> items;
        double total;

        DatosMesa(int numeroMesa, List<String> items, double total) {
            this.numeroMesa = numeroMesa;
            this.items = items;
            this.total = total;
        }
    }

    private void imprimirResumenMensual(String mesAnio, List<DatosMesa> datosMesas, double totalGeneral) {
        PrinterJob job = PrinterJob.getPrinterJob();

        // Configurar formato de página para 48mm
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();

        double width = 48 * 2.834645669;  // 48mm
        double height = 842;

        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable(new ResumenMensualPrintable(mesAnio, datosMesas, totalGeneral), pf);

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "Resumen mensual impreso correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al imprimir: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Clase interna para imprimir el resumen mensual
    private class ResumenMensualPrintable implements Printable {
        private String mesAnio;
        private List<DatosMesa> datosMesas;
        private double totalGeneral;

        public ResumenMensualPrintable(String mesAnio, List<DatosMesa> datosMesas, double totalGeneral) {
            this.mesAnio = mesAnio;
            this.datosMesas = datosMesas;
            this.totalGeneral = totalGeneral;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            // Fuentes optimizadas para 48mm (igual que TicketPrintable)
            Font fTitulo = new Font("Courier New", Font.BOLD, 10);
            Font fNormal = new Font("Courier New", Font.PLAIN, 8);
            Font fPequena = new Font("Courier New", Font.PLAIN, 7);
            Font fTotal = new Font("Courier New", Font.BOLD, 10);

            int y = 10;
            int lineHeight = 11;
            int margen = 3;
            int anchoTicket = (int) pf.getImageableWidth();

            // === ENCABEZADO ===
            g2d.setFont(fTitulo);
            String titulo = "LOS TRONCOS RESTO BAR";
            int anchoTitulo = g2d.getFontMetrics().stringWidth(titulo);
            g2d.drawString(titulo, (anchoTicket - anchoTitulo) / 2, y);
            y += lineHeight;

            // Subtítulo
            g2d.setFont(fNormal);
            String subtitulo = "RESUMEN MENSUAL";
            int anchoSubtitulo = g2d.getFontMetrics().stringWidth(subtitulo);
            g2d.drawString(subtitulo, (anchoTicket - anchoSubtitulo) / 2, y);
            y += lineHeight;

            // Mes y año
            g2d.setFont(fPequena);
            String mesAnioUpper = mesAnio.toUpperCase();
            int anchoMes = g2d.getFontMetrics().stringWidth(mesAnioUpper);
            g2d.drawString(mesAnioUpper, (anchoTicket - anchoMes) / 2, y);
            y += lineHeight;

            // Línea separadora
            g2d.setFont(fNormal);
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === DATOS POR MESA ===
            for (DatosMesa mesa : datosMesas) {
                // Encabezado de mesa
                g2d.drawString("MESA " + mesa.numeroMesa, margen, y);
                y += lineHeight;

                // Items de la mesa
                g2d.setFont(fPequena);
                for (String item : mesa.items) {
                    if (item.length() > 26) {
                        item = item.substring(0, 23) + "...";
                    }
                    g2d.drawString(item, margen + 2, y);
                    y += lineHeight - 1;
                }

                // Total de la mesa
                g2d.setFont(fNormal);
                String totalMesaStr = String.format("Total: $%.2f", mesa.total);
                int anchoTotalMesa = g2d.getFontMetrics().stringWidth(totalMesaStr);
                g2d.drawString(totalMesaStr, anchoTicket - anchoTotalMesa - margen, y);
                y += lineHeight;

                // Línea separadora entre mesas
                g2d.drawString("--------------------------", margen, y);
                y += lineHeight;
            }

            // Línea separadora antes del total
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === TOTAL MENSUAL ===
            g2d.setFont(fTotal);
            String textoTotal = "TOTAL MENSUAL:";
            g2d.drawString(textoTotal, margen, y);
            y += lineHeight;

            String valorTotal = String.format("$%.2f", totalGeneral);
            int anchoValorTotal = g2d.getFontMetrics().stringWidth(valorTotal);
            g2d.drawString(valorTotal, (anchoTicket - anchoValorTotal) / 2, y);
            y += lineHeight + 5;

            // Línea separadora final
            g2d.setFont(fNormal);
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === PIE DE PÁGINA ===
            g2d.setFont(fPequena);
            String gracias = "Gracias por su visita";
            int anchoGracias = g2d.getFontMetrics().stringWidth(gracias);
            g2d.drawString(gracias, (anchoTicket - anchoGracias) / 2, y);
            y += lineHeight - 2;

            String vuelva = "Vuelva pronto!";
            int anchoVuelva = g2d.getFontMetrics().stringWidth(vuelva);
            g2d.drawString(vuelva, (anchoTicket - anchoVuelva) / 2, y);

            return PAGE_EXISTS;
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

        try {
            java.time.LocalDateTime hora = ModeloPedidos.getHoraPrimerPedido(numeroMesa);
            if (hora != null) {
                // 'formato' debe ser un DateTimeFormatter definido en la clase (ej: "HH:mm")
                btn.setText("<html>Mesa " + numeroMesa + "<br>" + hora.format(formato) + "</html>");

            } else {
                // si no hay hora registrada mostramos el texto simple
                btn.setText("Mesa " + numeroMesa);
            }
        } catch (Exception ex) {
            // si por alguna razón no existe el método en ModeloPedidos o falla,
            // dejamos el texto por defecto para no romper la UI
            btn.setText("Mesa " + numeroMesa);
        }
        // --- fin añadido ---

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


}
