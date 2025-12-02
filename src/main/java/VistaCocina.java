// Importaciones necesarias para la interfaz gráfica
import javax.swing.*; // Componentes gráficos (ventanas, botones, paneles)
import java.awt.*; // Layouts, colores, fuentes
import java.awt.event.MouseAdapter; // Para detectar eventos del mouse
import java.awt.event.MouseEvent; // Para manejar cliks y doble cliks
import java.sql.*; // Para conectar con la base de datos MySQL
import java.text.SimpleDateFormat; // Para dar formato a fechas y horas
import java.util.Timer; // Para programar tareas repetitivas
import java.util.TimerTask; // Para definir las tareas a ejecutar
import java.util.*; // Utilidades generales (Date, ArrayList, etc.)
import java.util.List; // Para usar listas de datos


public class VistaCocina extends JFrame {
    private JPanel panelMesas; // Panel donde se muestran los botones de las mesas
    private Timer timer; // Timer para refrescar las mesas cada 5 segundos
    private Timer timerReloj; // Timer para actualizar el reloj cada segundo
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm"); // Formato hora para mesas (ej: 14:30)
    private SimpleDateFormat formatoReloj = new SimpleDateFormat("HH:mm:ss"); // Formato hora para reloj (ej: 14:30:45)
    private JLabel lblReloj; // Etiqueta que muestra la hora actual

    // Constructor
    public VistaCocina() {
        // Obtener el usuario que inició sesión
        Usuario usuario = SesionUsuario.getInstancia().getUsuarioActual(); // traer usuario actual

        // Configurar la ventana principal
        setTitle("Vista de Cocina - Los Troncos"); // Título de la ventana
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizar a pantalla completa
        setLocationRelativeTo(null); // Centrar en pantalla
        setLayout(new BorderLayout(10, 10)); // Layout con espaciado

        // ========== PANEL SUPERIOR (HEADER) ==========
        // Panel con fondo naranja que contiene título, reloj y botones
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(255, 152, 0)); // Color naranja
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Márgenes

        // Panel izquierdo: Título y subtítulo
        JPanel panelTitulo = new JPanel(new BorderLayout()); // Layout vertical
        panelTitulo.setOpaque(false); // Fondo transparente

        // Título principal
        JLabel lblTitulo = new JLabel("👨‍🍳 VISTA DE COCINA");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24)); // Fuente grande y negrita
        lblTitulo.setForeground(Color.WHITE); // Color blanco

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Pedidos por Orden de Llegada");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14)); // Fuente más pequeña
        lblSubtitulo.setForeground(new Color(255, 255, 255, 200)); // Blanco semi-transparente

        panelTitulo.add(lblTitulo, BorderLayout.NORTH); // Agregar título arriba
        panelTitulo.add(lblSubtitulo, BorderLayout.CENTER); // Agregar subtítulo abajo

        panelSuperior.add(panelTitulo, BorderLayout.WEST); // Agregar título a la izquierda

        // Panel central: Reloj grande en tiempo real
        JPanel panelCentroHeader = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Centrado
        panelCentroHeader.setOpaque(false); // Fondo transparente

        // Crea label para el reloj
        lblReloj = new JLabel();
        lblReloj.setFont(new Font("Digital-7", Font.BOLD, 48)); // Fuente grande para el reloj
        lblReloj.setForeground(Color.WHITE); // Color blanco
        actualizarReloj(); // Inicializar con la hora actual

        // Agregar reloj al panel central
        panelCentroHeader.add(lblReloj);
        panelSuperior.add(panelCentroHeader, BorderLayout.CENTER); // Agregar reloj al centro

        // Panel derecho: Usuario y botón (Volver o Salir según el rol)
        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelUsuario.setOpaque(false); // Fondo transparente

        // label con el nombre del usuario
        JLabel lblUsuario = new JLabel("Usuario: " + usuario.getNombre());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 16));
        lblUsuario.setForeground(Color.WHITE);

        JButton btnAccion;

        // Si el usuario es ADMIN: mostrar botón "Volver al Panel Principal"
        if (usuario.esAdmin()) {
            btnAccion = new JButton("⬅️ Volver al Panel");
            btnAccion.setFont(new Font("Arial", Font.BOLD, 14));
            btnAccion.setBackground(new Color(33, 150, 243));
            btnAccion.setForeground(Color.WHITE); // Texto blanco
            btnAccion.setFocusPainted(false); // Quitar borde al hacer click
            btnAccion.addActionListener(e -> {
                // Detener los timers para evitar errores
                if (timer != null) {
                    timer.cancel();
                }
                if (timerReloj != null) {
                    timerReloj.cancel();
                }
                // Solo cerrar esta ventana (el MenuPrincipal sigue abierto)
                dispose();
            });
        } else {
            // Si el usuario es COCINA: mostrar botón "Salir" (cerrar sesión)
            btnAccion = new JButton("🚪 Salir");
            btnAccion.setFont(new Font("Arial", Font.BOLD, 14));
            btnAccion.setBackground(new Color(244, 67, 54)); // Rojo
            btnAccion.setForeground(Color.WHITE);
            btnAccion.setFocusPainted(false);
            btnAccion.addActionListener(e -> {
                // Confirmar si quiere salir
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Está seguro que desea cerrar sesión?",
                        "Confirmar Salida",
                        JOptionPane.YES_NO_OPTION);

                // Si confirma, cerrar sesión y volver al login
                if (confirm == JOptionPane.YES_OPTION) {
                    // Detener timers
                    if (timer != null) {
                        timer.cancel();
                    }
                    if (timerReloj != null) {
                        timerReloj.cancel();
                    }

                    // Cerrar sesión del usuario
                    SesionUsuario.getInstancia().cerrarSesion();

                    // Cerrar ventana actual
                    dispose();

                    // Abrir Login nuevamente
                    SwingUtilities.invokeLater(() -> {
                        Login login = new Login();
                        login.setVisible(true);
                    });
                }
            });
        }

        // Agregar usuario y botón al panel derecho
        panelUsuario.add(lblUsuario);
        panelUsuario.add(Box.createHorizontalStrut(20)); // Espacio entre elementos
        panelUsuario.add(btnAccion);
        panelSuperior.add(panelUsuario, BorderLayout.EAST); // Agregar a la derecha

        add(panelSuperior, BorderLayout.NORTH); // Agregar panel superior a la ventana

        // ========== PANEL CENTRAL CON LAS MESAS ==========
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Márgenes

        // Panel donde se mostrarán los botones de las mesas
        panelMesas = new JPanel();
        panelMesas.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Alinear a la izquierda con espaciado
        panelMesas.setBackground(Color.WHITE);

        // Agregar barra de scroll para cuando hay muchas mesas
        JScrollPane scrollPane = new JScrollPane(panelMesas);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelCentral.add(scrollPane, BorderLayout.CENTER);

        // ========== PANEL INFERIOR CON BOTONES E INFO ==========
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelInferior.setBackground(new Color(245, 245, 245)); // Gris claro

        // Botón para refrescar manualmente las mesas
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setFont(new Font("Arial", Font.BOLD, 16));
        btnRefrescar.setPreferredSize(new Dimension(180, 45));
        btnRefrescar.setBackground(new Color(33, 150, 243)); // Azul
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.addActionListener(e -> cargarMesasConPedidos()); // Al hacer clik, recargar mesas

        // Etiqueta informativa
        JLabel lblInfo = new JLabel("🔔 Se actualiza automáticamente cada 5 segundos | Doble click para ver pedido");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 13));
        lblInfo.setForeground(Color.GRAY);

        // Agregar botón e info al panel inferior
        panelInferior.add(btnRefrescar);
        panelInferior.add(lblInfo);

        panelCentral.add(panelInferior, BorderLayout.SOUTH); // Agregar panel inferior
        add(panelCentral, BorderLayout.CENTER); // Agregar panel central a la ventana

        // Cargar mesas iniciales
        cargarMesasConPedidos();

        // Timer para actualización del reloj cada segundo
        timerReloj = new Timer();
        timerReloj.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> actualizarReloj()); // Actualizar en hilo de Swing
            }
        }, 1000, 1000); // Cada 1 segundo

        // Timer para actualización automática de mesas cada 5 segundos
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> cargarMesasConPedidos()); // Refrescar mesas en hilo de Swing
            }
        }, 5000, 5000); // Cada 5 segundos

        // Detener timers al cerrar la ventana (evita errores)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (timer != null) {
                    timer.cancel(); // Detener timer de mesas
                }
                if (timerReloj != null) {
                    timerReloj.cancel(); // Detener timer de reloj
                }
            }
        });
    }

    // Actualiza el reloj con la hora actual
    private void actualizarReloj() {
        String horaActual = formatoReloj.format(new java.util.Date()); // Obtener hora actual
        lblReloj.setText("🕐 " + horaActual); // Mostrar en label
    }

    // Carga las mesas que tienen pedidos activos, ordenadas por hora de llegada
    private void cargarMesasConPedidos() {
        panelMesas.removeAll(); // Limpiar panel para refrescar

        // Conectar a la base de datos y obtener mesas con pedidos
        try {
            Connection con = Conexion.GetConnection(); // Conectar a BD

            // Obtener mesas con pedidos, ordenadas por hora del primer pedido
            String sql = "SELECT DISTINCT mesa, MIN(hora_pedido) as primera_hora " +
                        "FROM `mesa pedido` " +
                        "GROUP BY mesa " +
                        "ORDER BY primera_hora ASC"; // Orden de llegada

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql); // Ejecutar consulta

            // Guardar mesas en una lista
            List<MesaInfo> mesas = new ArrayList<>();
            while (rs.next()) {
                int numeroMesa = rs.getInt("mesa");
                Timestamp hora = rs.getTimestamp("primera_hora");
                mesas.add(new MesaInfo(numeroMesa, hora)); // Agregar mesa a la lista
            }

            // Cerrar conexiones
            rs.close();
            st.close();
            con.close();

            // Si no hay mesas con pedidos
            if (mesas.isEmpty()) {
                JLabel lblVacio = new JLabel("No hay pedidos pendientes");
                lblVacio.setFont(new Font("Arial", Font.BOLD, 20));
                lblVacio.setForeground(Color.GRAY);
                panelMesas.add(lblVacio); // Mostrar mensaje
            } else {
                // Crear botones para cada mesa
                for (MesaInfo mesaInfo : mesas) {
                    JButton btnMesa = crearBotonMesa(mesaInfo); // Crear botón personalizado
                    panelMesas.add(btnMesa); // Agregar al panel
                }
            }


        } catch (Exception e) { // Si hay error al cargar mesas
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar mesas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar el panel para mostrar cambios
        panelMesas.revalidate();
        panelMesas.repaint();
    }

    // Crea un botón personalizado para cada mesa con su número y hora
    private JButton crearBotonMesa(MesaInfo mesaInfo) {
        // Formatear hora o mostrar "--:--" si no hay hora
        String horaTexto = mesaInfo.hora != null ? formatoHora.format(mesaInfo.hora) : "--:--";
        // Crear texto HTML para el botón (con estilos)
        String textoBoton = "<html><center><b style='font-size:18px'>Mesa " + mesaInfo.numeroMesa +
                           "</b><br><span style='font-size:14px'>⏰ " + horaTexto + "</span></center></html>";

        // Crear botón con estilos personalizados
        JButton btnMesa = new JButton(textoBoton);
        btnMesa.setPreferredSize(new Dimension(150, 100)); // Tamaño del botón
        btnMesa.setBackground(new Color(255, 87, 34)); // Naranja/Rojo
        btnMesa.setForeground(Color.WHITE);
        btnMesa.setFont(new Font("Arial", Font.BOLD, 14));
        btnMesa.setOpaque(true);
        btnMesa.setBorderPainted(true);
        btnMesa.setFocusPainted(false);

        // Doble clik para ver el pedido en modo solo lectura
        btnMesa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Si es doble click
                    verPedidoSoloLectura(mesaInfo.numeroMesa); // Abrir ventana con el pedido
                }
            }
        });

        return btnMesa;
    }

    // Muestra una ventana con el pedido completo de la mesa en modo solo lectura
    private void verPedidoSoloLectura(int numeroMesa) {
        try {
            Connection con = Conexion.GetConnection(); // Conectar a BD

            // Obtener todos los productos del pedido de la base de datos
            String sql = "SELECT p.nombre, mp.cantidad, mp.observacion " +
                        "FROM `mesa pedido` mp " +
                        "JOIN productos p ON mp.producto_id = p.id " +
                        "WHERE mp.mesa = ?";

            PreparedStatement ps = con.prepareStatement(sql); // Preparar consulta
            ps.setInt(1, numeroMesa); // Establecer número de mesa
            ResultSet rs = ps.executeQuery(); // Ejecutar consulta

            // Guardar items del pedido en una lista
            List<ItemPedidoCocina> items = new ArrayList<>();
            while (rs.next()) { // Recorrer todos los productos
                String producto = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                String obs = rs.getString("observacion");
                items.add(new ItemPedidoCocina(producto, cantidad, obs)); // Agregar a la lista
            }

            // Cerrar conexiones
            rs.close();
            ps.close();
            con.close();

            // Si no hay productos en el pedido, mostrar mensaje
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Esta mesa no tiene productos en el pedido",
                        "Mesa " + numeroMesa,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Abrir ventana personalizada con los productos
                VentanaPedidoCocina ventana = new VentanaPedidoCocina(this, numeroMesa, items);
                ventana.setVisible(true); // Mostrar ventana modal
            }

        } catch (Exception e) { // si hay error al cargar el pedido
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al cargar pedido: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clase auxiliar para almacenar items de pedido
    private class ItemPedidoCocina {
        String producto; // Nombre del producto
        int cantidad; // Cantidad pedida
        String observacion; // Observaciones especiales

        // constructor
        ItemPedidoCocina(String producto, int cantidad, String observacion) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.observacion = observacion;
        }
    }

    // Ventana personalizada para mostrar el pedido completo de una mesa
    private class VentanaPedidoCocina extends JDialog {
        public VentanaPedidoCocina(JFrame parent, int numeroMesa, List<ItemPedidoCocina> items) {
            super(parent, "Pedido Mesa " + numeroMesa, true); // Modal
            setSize(700, 600); // Tamaño de la ventana
            setLocationRelativeTo(parent); // Centrar sobre la ventana padre
            setLayout(new BorderLayout(0, 0));

            // ========== PANEL SUPERIOR CON NÚMERO DE MESA ==========
            JPanel panelHeader = new JPanel(new BorderLayout());
            panelHeader.setBackground(new Color(255, 87, 34)); // Naranja
            panelHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Márgenes

            // Títulos
            JLabel lblMesa = new JLabel("MESA " + numeroMesa);
            lblMesa.setFont(new Font("Arial", Font.BOLD, 36)); // Grande
            lblMesa.setForeground(Color.WHITE);
            lblMesa.setHorizontalAlignment(JLabel.CENTER);

            // Subtítulo
            JLabel lblSubtitulo = new JLabel("Pedido en cocina");
            lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 16));
            lblSubtitulo.setForeground(new Color(255, 255, 255, 200)); // Blanco semi-transparente
            lblSubtitulo.setHorizontalAlignment(JLabel.CENTER);

            // Panel para títulos
            JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 5));
            panelTitulos.setOpaque(false); // Fondo transparente
            panelTitulos.add(lblMesa);
            panelTitulos.add(lblSubtitulo);

            // Agregar títulos al header
            panelHeader.add(panelTitulos, BorderLayout.CENTER);
            add(panelHeader, BorderLayout.NORTH);

            // Panel central con los items
            JPanel panelItems = new JPanel();
            panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS)); // Vertical
            panelItems.setBackground(Color.WHITE);
            panelItems.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Agregar cada producto como un panel
            for (int i = 0; i < items.size(); i++) {
                ItemPedidoCocina item = items.get(i);
                JPanel itemPanel = crearPanelItem(item, i + 1); // Crear panel del item
                panelItems.add(itemPanel);
                panelItems.add(Box.createVerticalStrut(15)); // Espacio entre items
            }

            // Scroll para lista de items
            JScrollPane scroll = new JScrollPane(panelItems);
            scroll.setBorder(null);
            scroll.getVerticalScrollBar().setUnitIncrement(16); // Velocidad de scroll
            add(scroll, BorderLayout.CENTER);

            // Panel inferior con total y botón cerrar
            JPanel panelFooter = new JPanel(new BorderLayout());
            panelFooter.setBackground(new Color(245, 245, 245)); // Gris claro
            panelFooter.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            // Label con total de items
            JLabel lblTotal = new JLabel("Total de items: " + items.size());
            lblTotal.setFont(new Font("Arial", Font.BOLD, 18));
            lblTotal.setForeground(new Color(60, 60, 60));

            // Botón para cerrar la ventana
            JButton btnCerrar = new JButton("Cerrar");
            btnCerrar.setFont(new Font("Arial", Font.BOLD, 16));
            btnCerrar.setPreferredSize(new Dimension(120, 40));
            btnCerrar.setBackground(new Color(33, 150, 243)); // Azul
            btnCerrar.setForeground(Color.WHITE);
            btnCerrar.setFocusPainted(false);
            btnCerrar.addActionListener(e -> dispose()); // Cerrar al hacer click

            panelFooter.add(lblTotal, BorderLayout.WEST); // Total a la izquierda
            panelFooter.add(btnCerrar, BorderLayout.EAST); // Botón a la derecha
            add(panelFooter, BorderLayout.SOUTH); // Agregar footer abajo
        }

        // Crea un panel visual para cada item del pedido (producto)
        private JPanel crearPanelItem(ItemPedidoCocina item, int numero) {
            JPanel panel = new JPanel(new BorderLayout(15, 10));
            panel.setBackground(new Color(250, 250, 250)); // Gris muy claro
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 2), // Borde gris
                BorderFactory.createEmptyBorder(15, 15, 15, 15) // Márgenes internos
            ));

            // Número del item (lado izquierdo)
            JLabel lblNumero = new JLabel(numero + ".");
            lblNumero.setFont(new Font("Arial", Font.BOLD, 24));
            lblNumero.setForeground(new Color(255, 87, 34)); // Naranja
            lblNumero.setPreferredSize(new Dimension(40, 40));

            // Panel con producto y cantidad (lado derecho)
            JPanel panelInfo = new JPanel();
            panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS)); // Vertical
            panelInfo.setOpaque(false); // Fondo transparente

            // Nombre del producto
            JLabel lblProducto = new JLabel(item.producto);
            lblProducto.setFont(new Font("Arial", Font.BOLD, 20)); // Grande
            lblProducto.setForeground(new Color(40, 40, 40)); // Gris oscuro

            // Cantidad pedida
            JLabel lblCantidad = new JLabel("Cantidad: " + item.cantidad);
            lblCantidad.setFont(new Font("Arial", Font.PLAIN, 16));
            lblCantidad.setForeground(new Color(100, 100, 100)); // Gris

            // Agregar componentes al panel de info
            panelInfo.add(lblProducto);
            panelInfo.add(Box.createVerticalStrut(5)); // Espacio
            panelInfo.add(lblCantidad);

            // Observaciones (solo si existen)
            if (item.observacion != null && !item.observacion.trim().isEmpty()) {
                panelInfo.add(Box.createVerticalStrut(10)); // Espacio

                JPanel panelObs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Alinear a la izquierda
                panelObs.setOpaque(false); // Fondo transparente

                JLabel lblIcono = new JLabel("📝 ");
                lblIcono.setFont(new Font("Arial", Font.PLAIN, 16));

                // Área de texto para la observación (resaltada en naranja)
                JTextArea txtObs = new JTextArea(item.observacion);
                txtObs.setFont(new Font("Arial", Font.ITALIC, 15)); // Cursiva
                txtObs.setForeground(new Color(255, 87, 34)); // Naranja
                txtObs.setBackground(new Color(255, 243, 224)); // Fondo naranja claro
                txtObs.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Márgenes
                txtObs.setEditable(false); // No editable (solo lectura)
                txtObs.setLineWrap(true); // Ajustar texto
                txtObs.setWrapStyleWord(true); // Ajustar por palabras
                txtObs.setRows(2); // Altura mínima

                panelInfo.add(txtObs);
            }

            // Agregar componentes al panel principal
            panel.add(lblNumero, BorderLayout.WEST); // Número a la izquierda
            panel.add(panelInfo, BorderLayout.CENTER); // Info al centro

            return panel;
        }
    }

    // Clase auxiliar para almacenar información de mesa con su hora
    private class MesaInfo {
        int numeroMesa; // Número de la mesa
        Timestamp hora; // Hora del primer pedido

        MesaInfo(int numeroMesa, Timestamp hora) {
            this.numeroMesa = numeroMesa;
            this.hora = hora;
        }
    }
}


