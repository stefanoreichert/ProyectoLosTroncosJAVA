import javax.swing.*; // componentes gráficos
import javax.swing.table.DefaultTableModel; // modelo de tabla
import javax.swing.table.DefaultTableCellRenderer; // diseño celdas tabla
import javax.swing.event.TableModelEvent; // evento cambio tabla
import javax.swing.event.TableModelListener; // escucha cambios tabla
import java.awt.*; // colores y fuentes
import java.awt.event.*; // eventos de botones, mouse, teclas
import java.awt.print.*; // imprimir
import java.sql.*; // base de datos
import java.text.SimpleDateFormat; // formatear fechas
import java.util.List; // lista
import java.time.LocalDateTime; // fecha y hora actual
import java.time.format.DateTimeFormatter; // formato fecha/hora
import java.util.Date; // fecha clásica
import static java.awt.print.Printable.PAGE_EXISTS; // impresión

// clase que representa la ventana de gestión de pedidos para una mesa específica
public class VentanaPedido extends JDialog {
    // Atributos de la clase
    private int numeroMesa;
    private MenuPrincipal menuPrincipal;
    private Connection conexion;

    // Modelos y tablas para mostrar datos
    private DefaultTableModel modeloTablaPedido;
    private JTable tablaPedido;
    private DefaultTableModel modeloTablaProductos;
    private JTable tablaProductos;

    // Controles de la interfaz gráfica de usuario.
    private JTextField txtBusquedaRapida;
    private JLabel lblResultadoBusqueda;
    private JComboBox<ComboItem> cmbTipo;
    private JComboBox<ComboItem> cmbCategoria;
    private JSpinner spCantidad;
    private JLabel lblTotal;
    private JButton btnImprimirTicket;
    private JButton btnCerrarMesa;
    private JButton btnNuevoProducto;
    private JButton btnEditarProducto;
    private JButton btnEliminarProducto;

    // Constructor: se ejecuta cuando se crea una nueva ventana de pedido
    public VentanaPedido(MenuPrincipal parent, int mesa) {
        super(parent, "Mesa " + mesa + " - Gestión de Pedido", true); // modal
        this.menuPrincipal = parent;
        this.numeroMesa = mesa;

        // Inicializar componentes y cargar datos
        initComponents();

        // Cargar primero las categorías y luego los tipos según la categoría seleccionada
        cargarCategorias();
        ComboItem selCat = (ComboItem) cmbCategoria.getSelectedItem();
        int idCatInicial = selCat == null ? 0 : selCat.getValue();
        cargarTipos(idCatInicial);

        // Cargar productos y pedido de la mesa
        cargarProductosDisponibles("", 0, 0);
        cargarPedidoMesa();
        actualizarTotal();
    }

    // Configura el tamaño y diseño básico de la ventana de pedido
    private void initComponents() {
        setSize(1400, 800);
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Cerrar al salir

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // crear panel principal
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(crearPanelBusqueda(), BorderLayout.NORTH);
        mainPanel.add(crearPanelCentral(), BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    // Crea el panel superior con los filtros de búsqueda
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Título de la sección
        JLabel lblTitulo = new JLabel("MESA " + numeroMesa + " - BÚSQUEDA DE PRODUCTOS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 38));

        // Panel que contiene todos los controles de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setOpaque(false);

        // ComboBox de Categoría de producto
        JLabel lblCategoria = new JLabel("Categoría:");
        lblCategoria.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbCategoria = new JComboBox<ComboItem>();
        cmbCategoria.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbCategoria.setPreferredSize(new Dimension(150, 30));

        // ComboBox de Tipo de producto (dependiente de la categoría)
        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbTipo = new JComboBox<ComboItem>();
        cmbTipo.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbTipo.setPreferredSize(new Dimension(120, 30));

        // Campo de texto para buscar productos
        JLabel lblBuscar = new JLabel("Producto:");
        lblBuscar.setFont(new Font("Arial", Font.PLAIN, 14));

        // campo de texto para búsqueda rápida
        txtBusquedaRapida = new JTextField(20);
        txtBusquedaRapida.setFont(new Font("Arial", Font.PLAIN, 14));
        txtBusquedaRapida.setPreferredSize(new Dimension(200, 30));

        // Spinner para seleccionar cantidad
        JLabel lblCant = new JLabel("Cantidad:");
        lblCant.setFont(new Font("Arial", Font.PLAIN, 14));
        spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spCantidad.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spCantidad.getEditor();
        editor.getTextField().setColumns(4);

        // Botón para agregar producto
        JButton btnAgregarRapido = new JButton("Agregar");
        btnAgregarRapido.setFont(new Font("Arial", Font.BOLD, 14));

        // Etiqueta para mostrar resultado de búsqueda
        lblResultadoBusqueda = new JLabel("");
        lblResultadoBusqueda.setFont(new Font("Arial", Font.ITALIC, 12));
        lblResultadoBusqueda.setForeground(Color.BLUE);

        // Agregar en el orden: Categoría -> Tipo -> Producto -> Cantidad -> Botón
        panelBusqueda.add(lblCategoria);
        panelBusqueda.add(cmbCategoria);
        panelBusqueda.add(lblTipo);
        panelBusqueda.add(cmbTipo);
        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBusquedaRapida);
        panelBusqueda.add(lblCant);
        panelBusqueda.add(spCantidad);
        panelBusqueda.add(btnAgregarRapido);
        panelBusqueda.add(lblResultadoBusqueda);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelBusqueda, BorderLayout.CENTER);

        // Cuando se selecciona una categoría, recargar los tipos disponibles para esa categoría
        cmbCategoria.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ComboItem sel = (ComboItem) cmbCategoria.getSelectedItem(); // obtener lo seleccionado
                int idCat = sel == null ? 0 : sel.getValue(); // obtener id de categoría
                cargarTipos(idCat); // // carga el combo de tipos según la categoría elegida
                filtrarProductos(); // filtra la tabla de productos
            }
        });

        // Mantener listeners para filtrado y búsqueda rápida
        cmbTipo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) { // cuando cambia el ítem seleccionado
                if (e.getStateChange() == ItemEvent.SELECTED) { // si es seleccionado
                    filtrarProductos(); // filtra la tabla de productos
                }
            }
        });

        // Configurar búsqueda rápida con teclado y botón
        configurarBusquedaRapida(btnAgregarRapido);

        return panel;
    }

    // Configura el comportamiento de búsqueda rápida con teclado
    private void configurarBusquedaRapida(JButton btnAgregar) {

        // Evento cuando se presiona una tecla en el campo de búsqueda
        txtBusquedaRapida.addKeyListener(new KeyAdapter() { // "escucha" de teclado
            public void keyPressed(KeyEvent e) {
                // Flecha arriba: selecciona el producto encontrado
                if(e.getKeyCode() == KeyEvent.VK_UP) { // flecha arriba
                    buscarYSeleccionarProducto(); // buscar y seleccionar producto
                }
                // Enter: agrega el producto seleccionado
                else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    agregarProductoRapido();
                }
            }

            // Filtra productos al soltar una tecla (excepto ↑ y Enter)
            public void keyReleased(KeyEvent e) {
                // Al soltar cualquier otra tecla, filtra los productos(mientras se va escribiendo)
                if(e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_ENTER) {
                    filtrarProductos();
                }
            }
        });

        // Eventos cuando cambian los combobox
        cmbTipo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filtrarProductos();
            }
        });

        cmbCategoria.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filtrarProductos();
            }
        });

        // Evento del botón agregar
        btnAgregar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregarProductoRapido();
            }
        });
    }

    // Busca un producto en la tabla y lo selecciona
    private void buscarYSeleccionarProducto() {
        String busqueda = txtBusquedaRapida.getText().trim().toLowerCase();
        if(busqueda.isEmpty()) {
            return;
        }

        // Recorre todas las filas de la tabla de productos
        for(int i = 0; i < modeloTablaProductos.getRowCount(); i++) {
            String nombre = modeloTablaProductos.getValueAt(i, 1).toString().toLowerCase();
            if(nombre.contains(busqueda)) {
                // Si lo encuentra, selecciona esa fila
                tablaProductos.setRowSelectionInterval(i, i);
                tablaProductos.scrollRectToVisible(tablaProductos.getCellRect(i, 0, true));
                lblResultadoBusqueda.setText("✓ " + modeloTablaProductos.getValueAt(i, 1));
                return;
            }
        }
        lblResultadoBusqueda.setText("No encontrado");
    }

    // Agrega el producto que está seleccionado actualmente
    private void agregarProductoRapido() {
        int fila = tablaProductos.getSelectedRow();
        if(fila == -1) {
            JOptionPane.showMessageDialog(this, "Primero usa ↑ para seleccionar el producto",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        agregarProductoSeleccionado(fila);

        // Limpiar campos después de agregar
        txtBusquedaRapida.setText("");
        lblResultadoBusqueda.setText("");
        spCantidad.setValue(1);
        txtBusquedaRapida.requestFocus();
    }

    // Crea el panel central dividido en dos: productos y pedido
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.add(crearPanelProductos());
        panel.add(crearPanelPedido());
        return panel;
    }

    // Crea el panel izquierdo con la lista de productos disponibles
    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                "PRODUCTOS DISPONIBLES",
                0, 0, new Font("Arial", Font.BOLD, 14)
        ));

        // Crear modelo de tabla con 4 columnas para el listado de productos
        modeloTablaProductos = new DefaultTableModel(
                new Object[]{"ID", "Producto", "Stock", "Precio"}, 0) {

            // Las celdas no se pueden editar directamente
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            // Define el tipo de dato de cada columna
            public Class<?> getColumnClass(int columnIndex) {
                Class<?> tipoColumna; // Variable para almacenar el tipo de retorno

                if (columnIndex == 2) {
                    tipoColumna = Integer.class; // Columna Stock
                } else if (columnIndex == 3) {
                    tipoColumna = Double.class; // Columna Precio
                } else {
                    tipoColumna = super.getColumnClass(columnIndex); // Tipo por defecto
                }

                return tipoColumna; // Un solo punto de retorno
            }
        };

        tablaProductos = new JTable(modeloTablaProductos);
        configurarEstiloTabla(tablaProductos);

        // Configurar anchos de columnas
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(50);
        tablaProductos.getColumnModel().getColumn(2).setMaxWidth(70);
        tablaProductos.getColumnModel().getColumn(3).setMaxWidth(90);

        // Doble clic en una fila agrega el producto
        tablaProductos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    int fila = tablaProductos.getSelectedRow();
                    if(fila != -1) {
                        agregarProductoSeleccionado(fila);
                    }
                }
            }
        });

        JScrollPane scrollProductos = new JScrollPane(tablaProductos);

        // Panel con botones CRUD (Crear, Leer, Actualizar, Eliminar)
        JPanel panelCRUD = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelCRUD.setBackground(Color.WHITE);

        btnNuevoProducto = new JButton("+ Nuevo");
        btnEditarProducto = new JButton("✎ Editar");
        btnEliminarProducto = new JButton("− Eliminar");

        btnNuevoProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        btnEditarProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        btnEliminarProducto.setFont(new Font("Arial", Font.PLAIN, 12));

        // Solo mostrar botones de CRUD si el usuario es ADMIN
        Usuario usuarioActual = SesionUsuario.getInstancia().getUsuarioActual();
        if (usuarioActual != null && usuarioActual.esAdmin()) {
            panelCRUD.add(btnNuevoProducto);
            panelCRUD.add(btnEditarProducto);
            panelCRUD.add(btnEliminarProducto);
        }

        // Eventos de los botones
        btnNuevoProducto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirDialogoNuevoProducto();
            }
        });

        btnEditarProducto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirDialogoEditarProducto();
            }
        });

        btnEliminarProducto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eliminarProducto();
            }
        });

        panel.add(scrollProductos, BorderLayout.CENTER);
        panel.add(panelCRUD, BorderLayout.SOUTH);
        return panel;
    }

    private void descontarStockPedido(List<ItemPedido> pedido) throws SQLException {
        // Usamos try-with-resources para asegurar que la conexión se cierre automáticamente
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "")) {

            // La consulta SQL resta la cantidad pedida del stock actual para el producto.
            String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                // Recorrer cada ítem del pedido
                for (ItemPedido item : pedido) {
                    // 1. Establecer la cantidad a restar (getCantidad())
                    ps.setInt(1, item.getCantidad());
                    // 2. Establecer el ID del producto (getIdProducto())
                    ps.setInt(2, item.getIdProducto());

                    // Ejecutar la actualización para el ítem actual
                    ps.executeUpdate();
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
            // Relanzar la excepción para que pueda ser manejada por imprimirTicket()
            throw new SQLException("Error al actualizar stock: " + e.getMessage());
        }
    }

    private void imprimirTicket() {
        System.out.println(">>> INICIANDO IMPRESIÓN DE TICKET <<<");

        List<ItemPedido> pedido = ModeloPedidos.cargarPedidoDesdeBD(numeroMesa);

        for (ItemPedido item : pedido) {
            System.out.println("Producto: " + item.getNombreProducto() +
                    " - Precio: " + item.getPrecioUnitario() +
                    " - Cantidad: " + item.getCantidad() +
                    " - Subtotal: " + item.getSubtotal());
        }

        // Verificar stock antes de imprimir
        StringBuilder productosAgotados = new StringBuilder();
        boolean stockSuficiente = true;

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

            for (ItemPedido item : pedido) {
                String sql = "SELECT stock FROM productos WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, item.getIdProducto());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int stock = rs.getInt("stock");
                    if (stock < item.getCantidad()) {
                        stockSuficiente = false;
                        productosAgotados.append("- ").append(item.getNombreProducto())
                                .append(" (Solicitado: ").append(item.getCantidad())
                                .append(", Disponible: ").append(stock).append(")\n");
                    }
                }
                rs.close();
                ps.close();
            }

            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        if (!stockSuficiente) {
            JOptionPane.showMessageDialog(this,
                    "No hay stock suficiente para los siguientes productos:\n\n" + productosAgotados.toString(),
                    "Stock Insuficiente", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Imprimir ticket
        PrinterJob job = PrinterJob.getPrinterJob();

        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        double width = 48 * 2.834645669;
        double height = 842;
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable(new TicketPrintable(pedido), pf);

        if (job.printDialog()) {
            try {
                job.print();

                // ✅ GUARDAR EN resumenes_diarios ANTES DE BORRAR
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

                // Calcular total
                double totalMesa = 0;
                StringBuilder productosDetalle = new StringBuilder();
                for (ItemPedido item : pedido) {
                    totalMesa += item.getSubtotal();
                    productosDetalle.append(item.getNombreProducto())
                            .append(" x").append(item.getCantidad())
                            .append(" ($").append(item.getPrecioUnitario())
                            .append("); ");
                }

                // Insertar en resumenes_diarios
                SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");
                Date ahora = new Date();

                String sqlInsert = "INSERT INTO resumenes_diarios (fecha, hora, mesa, total, productos) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psInsert = con.prepareStatement(sqlInsert);
                psInsert.setString(1, sdfDB.format(ahora));
                psInsert.setString(2, sdfHora.format(ahora));
                psInsert.setInt(3, numeroMesa);
                psInsert.setDouble(4, totalMesa);
                psInsert.setString(5, productosDetalle.toString());
                psInsert.executeUpdate();
                psInsert.close();

                // Descontar stock
                descontarStockPedido(pedido);

                // Cerrar la mesa (borrar pedido de la BD)
                String sql = "DELETE FROM `mesa pedido` WHERE mesa = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, numeroMesa);
                ps.executeUpdate();
                ps.close();
                con.close();

                JOptionPane.showMessageDialog(this,
                        "Ticket impreso correctamente.\n" +
                                "Stock actualizado.\n" +
                                "Mesa cerrada.\n" +
                                "Guardado en resumen diario.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

                // Actualizar estado de la mesa en el menú principal
                menuPrincipal.actualizarEstadoMesa(numeroMesa);

                // Cerrar esta ventana
                dispose();

            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private class TicketPrintable implements Printable {
        private final List<ItemPedido> items;

        public TicketPrintable(List<ItemPedido> items) {
            this.items = items;
        }

        // Método que dibuja el contenido del ticket
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Fuentes optimizadas para 48mm
            Font fTitulo = new Font("Courier New", Font.BOLD, 10);
            Font fNormal = new Font("Courier New", Font.PLAIN, 8);
            Font fPequena = new Font("Courier New", Font.PLAIN, 7);
            Font fTotal = new Font("Courier New", Font.BOLD, 10);

            int y = 10;
            int lineHeight = 11;
            int margen = 3;
            int anchoTicket = (int) pageFormat.getImageableWidth();

            // === ENCABEZADO ===
            g2d.setFont(fTitulo);
            String titulo = "LOS TRONCOS RESTO BAR";
            int anchoTitulo = g2d.getFontMetrics().stringWidth(titulo);
            g2d.drawString(titulo, (anchoTicket - anchoTitulo) / 2, y);
            y += lineHeight;

            // Fecha y hora
            g2d.setFont(fPequena);
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
            g2d.drawString(fecha, margen, y);
            y += lineHeight - 2;

            // Número de mesa
            g2d.setFont(fNormal);
            g2d.drawString("Mesa: " + numeroMesa, margen, y);
            y += lineHeight;

            // Línea separadora
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === PRODUCTOS === AJUSTAR NOMBRES LARGOS PARA IMPRESORA 48 MM
            double total = 0;
            for (ItemPedido item : items) {
                String nombre = item.getNombreProducto();

                // Si el nombre es muy largo, dividirlo en líneas
                if (nombre.length() > 20) {
                    // Primera línea (hasta 20 caracteres)
                    g2d.drawString(nombre.substring(0, 20), margen, y);
                    y += lineHeight;
                    // Segunda línea (resto del nombre)
                    String resto = nombre.substring(20);
                    if (resto.length() > 20) {
                        resto = resto.substring(0, 17) + "...";
                    }
                    g2d.drawString(resto, margen, y);
                    y += lineHeight;
                } else {
                    g2d.drawString(nombre, margen, y);
                    y += lineHeight;
                }

                // Cantidad x Precio = Subtotal
                int cantidad = item.getCantidad();
                double precio = item.getPrecioUnitario();
                double subtotal = item.getSubtotal();

                String lineaCantidad = String.format("%d x $%.2f", cantidad, precio);
                String lineaSubtotal = String.format("$%.2f", subtotal);

                g2d.drawString(lineaCantidad, margen + 5, y);

                // Alinear subtotal a la derecha
                int anchoSubtotal = g2d.getFontMetrics().stringWidth(lineaSubtotal);
                g2d.drawString(lineaSubtotal, anchoTicket - anchoSubtotal - margen, y);

                y += lineHeight + 2;

                total += subtotal;
            }

            // Línea separadora antes del total
            y += 2;
            g2d.drawString("==========================", margen, y);
            y += lineHeight;

            // === TOTAL ===
            g2d.setFont(fTotal);
            String textoTotal = "TOTAL:";
            String valorTotal = String.format("$%.2f", total);

            g2d.drawString(textoTotal, margen, y);
            int anchoValorTotal = g2d.getFontMetrics().stringWidth(valorTotal);
            g2d.drawString(valorTotal, anchoTicket - anchoValorTotal - margen, y);
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

    // Crea el panel derecho con el pedido actual de la mesa
    private JPanel crearPanelPedido() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                "PEDIDO MESA " + numeroMesa,
                0, 0, new Font("Arial", Font.BOLD, 14)
        ));

        // Crear modelo de tabla para el pedido
        modeloTablaPedido = new DefaultTableModel(
                new Object[]{"ID", "Producto", "Cantidad", "Precio", "Subtotal"}, 0) {

            // Solo se pueden editar Cantidad (columna 2) y Precio (columna 3)
            public boolean isCellEditable(int r, int c) {
                return c == 2 || c == 3;
            }

            public Class<?> getColumnClass(int columnIndex) {
                Class<?> tipoColumna; // Variable para almacenar el tipo de retorno

                if (columnIndex == 2) {
                    tipoColumna = Integer.class; // Columna Cantidad
                } else if (columnIndex == 3 || columnIndex == 4) {
                    tipoColumna = Double.class; // Columnas Precio y Subtotal
                } else {
                    tipoColumna = super.getColumnClass(columnIndex); // Tipo por defecto
                }

                return tipoColumna; //
            }
        };

        // Crear tabla para mostrar el pedido
        tablaPedido = new JTable(modeloTablaPedido);
        configurarEstiloTabla(tablaPedido);
        tablaPedido.getColumnModel().getColumn(0).setMaxWidth(50);
        tablaPedido.getColumnModel().getColumn(2).setMaxWidth(70);
        tablaPedido.getColumnModel().getColumn(3).setMaxWidth(90);
        tablaPedido.getColumnModel().getColumn(4).setMaxWidth(100);

        // Listener que detecta cuando se modifica una celda de la tabla
        modeloTablaPedido.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                // Solo actuar cuando se actualiza una celda
                if (e.getType() == TableModelEvent.UPDATE) {
                    int fila = e.getFirstRow();
                    int columna = e.getColumn();

                    // Si se modificó cantidad o precio
                    if (columna == 2 || columna == 3) {
                        try {
                            int id = (int) modeloTablaPedido.getValueAt(fila, 0);
                            int cantidad = (int) modeloTablaPedido.getValueAt(fila, 2);
                            double precio = (double) modeloTablaPedido.getValueAt(fila, 3);

                            // Validaciones
                            if (cantidad <= 0) {
                                JOptionPane.showMessageDialog(VentanaPedido.this,
                                        "La cantidad debe ser mayor a 0", "Error", JOptionPane.ERROR_MESSAGE);
                                cargarPedidoMesa();
                                return;
                            }

                            if (precio < 0) {
                                JOptionPane.showMessageDialog(VentanaPedido.this,
                                        "El precio no puede ser negativo", "Error", JOptionPane.ERROR_MESSAGE);
                                cargarPedidoMesa();
                                return;
                            }

                            // Actualizar en la base de datos
                            actualizarItemPedido(id, cantidad, precio);

                            // Actualizar subtotal en la tabla
                            double subtotal = cantidad * precio;
                            modeloTablaPedido.setValueAt(subtotal, fila, 4);
                            actualizarTotal();

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(VentanaPedido.this,
                                    "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            cargarPedidoMesa();
                        }
                    }
                }
            }
        });

        // Agregar la tabla dentro de un JScrollPane
        JScrollPane scrollPedido = new JScrollPane(tablaPedido);

        // Panel inferior con total y botones
        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelTotal.setBackground(Color.WHITE);

        lblTotal = new JLabel("TOTAL: $0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));

        // NUEVO: Botón para eliminar item del pedido
        JButton btnEliminarItem = new JButton("🗑️ Eliminar Item");
        btnEliminarItem.setFont(new Font("Arial", Font.PLAIN, 14));
        btnEliminarItem.setBackground(new Color(255, 69, 0));
        btnEliminarItem.setForeground(Color.WHITE);
        btnEliminarItem.setFocusPainted(false);
        btnEliminarItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eliminarItemPedido();
            }
        });

        btnImprimirTicket = new JButton("Imprimir Ticket");
        btnCerrarMesa = new JButton("Cerrar Mesa");

        btnImprimirTicket.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCerrarMesa.setFont(new Font("Arial", Font.PLAIN, 14));

        panelTotal.add(lblTotal);
        panelTotal.add(btnEliminarItem);  // ← NUEVO BOTÓN
        panelTotal.add(btnImprimirTicket);
        panelTotal.add(btnCerrarMesa);

        // Eventos de los botones
        btnImprimirTicket.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imprimirTicket();
            }
        });

        btnCerrarMesa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cerrarMesa();
            }
        });

        panel.add(scrollPedido, BorderLayout.CENTER);
        panel.add(panelTotal, BorderLayout.SOUTH);

        return panel;
    }

    // Configura el estilo visual de una tabla (fuente, alto de fila, alineación)
    private void configurarEstiloTabla(JTable tabla) {
        tabla.setFont(new Font("Arial", Font.PLAIN, 14));
        tabla.setRowHeight(28);

        // Centrar el texto de todas las columnas
        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);
        for(int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centro);
        }
    }

    // Carga los tipos de productos desde la base de datos al ComboBox (filtrados por categoría)
    private void cargarTipos(int categoriaId) {
        cmbTipo.removeAllItems();
        cmbTipo.addItem(new ComboItem("Todos", 0));

        // Construir la consulta SQL
        try {
            Connection con = Conexion.GetConnection();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT DISTINCT t.id_tipo, t.nombre ");
            sql.append("FROM tipo t ");
            sql.append("JOIN productos p ON p.id_tipo = t.id_tipo ");
            if (categoriaId > 0) {
                sql.append("WHERE p.id_tipo_producto = ? ");
            }
            sql.append("ORDER BY t.nombre");

            // Ejecutar la consulta
            PreparedStatement ps = con.prepareStatement(sql.toString());
            if (categoriaId > 0) {
                ps.setInt(1, categoriaId);
            }

            // ejecutar la query
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int id = rs.getInt("id_tipo");
                cmbTipo.addItem(new ComboItem(nombre, id));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los tipos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        cmbTipo.setSelectedIndex(0);
    }

    // Sobrecarga para mantener compatibilidad con llamadas existentes
    private void cargarTipos() {
        cargarTipos(0);
    }

    // Carga las categorías de productos desde la base de datos al ComboBox
    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(new ComboItem("Todas", 0));

        // Conexión a la base de datos y consulta
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id_tipo_producto, nombre FROM `tipo producto`");

            // Agregar cada categoría al ComboBox
            while(rs.next()) {
                String nombre = rs.getString("nombre");
                int id = rs.getInt("id_tipo_producto");
                cmbCategoria.addItem(new ComboItem(nombre, id));
            }

            rs.close();
            st.close();
            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Filtra los productos según categoría, tipo y texto de búsqueda
    private void filtrarProductos() {
        ComboItem seleccionadoTipo = (ComboItem) cmbTipo.getSelectedItem();
        ComboItem seleccionadoCat = (ComboItem) cmbCategoria.getSelectedItem();

        if (seleccionadoTipo == null || seleccionadoCat == null) {
            return;
        }

        int idTipo = seleccionadoTipo.getValue();
        int idCategoria = seleccionadoCat.getValue();
        String texto = txtBusquedaRapida.getText().trim();

        modeloTablaProductos.setRowCount(0);

        // Construir la consulta SQL con parámetros
        StringBuilder sql = new StringBuilder("SELECT id, nombre, stock, precio FROM productos WHERE nombre LIKE ?");
        if (idTipo > 0) {
            sql.append(" AND id_tipo = ?");
        }
        if (idCategoria > 0) {
            sql.append(" AND id_tipo_producto = ?");  // ← CORREGIDO
        }

        // Ejecutar la consulta
        try {
            Connection con = Conexion.GetConnection();
            PreparedStatement ps = con.prepareStatement(sql.toString());

            int idx = 1;
            ps.setString(idx++, "%" + texto + "%");
            if (idTipo > 0) {
                ps.setInt(idx++, idTipo);
            }
            if (idCategoria > 0) {
                ps.setInt(idx++, idCategoria);
            }

            // ejecuta la query
            ResultSet rs = ps.executeQuery();

            // Agregar resultados a la tabla
            while (rs.next()) {
                Object[] fila = new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("stock"),
                        rs.getDouble("precio")
                };
                modeloTablaProductos.addRow(fila);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Carga productos disponibles con filtros simples (metodo alternativo)
    private void cargarProductosDisponibles(String filtro, int tipo, int categoria) {
        modeloTablaProductos.setRowCount(0);
        String sql = "SELECT id, nombre, stock, precio FROM productos WHERE nombre LIKE ?";
        if(tipo > 0) {
            sql += " AND tipo_id = " + tipo;
        }
        if(categoria > 0) {
            sql += " AND id_tipo_producto = " + categoria;
        }

        // conexión y ejecución de la consulta
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + filtro + "%");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                Object[] fila = new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("stock"),
                        rs.getDouble("precio")
                };
                modeloTablaProductos.addRow(fila);
            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Carga el pedido actual de la mesa desde la base de datos
    private void cargarPedidoMesa() {
        modeloTablaPedido.setRowCount(0);

        // Conexión y consulta
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            String sql = "SELECT p.id, p.nombre, mp.cantidad, mp.precio_unitario, p.precio as precio_original " +
                    "FROM `mesa pedido` mp " +
                    "JOIN productos p ON mp.producto_id = p.id " +
                    "WHERE mp.mesa = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numeroMesa);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int cant = rs.getInt("cantidad");
                // Usar precio_unitario si existe, sino el precio original del producto
                double precio = rs.getDouble("precio_unitario");
                if (precio == 0) {
                    precio = rs.getDouble("precio_original");
                }

                Object[] fila = new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        cant,
                        precio,
                        cant * precio // Subtotal
                };
                modeloTablaPedido.addRow(fila);
            }

            rs.close();
            ps.close();
            con.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Agrega un producto seleccionado al pedido de la mesa
    private void agregarProductoSeleccionado(int fila) {
        // Obtener datos del producto de la tabla
        int id = (int) modeloTablaProductos.getValueAt(fila, 0);
        String nombre = (String) modeloTablaProductos.getValueAt(fila, 1);
        int stock = (int) modeloTablaProductos.getValueAt(fila, 2);
        double precio = (double) modeloTablaProductos.getValueAt(fila, 3);
        int cantidad = (int) spCantidad.getValue();

        // Verificar que hay stock suficiente
        if(cantidad > stock) {
            JOptionPane.showMessageDialog(this,
                    "No hay suficiente stock disponible.\nStock actual: " + stock,
                    "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Agregar o actualizar el producto en el pedido de la mesa
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

            // Verificar si ya existe en el pedido
            String sqlCheck = "SELECT cantidad FROM `mesa pedido` WHERE mesa = ? AND producto_id = ?";
            PreparedStatement psCheck = con.prepareStatement(sqlCheck);
            psCheck.setInt(1, numeroMesa);
            psCheck.setInt(2, id);
            ResultSet rs = psCheck.executeQuery();

            int cantidadActual = 0;
            if (rs.next()) {
                cantidadActual = rs.getInt("cantidad");
            }

            // Verificar que la suma no exceda el stock
            if (cantidadActual + cantidad > stock) {
                JOptionPane.showMessageDialog(this,
                        "No se puede agregar. Ya tiene " + cantidadActual + " en el pedido.\nStock disponible: " + stock,
                        "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                rs.close();
                psCheck.close();
                con.close();
                return;
            }

            rs.close();
            psCheck.close();

            // Insertar o actualizar el pedido usando ON DUPLICATE KEY UPDATE
            String sqlInsert = "INSERT INTO `mesa pedido`(mesa, producto_id, cantidad, precio_unitario) VALUES(?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE cantidad = cantidad + ?, precio_unitario = ?";
            PreparedStatement ps = con.prepareStatement(sqlInsert);
            ps.setInt(1, numeroMesa);
            ps.setInt(2, id);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precio);
            ps.setInt(5, cantidad);
            ps.setDouble(6, precio);
            ps.executeUpdate();

            ps.close();
            con.close();

        } catch(Exception e) {
            e.printStackTrace();JOptionPane.showMessageDialog(this, "Error al agregar producto: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        cargarPedidoMesa();
        actualizarTotal();
        if (menuPrincipal != null) {
            menuPrincipal.actualizarEstadoMesa(numeroMesa);
        }
    }

    // Actualiza la cantidad y precio de un producto en el pedido
    private void actualizarItemPedido(int productoId, int cantidad, double precio) {
        // Conexión y actualización
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            String sql = "UPDATE `mesa pedido` SET cantidad = ?, precio_unitario = ? WHERE mesa = ? AND producto_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, cantidad);
            ps.setDouble(2, precio);
            ps.setInt(3, numeroMesa);
            ps.setInt(4, productoId);
            ps.executeUpdate();

            ps.close();
            con.close();

        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar: " + e.getMessage());
        }
    }

    // Calcula y actualiza el total del pedido sumando todos los subtotales
    private void actualizarTotal() {
        double total = 0;

        // Recorrer todas las filas de la tabla de pedido
        for(int i = 0; i < modeloTablaPedido.getRowCount(); i++) {
            int cantidad = (int) modeloTablaPedido.getValueAt(i, 2);
            double precio = (double) modeloTablaPedido.getValueAt(i, 3);
            total += cantidad * precio;
        }

        lblTotal.setText(String.format("TOTAL: $%.2f", total));
    }

    // Cierra la mesa y borra el pedido
    private void cerrarMesa() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Cerrar la mesa y borrar el pedido?",
                "Cerrar Mesa",
                JOptionPane.YES_NO_OPTION);

        // Si confirma el cierre, que borre el pedido
        if(opcion == JOptionPane.YES_OPTION) {
            // Conexión y borrado
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
                String sql = "DELETE FROM `mesa pedido` WHERE mesa = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, numeroMesa);
                ps.executeUpdate();

                ps.close();
                con.close();

            } catch(Exception e) {
                e.printStackTrace();
            }

            cargarPedidoMesa();
            actualizarTotal();

            // ← AGREGAR ESTAS LÍNEAS AQUÍ
            if (menuPrincipal != null) {
                menuPrincipal.actualizarEstadoMesa(numeroMesa);
            }

            JOptionPane.showMessageDialog(this, "Mesa cerrada");
            dispose();
        }
    }

    // Abre diálogo para crear un nuevo producto
    private void abrirDialogoNuevoProducto() {
        // Crear panel con campos de entrada
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField txtNombre = new JTextField();
        JTextField txtStock = new JTextField();
        JTextField txtPrecio = new JTextField();

        JComboBox<ComboItem> cmbTipoNuevo = new JComboBox<ComboItem>();
        JComboBox<ComboItem> cmbCategoriaNuevo = new JComboBox<ComboItem>();

        // Cargar tipos de productos
        try {
            Connection con = Conexion.GetConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id_tipo, nombre FROM tipo");

            // Agregar cada tipo al ComboBox con un while
            while (rs.next()) {
                cmbTipoNuevo.addItem(new ComboItem(rs.getString("nombre"), rs.getInt("id_tipo")));
            }

            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cargar categorías
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT id_tipo_producto, nombre FROM `tipo producto`");

            while(rs.next()) {
                cmbCategoriaNuevo.addItem(new ComboItem(rs.getString("nombre"), rs.getInt("id_tipo_producto")));
            }

            rs.close();
            st.close();
            con.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Agregar componentes al panel
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Stock:"));
        panel.add(txtStock);
        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);
        panel.add(new JLabel("Tipo:"));
        panel.add(cmbTipoNuevo);
        panel.add(new JLabel("Categoría:"));
        panel.add(cmbCategoriaNuevo);

        int resultado = JOptionPane.showConfirmDialog(this, panel,
                "Agregar Nuevo Producto", JOptionPane.OK_CANCEL_OPTION);

        // Si presionó OK, validar y guardar
        if (resultado == JOptionPane.OK_OPTION) {
            try {
                // Obtener datos ingresados
                String nombre = txtNombre.getText().trim();
                int stock = Integer.parseInt(txtStock.getText().trim());
                double precio = Double.parseDouble(txtPrecio.getText().trim());

                // Obtener tipo y categoría seleccionados
                ComboItem tipoSel = (ComboItem) cmbTipoNuevo.getSelectedItem();
                ComboItem catSel = (ComboItem) cmbCategoriaNuevo.getSelectedItem();

                // Validaciones
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (stock < 0 || precio < 0) {
                    JOptionPane.showMessageDialog(this, "Stock y precio deben ser positivos",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Insertar en la base de datos
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
                String sql = "INSERT INTO productos (nombre, stock, precio, id_tipo, id_tipo_producto) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setInt(2, stock);
                ps.setDouble(3, precio);
                ps.setInt(4, tipoSel.getValue());
                ps.setInt(5, catSel.getValue());
                ps.executeUpdate();

                ps.close();
                con.close();

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(this, "Producto agregado correctamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                filtrarProductos();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Stock y Precio deben ser números válidos",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al agregar producto: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Abre diálogo para editar el producto seleccionado
    private void abrirDialogoEditarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if(fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para editar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener datos actuales del producto
        int id = (int) modeloTablaProductos.getValueAt(fila, 0);
        String nombreActual = (String) modeloTablaProductos.getValueAt(fila, 1);
        int stockActual = (int) modeloTablaProductos.getValueAt(fila, 2);
        double precioActual = (double) modeloTablaProductos.getValueAt(fila, 3);

        // Crear panel con campos de entrada
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField txtNombre = new JTextField(nombreActual);
        JTextField txtStock = new JTextField(String.valueOf(stockActual));
        JTextField txtPrecio = new JTextField(String.valueOf(precioActual));

        // Agregar componentes al panel
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Stock:"));
        panel.add(txtStock);
        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);

        int resultado = JOptionPane.showConfirmDialog(this, panel,
                "Editar Producto", JOptionPane.OK_CANCEL_OPTION);

        // Si presionó OK, validar y guardar
        if (resultado == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText().trim();
                int stock = Integer.parseInt(txtStock.getText().trim());
                double precio = Double.parseDouble(txtPrecio.getText().trim());

                // Validaciones
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (stock < 0 || precio < 0) {
                    JOptionPane.showMessageDialog(this, "Stock y precio deben ser positivos",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Actualizar en la base de datos
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
                String sql = "UPDATE productos SET nombre = ?, stock = ?, precio = ? WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setInt(2, stock);
                ps.setDouble(3, precio);
                ps.setInt(4, id);
                ps.executeUpdate();

                ps.close();
                con.close();

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                filtrarProductos();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Stock y Precio deben ser números válidos",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al actualizar producto: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Elimina el producto seleccionado con confirmación
    private void eliminarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if(fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modeloTablaProductos.getValueAt(fila, 0);
        String nombre = (String) modeloTablaProductos.getValueAt(fila, 1);

        // Primera confirmación
        int opcion1 = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar el producto?\n\n" + nombre,
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if(opcion1 == JOptionPane.YES_OPTION) {
            // Segunda confirmación
            int opcion2 = JOptionPane.showConfirmDialog(this,
                    "Esta acción no se puede deshacer.\n¿Realmente desea eliminar este producto?",
                    "Última Confirmación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);

            // si confirma, eliminar
            if(opcion2 == JOptionPane.YES_OPTION) {
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
                    String sql = "DELETE FROM productos WHERE id = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setInt(1, id);
                    ps.executeUpdate();

                    ps.close();
                    con.close();

                    JOptionPane.showMessageDialog(this, "Producto eliminado correctamente",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    filtrarProductos();

                } catch(Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al eliminar producto: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Clase interna para manejar items del ComboBox con texto y valor
    public static class ComboItem {
        private String key; // Texto que se muestra
        private int value; // Valor numérico (ID)

        public ComboItem(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        // Este metodo define qué se muestra en el ComboBox
        public String toString() {
            return key;
        }
    }

    // Elimina un item seleccionado del pedido, no de la base de datos
    private void eliminarItemPedido() {
        int fila = tablaPedido.getSelectedRow();

        // Verificar que se haya seleccionado una fila
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un producto del pedido para eliminar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Obtener datos del item
        int idProducto = (int) modeloTablaPedido.getValueAt(fila, 0);
        String nombreProducto = (String) modeloTablaPedido.getValueAt(fila, 1);
        int cantidad = (int) modeloTablaPedido.getValueAt(fila, 2);

        // Confirmar eliminación
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar del pedido?\n\n" + nombreProducto + " (x" + cantidad + ")",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Eliminar de la base de datos
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            String sql = "DELETE FROM `mesa pedido` WHERE mesa = ? AND producto_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numeroMesa);
            ps.setInt(2, idProducto);
            ps.executeUpdate();

            ps.close();
            con.close();

            // Recargar pedido y actualizar total
            cargarPedidoMesa();
            actualizarTotal();

            // Actualizar el color de la mesa en el menú principal
            menuPrincipal.actualizarEstadoMesa(numeroMesa);  // ← AGREGAR ESTA LÍNEA

            JOptionPane.showMessageDialog(this,
                    "Producto eliminado del pedido correctamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al eliminar el producto: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
