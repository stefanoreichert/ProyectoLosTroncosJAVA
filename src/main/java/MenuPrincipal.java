import javax.swing.*; // componentes gráficos (ventanas, botones, tablas)
import java.awt.*; // colores, fuentes, tamaños
import java.awt.event.ActionEvent; // evento de botón presionado
import java.awt.event.ActionListener; // escucha acciones (botones)
import java.awt.event.MouseAdapter; // facilita eventos del mouse
import java.awt.event.MouseEvent; // datos del clic del mouse
import java.awt.print.PageFormat; // formato de página
import java.awt.print.Paper; // tamaño del papel
import java.awt.print.Printable; // permite imprimir un componente
import java.awt.print.PrinterException; // error al imprimir
import java.awt.print.PrinterJob; // trabajo de impresión
import java.sql.*; // conexión con base de datos (Connection, ResultSet)
import java.text.SimpleDateFormat; // formatear fechas
import java.time.LocalDateTime; // fecha y hora actual
import java.time.format.DateTimeFormatter; // formato para LocalDateTime
import java.util.ArrayList; // lista dinámica
import java.util.Calendar; // manejo de fechas
import java.util.Date; // fecha clásica
import java.util.List; // interfaz de lista


// Clase principal del sistema de gestión de restaurante
public class MenuPrincipal extends JFrame {
    // atributos
    private final JButton[] mesas = new JButton[40]; // botones de las mesas
    private JLabel lblLibres;
    private JLabel lblOcupadas;
    private int libres = 40;
    private int ocupadas = 0;
    private java.time.LocalDateTime[] horaPrimerPedido = new java.time.LocalDateTime[40]; // hora del primer pedido por mesa
    private java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("HH:mm"); // formato hora:mm

    // Constructor
    public MenuPrincipal() {
        Usuario usuario = SesionUsuario.getInstancia().getUsuarioActual();
        String nombreUsuario = usuario != null ? usuario.getNombre() : "Usuario";
        String rol = usuario != null ? usuario.getRol() : "MOZO"; // Por defecto MOZO

        // Configuración de la ventana principal
        setTitle("Sistema Restaurante - Mesas | " + nombreUsuario + " (" + rol + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Crear la barra de menú (adaptada según rol)
        crearBarraMenu();

        // Panel superior con título y usuario
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(33, 150, 243));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("🍽️ Sistema Restaurante - Mesas");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        top.add(titulo, BorderLayout.WEST);

        // Panel info usuario
        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelUsuario.setOpaque(false);

        // Etiqueta con nombre y rol del usuario
        JLabel lblUsuarioInfo = new JLabel("👤 " + nombreUsuario + " | " + rol);
        lblUsuarioInfo.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsuarioInfo.setForeground(Color.WHITE);

        // Botón cerrar sesión
        JButton btnCerrarSesion = new JButton("🚪 Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 12));
        btnCerrarSesion.setBackground(new Color(244, 67, 54));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        // Agregar componentes al panel usuario
        panelUsuario.add(lblUsuarioInfo);
        panelUsuario.add(Box.createHorizontalStrut(15));
        panelUsuario.add(btnCerrarSesion);
        top.add(panelUsuario, BorderLayout.EAST);

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

            // al hacer clic en una mesa se abre la ventana de pedido
            mesas[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        abrirVentanaMesa(numeroMesa); // clic simple
                    } else if (e.getClickCount() == 2) {
                        verPedidoMesa(numeroMesa); // doble clic
                    }
                }
            });

            center.add(mesas[i]);
        }

        // Inicializar contadores
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

        // Agregar componentes al panel derecho
        right.add(Box.createVerticalStrut(10));
        right.add(lblLibres);
        right.add(Box.createVerticalStrut(10));
        right.add(lblOcupadas);
        right.add(Box.createVerticalStrut(20));
        right.add(lblAyuda);
        right.add(Box.createVerticalGlue());
        add(right, BorderLayout.EAST);
    }

    // Crea la barra de menú con todas las opciones (adaptada según rol)
    private void crearBarraMenu() {
        Usuario usuario = SesionUsuario.getInstancia().getUsuarioActual();
        boolean esAdmin = usuario != null && usuario.esAdmin();
        boolean esMozo = usuario != null && usuario.esMozo();

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

        // Menú Reportes (solo si es ADMIN)
        if (esAdmin) {
            JMenu menuReportes = new JMenu("Reportes");

            JMenuItem itemResumenDia = new JMenuItem("📊 Resumen del Día");
            itemResumenDia.setFont(new Font("Arial", Font.PLAIN, 13));
            itemResumenDia.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mostrarResumenDia();
                }
            });

            // Resumen Mensual
            JMenuItem itemResumenMes = new JMenuItem("📅 Resumen del Mes");
            itemResumenMes.setFont(new Font("Arial", Font.PLAIN, 13));
            itemResumenMes.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mostrarResumenMes();
                }
            });

            menuReportes.add(itemResumenDia);
            menuReportes.addSeparator();
            menuReportes.add(itemResumenMes);
            menuBar.add(menuReportes);
        }

        // Menú Administración (solo ADMIN)
        if (esAdmin) {
            JMenu menuAdmin = new JMenu("Administración");

            JMenuItem itemGestionarUsuarios = new JMenuItem("👥 Gestionar Usuarios");
            itemGestionarUsuarios.setFont(new Font("Arial", Font.PLAIN, 13));
            itemGestionarUsuarios.addActionListener(e -> abrirGestionUsuarios());

            JMenuItem itemGestionarProductos = new JMenuItem("📦 Gestionar Productos");
            itemGestionarProductos.setFont(new Font("Arial", Font.PLAIN, 13));
            itemGestionarProductos.addActionListener(e -> abrirGestionProductos());

            menuAdmin.add(itemGestionarUsuarios);
            menuAdmin.add(itemGestionarProductos);
            menuBar.add(menuAdmin);
        }

        // Menú Vista (disponible para ADMIN y MOZO - para ver cocina si son admin)
        if (esAdmin) {
            JMenu menuVista = new JMenu("Vista");

            JMenuItem itemVistaCocina = new JMenuItem("👨‍🍳 Ver Vista de Cocina");
            itemVistaCocina.setFont(new Font("Arial", Font.PLAIN, 13));
            itemVistaCocina.addActionListener(e -> abrirVistaCocina());

            menuVista.add(itemVistaCocina);
            menuBar.add(menuVista);
        }

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
                                "📋 DESCRIPCIÓN DEL SISTEMA:\n" +
                                "───────────────────────────────────────────────\n" +
                                "Sistema completo de punto de venta para restaurantes\n" +
                                "que permite gestionar pedidos de mesas, control de\n" +
                                "inventario, visualización de pedidos en cocina y\n" +
                                "generación de reportes de ventas con tres niveles\n" +
                                "de acceso diferenciados por roles de usuario.\n\n" +
                                "Versión: 1.0\n" +
                                "Base de datos: MySQL\n" +
                                "Lenguaje: Java Swing\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "👥 ROLES Y PERMISOS:\n" +
                                "───────────────────────────────────────────────\n\n" +
                                "🔹 MOZO:\n" +
                                "  • Ver y gestionar solo sus mesas asignadas\n" +
                                "  • Agregar productos al pedido\n" +
                                "  • Editar observaciones de productos\n" +
                                "  • Eliminar productos del pedido\n" +
                                "  • Cerrar mesa e imprimir ticket\n" +
                                "  • Sin acceso a: Cocina, reportes, gestión BD\n\n" +
                                "🔹 COCINA:\n" +
                                "  • Ver todos los pedidos activos ordenados\n" +
                                "  • Ver detalle de productos con observaciones\n" +
                                "  • Ver nombre del mozo asignado\n" +
                                "  • Reloj en tiempo real\n" +
                                "  • Sin acceso a: Editar pedidos, gestión mesas\n\n" +
                                "🔹 ADMIN:\n" +
                                "  • Acceso total al sistema\n" +
                                "  • Gestión completa de mesas y pedidos\n" +
                                "  • CRUD de productos (Crear/Editar/Eliminar)\n" +
                                "  • Gestión de usuarios\n" +
                                "  • Vista de cocina con volver al panel\n" +
                                "  • Reportes diarios y mensuales\n" +
                                "  • Control de inventario y stock\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "🍽️ GESTIÓN DE MESAS:\n" +
                                "───────────────────────────────────────────────\n" +
                                "  • Click simple: Abrir ventana de pedido\n" +
                                "  • Doble click: Ver resumen rápido\n" +
                                "  • 🟢 Verde: Mesa disponible\n" +
                                "  • 🔴 Rojo: Mesa con pedido activo\n" +
                                "  • Asignación automática de mozo\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "🛒 TOMAR PEDIDOS:\n" +
                                "───────────────────────────────────────────────\n" +
                                "  • Filtrar primero por categoría (Comida/Bebida)\n" +
                                "  • Filtrar por tipo según categoría seleccionada\n" +
                                "  • Búsqueda rápida por nombre de producto\n" +
                                "  • Doble click en producto para agregar\n" +
                                "  • Editar cantidad y observaciones en tabla\n" +
                                "  • Calcular total automático\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "📦 GESTIÓN DE PRODUCTOS (Solo Admin):\n" +
                                "───────────────────────────────────────────────\n" +
                                "  • Alta de nuevos productos\n" +
                                "  • Modificación de nombre, precio, stock\n" +
                                "  • Baja con confirmación de seguridad\n" +
                                "  • Control de inventario en tiempo real\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "🖨️ IMPRESIÓN:\n" +
                                "───────────────────────────────────────────────\n" +
                                "  • Ticket de mesa con detalle completo\n" +
                                "  • Descuento automático de stock al imprimir\n" +
                                "  • Impresión de resúmenes diarios\n" +
                                "  • Impresión de resúmenes mensuales\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "📊 REPORTES (Solo Admin):\n" +
                                "───────────────────────────────────────────────\n" +
                                "  • Resumen del Día: Total + impresión + reinicio\n" +
                                "  • Resumen del Mes: Total + impresión + reinicio\n" +
                                "  • Ambos muestran fecha y total de ventas\n\n" +
                                "───────────────────────────────────────────────\n" +
                                "⚠️  IMPORTANTE:\n" +
                                "───────────────────────────────────────────────\n" +
                                "  ⚡ Los resúmenes BORRAN todos los pedidos\n" +
                                "  ⚡ SIEMPRE imprimir antes de cerrar resumen\n" +
                                "  ⚡ El stock se descuenta al imprimir ticket\n" +
                                "  ⚡ Los cambios en BD requieren nivel Admin\n" +
                                "  ⚡ Cocina solo visualiza, no modifica\n" +
                                "───────────────────────────────────────────────\n\n" +
                                "Desarrollado por: Sistema Los Troncos\n" +
                                "Soporte: info@lostroncos.com"
                );
                // Configuración del área de texto para mostrar el contenido de "Acerca de"
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

        // Agregar menús a la barra (solo los que se crearon según el rol)
        menuBar.add(menuArchivo);
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

            // Obtener la fecha de hoy
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaHoy = new Date();
            String fecha = sdf.format(fechaHoy);
            String fechaDB = sdfDB.format(fechaHoy);

            // Consultar resumenes diarios de hoy
            String sqlDia = "SELECT mesa, total, productos FROM resumenes_diarios WHERE fecha = ? ORDER BY mesa";
            PreparedStatement ps = con.prepareStatement(sqlDia);
            ps.setString(1, fechaDB);
            ResultSet rs = ps.executeQuery();

            // Mensaje del resumen
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

            // Botón para cerrar el día
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

                // Si confirma, guardar en resumenes_mensuales y borrar resumenes_diarios
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

                        // preparar y ejecutar la inserción de resumen mensual en la base de datos
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

                        // Borrar resumenes_diarios de hoy, reiniciarlos
                        String sqlDelete = "DELETE FROM resumenes_diarios WHERE fecha = ?";
                        PreparedStatement psDelete = con.prepareStatement(sqlDelete);
                        psDelete.setString(1, fechaDB);
                        int filasEliminadas = psDelete.executeUpdate();
                        psDelete.close();

                        con.close();

                        // Mostrar mensaje de éxito
                        JOptionPane.showMessageDialog(this,
                                "Día cerrado correctamente.\n" +
                                        "Total guardado: $" + String.format("%.2f", totalFinal) + "\n" +
                                        filasEliminadas + " tickets eliminados.",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Cerrar la ventana del resumen
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

            // Agregar botones al panel
            panelBotones.add(btnImprimir);
            panelBotones.add(btnCerrarDia);
            panel.add(panelBotones, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, panel,
                    "Resumen del Día", JOptionPane.INFORMATION_MESSAGE);

            // Cerrar conexión
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

    // Metodo para imprimir el resumen del día
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

        // Asignar el Printable personalizado
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
        // Atributos
        private String fecha;
        private List<String> datosMesas;
        private int mesasOcupadas;
        private double totalGeneral;

        // Constructor
        public ResumenDiaPrintable(String fecha, List<String> datosMesas, int mesasOcupadas, double totalGeneral) {
            this.fecha = fecha;
            this.datosMesas = datosMesas;
            this.mesasOcupadas = mesasOcupadas;
            this.totalGeneral = totalGeneral;
        }

        // Método para imprimir
        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE; // Solo una página
            }

            // Configurar el contexto gráfico
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

            // Línea separadora antes del total general
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === TOTAL GENERAL ===
            g2d.setFont(fTotal);
            String textoTotal = "TOTAL GENERAL:";
            String valorTotal = String.format("$%.2f", totalGeneral);

            g2d.drawString(textoTotal, margen, y);
            y += lineHeight;

            // Alinear valor total al centro
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

        // Si no confirma, salir
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

        // Si no confirma, salir
        if (confirmacion2 != JOptionPane.YES_OPTION) {
            return;
        }

        // Generar resumen mensual
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

            // Encabezado del resumen
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

                // Sí es una nueva mesa, mostrar encabezado
                if (mesaActual != mesa) {
                    resumen.append(String.format("MESA %d:\n", mesa));
                    mesaActual = mesa;
                }

                // Agregar línea del producto
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

            // mostrar total general
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

            // agregar scroll al textArea
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

            // Mensaje de éxito
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

    // Metodo para imprimir el resumen mensual
    private void mostrarResumenMes() {
        JFrame frame = new JFrame("Resumen del Mes");
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Selector de mes y año
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // ComboBox de meses
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        JComboBox<String> comboMes = new JComboBox<>(meses);

        // comboBox de años (últimos 4 años)
        JComboBox<Integer> comboAnio = new JComboBox<>();
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = anioActual; i >= anioActual - 3; i--) {
            comboAnio.addItem(i);
        }

        // Seleccionar mes actual por defecto
        Calendar cal = Calendar.getInstance();
        comboMes.setSelectedIndex(cal.get(Calendar.MONTH));

        JButton btnBuscar = new JButton("🔍 Buscar");

        // Agregar componentes al panel superior
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

        // agregar scroll al textArea
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelInferior = new JPanel(new BorderLayout());
        JLabel lblTotal = new JLabel("TOTAL DEL MES: $0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal.setHorizontalAlignment(SwingConstants.CENTER);

        // agregar botones al panel inferior
        JPanel panelBotonesInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnImprimirMes = new JButton("🖨️ Imprimir Resumen");
        btnImprimirMes.setBackground(new Color(33, 150, 243));
        btnImprimirMes.setForeground(Color.WHITE);
        btnImprimirMes.setEnabled(false);

        // agregar botón cerrar mes
        JButton btnCerrarMes = new JButton("💾 Cerrar Mes");
        btnCerrarMes.setBackground(new Color(244, 67, 54));
        btnCerrarMes.setForeground(Color.WHITE);
        btnCerrarMes.setEnabled(false);

        panelBotonesInferior.add(btnImprimirMes);
        panelBotonesInferior.add(btnCerrarMes);

        // agregar al panel inferior
        panelInferior.add(lblTotal, BorderLayout.CENTER);
        panelInferior.add(panelBotonesInferior, BorderLayout.SOUTH);
        panel.add(panelInferior, BorderLayout.SOUTH);

        // Agregar panel al frame
        btnBuscar.addActionListener(e -> {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

                int mesSeleccionado = comboMes.getSelectedIndex() + 1;
                int anioSeleccionado = (Integer) comboAnio.getSelectedItem();

                String sql = "SELECT fecha, total_dia, mesas_atendidas FROM resumenes_mensuales " +
                        "WHERE mes = ? AND anio = ? ORDER BY fecha";

                // Ejecutar consulta
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, mesSeleccionado);
                ps.setInt(2, anioSeleccionado);
                ResultSet rs = ps.executeQuery();

                // Construir el resumen
                StringBuilder resumen = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                // Encabezado
                resumen.append("═══════════════════════════════════════\n");
                resumen.append("    RESUMEN MENSUAL\n");
                resumen.append("    ").append(comboMes.getSelectedItem()).append(" ").append(anioSeleccionado).append("\n"); // mes y año
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

                // Mostrar el resumen en el textArea
                textArea.setText(resumen.toString());
                lblTotal.setText(String.format("TOTAL DEL MES: $%.2f", totalMes));

                // botones habilitados si hay ventas
                btnCerrarMes.setEnabled(diasConVentas > 0);
                btnImprimirMes.setEnabled(diasConVentas > 0);

                // Variables finales para los listeners
                final double totalMesFinal = totalMes;
                final int mesSeleccionadoFinal = mesSeleccionado;
                final int anioSeleccionadoFinal = anioSeleccionado;
                final int diasConVentasFinal = diasConVentas;
                final int totalMesasAtendidasFinal = totalMesasAtendidas;
                final String mesAnioTexto = comboMes.getSelectedItem() + " " + anioSeleccionado;

                // Listener para el botón de imprimir
                btnImprimirMes.addActionListener(ev -> {
                    try {
                        // Obtener los datos del mes para imprimir
                        Connection conImpresion = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
                        // Consulta para obtener los datos del mes
                        String sqlImpresion = "SELECT fecha, total_dia, mesas_atendidas FROM resumenes_mensuales " +
                                "WHERE mes = ? AND anio = ? ORDER BY fecha";

                        // Ejecutar consulta
                        PreparedStatement psImpresion = conImpresion.prepareStatement(sqlImpresion);
                        psImpresion.setInt(1, mesSeleccionadoFinal);
                        psImpresion.setInt(2, anioSeleccionadoFinal);
                        ResultSet rsImpresion = psImpresion.executeQuery();

                        // Construir las líneas para imprimir
                        List<String> lineasResumen = new ArrayList<>();
                        SimpleDateFormat sdfImpresion = new SimpleDateFormat("dd/MM/yyyy");

                        while (rsImpresion.next()) {
                            Date fecha = rsImpresion.getDate("fecha");
                            double totalDia = rsImpresion.getDouble("total_dia");
                            int mesasAtendidas = rsImpresion.getInt("mesas_atendidas");

                            lineasResumen.add(String.format("%s - $%.2f (%d mesas)",
                                    sdfImpresion.format(fecha), totalDia, mesasAtendidas));
                        }

                        rsImpresion.close();
                        psImpresion.close();
                        conImpresion.close();

                        // Llamar al metodo de impresión
                        imprimirResumenMensual(mesAnioTexto, lineasResumen, totalMesFinal,
                                diasConVentasFinal, totalMesasAtendidasFinal);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame,
                                "Error al imprimir: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

                // Listener para el botón de cerrar mes
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

    // Metodo para imprimir el resumen mensual
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

        // Diálogo de impresión
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

    // Sobrecarga para imprimir resumen mensual desde la ventana de consulta
    private void imprimirResumenMensual(String mesAnio, List<String> lineasResumen, double totalMes,
                                        int diasConVentas, int totalMesasAtendidas) {
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

        job.setPrintable(new ResumenMensualSimplePrintable(mesAnio, lineasResumen, totalMes,
                diasConVentas, totalMesasAtendidas), pf);

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(null,
                        "Resumen mensual impreso correctamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error al imprimir: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Clase interna para imprimir el resumen mensual
    private class ResumenMensualPrintable implements Printable {
        // Atributos
        private String mesAnio;
        private List<DatosMesa> datosMesas;
        private double totalGeneral;

        // Constructor
        public ResumenMensualPrintable(String mesAnio, List<DatosMesa> datosMesas, double totalGeneral) {
            this.mesAnio = mesAnio;
            this.datosMesas = datosMesas;
            this.totalGeneral = totalGeneral;
        }

        // Metodo para imprimir
        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            // Configurar el contexto gráfico
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

            // Alinear valor total al centro
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

            // Mensaje de vuelva pronto
            String vuelva = "Vuelva pronto!";
            int anchoVuelva = g2d.getFontMetrics().stringWidth(vuelva);
            g2d.drawString(vuelva, (anchoTicket - anchoVuelva) / 2, y);

            return PAGE_EXISTS;
        }
    }

    // Clase interna para imprimir el resumen mensual (versión simplificada para consulta)
    private class ResumenMensualSimplePrintable implements Printable {
        // Atributos
        private String mesAnio;
        private List<String> lineasResumen;
        private double totalMes;
        private int diasConVentas;
        private int totalMesasAtendidas;

        // Constructor
        public ResumenMensualSimplePrintable(String mesAnio, List<String> lineasResumen,
            double totalMes, int diasConVentas, int totalMesasAtendidas) {
            this.mesAnio = mesAnio;
            this.lineasResumen = lineasResumen;
            this.totalMes = totalMes;
            this.diasConVentas = diasConVentas;
            this.totalMesasAtendidas = totalMesasAtendidas;
        }

        // Metodo para imprimir
        @Override
        public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
            if (page > 0) {
                return NO_SUCH_PAGE;
            }

            // Configurar el contexto gráfico
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            // Fuentes optimizadas para 48mm
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
            y += lineHeight + 2;

            // === LISTADO DE DÍAS ===
            g2d.setFont(fPequena);
            for (String linea : lineasResumen) {
                // Si la línea es muy larga, dividirla
                if (linea.length() > 26) {
                    // Primera parte
                    g2d.drawString(linea.substring(0, 26), margen, y);
                    y += lineHeight - 1;
                    // Segunda parte si existe
                    if (linea.length() > 26) {
                        String resto = linea.substring(26);
                        if (resto.length() > 26) {
                            resto = resto.substring(0, 23) + "...";
                        }
                        g2d.drawString(resto, margen, y);
                        y += lineHeight - 1;
                    }
                } else {
                    g2d.drawString(linea, margen, y);
                    y += lineHeight - 1;
                }
            }

            y += 3;

            // Línea separadora
            g2d.setFont(fNormal);
            g2d.drawString("--------------------------", margen, y);
            y += lineHeight;

            // === ESTADÍSTICAS ===
            g2d.setFont(fPequena);
            g2d.drawString("Dias con ventas: " + diasConVentas, margen, y);
            y += lineHeight;
            g2d.drawString("Total mesas: " + totalMesasAtendidas, margen, y);
            y += lineHeight;

            double promedio = diasConVentas > 0 ? totalMes / diasConVentas : 0;
            g2d.drawString(String.format("Promedio: $%.2f", promedio), margen, y);
            y += lineHeight + 2;

            // Línea separadora antes del total
            g2d.setFont(fNormal);
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === TOTAL MENSUAL ===
            g2d.setFont(fTotal);
            String textoTotal = "TOTAL DEL MES:";
            g2d.drawString(textoTotal, margen, y);
            y += lineHeight;

            String valorTotal = String.format("$%.2f", totalMes);
            int anchoValorTotal = g2d.getFontMetrics().stringWidth(valorTotal);
            g2d.drawString(valorTotal, (anchoTicket - anchoValorTotal) / 2, y);
            y += lineHeight + 5;

            // Línea separadora final
            g2d.setFont(fNormal);
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === PIE DE PÁGINA ===
            g2d.setFont(fPequena);
            String gracias = "Gracias";
            int anchoGracias = g2d.getFontMetrics().stringWidth(gracias);
            g2d.drawString(gracias, (anchoTicket - anchoGracias) / 2, y);

            return PAGE_EXISTS;
        }
    }

    // Actualiza el estado visual de la mesa (color y contadores)
    public void actualizarEstadoMesa(int numeroMesa) {
        if (numeroMesa < 1 || numeroMesa > mesas.length) return;

        boolean tienePedido = ModeloPedidos.tienePedido(numeroMesa);

        // Actualizar color y propiedad del botón
        JButton btn = mesas[numeroMesa - 1];
        if (tienePedido) {
            btn.setBackground(Color.RED);
            btn.putClientProperty("estado", "ocupada");
        } else {
            btn.setBackground(Color.GREEN);
            btn.putClientProperty("estado", "libre");
        }

        // --- añadido para mostrar hora del primer pedido ---
        try {
            java.time.LocalDateTime hora = ModeloPedidos.getHoraPrimerPedido(numeroMesa);
            if (hora != null) {
                // 'formato' debe ser un DateTimeFormatter definido en la clase (ej: "HH:mm")
                btn.setText("<html><center>Mesa " + numeroMesa + "<br>" + hora.format(formato) + "</center></html>");

            } else {
                // si no hay hora registrada mostramos el texto simple
                btn.setText("Mesa " + numeroMesa);
            }
        } catch (Exception ex) {
            // si por alguna razón no existe el metodo en ModeloPedidos o falla,
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

    // Cerrar sesión del usuario actual
    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SesionUsuario.getInstancia().cerrarSesion();
            dispose();
            new Login().setVisible(true);
        }
    }

    // Abrir gestión de usuarios (solo ADMIN)
    private void abrirGestionUsuarios() {
        if (!SesionUsuario.getInstancia().tienePermiso("GESTIONAR_USUARIOS")) {
            JOptionPane.showMessageDialog(this,
                    "No tiene permisos para acceder a esta función",
                    "Acceso Denegado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        GestionUsuarios ventana = new GestionUsuarios(this);
        ventana.setVisible(true);
    }

    // Abrir gestión de productos (solo ADMIN)
    private void abrirGestionProductos() {
        if (!SesionUsuario.getInstancia().tienePermiso("GESTIONAR_PRODUCTOS")) {
            JOptionPane.showMessageDialog(this,
                    "No tiene permisos para acceder a esta función",
                    "Acceso Denegado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "La gestión de productos se realiza desde VentanaPedido\n" +
                "con los botones: + Nuevo, ✎ Editar, − Eliminar",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Abrir vista de cocina (solo ADMIN)
    private void abrirVistaCocina() {
        if (!SesionUsuario.getInstancia().tienePermiso("VER_COCINA")) {
            JOptionPane.showMessageDialog(this,
                    "No tiene permisos para acceder a esta función",
                    "Acceso Denegado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        VistaCocina cocina = new VistaCocina();
        cocina.setVisible(true);
    }
}
