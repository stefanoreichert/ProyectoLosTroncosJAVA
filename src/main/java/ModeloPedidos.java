import java.util.ArrayList; // lista dinámica
import java.util.List; // interfaz de lista
import java.util.concurrent.ConcurrentHashMap; // mapa seguro para multihilos
import java.sql.*; // conexión y consultas a base de datos
import java.time.LocalDateTime; // fecha y hora moderna
import java.util.Map; // estructura clave-valor



// Clase para gestionar todos los pedidos de todas las mesas en memoria
public class ModeloPedidos {
    // Mapa concurrente para manejar pedidos por mesa
    private static final ConcurrentHashMap<Integer, List<ItemPedido>> pedidosPorMesa = new ConcurrentHashMap<>(); // Mapa de mesa -> lista de items de pedido
    private static final Map<Integer, LocalDateTime> horaPrimerPedido = new ConcurrentHashMap<>();

    // Inicializar el mapa con listas vacías para cada mesa (asumimos 40)
    static {
        for (int i = 1; i <= 40; i++) {
            pedidosPorMesa.put(i, new ArrayList<>());
        }
    }

    // Retorna la lista de pedidos para una mesa específica
    public static List<ItemPedido> getPedidoMesa(int numeroMesa) {
        return pedidosPorMesa.get(numeroMesa);
    }

    // Agrega o actualiza un item al pedido de una mesa.
    public static void agregarOActualizarItem(int numeroMesa, ItemPedido newItem) {
        List<ItemPedido> pedido = pedidosPorMesa.get(numeroMesa);
        if (pedido == null) {
            pedido = new ArrayList<>();
            pedidosPorMesa.put(numeroMesa, pedido);
        }

        // si la lista estaba vacía antes de agregar, vamos a registrar la hora
        boolean estabaVacio = pedido.isEmpty();

        // Buscar si el producto ya existe (por ID)
        for (ItemPedido item : pedido) {
            if (item.getIdProducto() == newItem.getIdProducto()) {
                item.setCantidad(item.getCantidad() + newItem.getCantidad());
                return;
            }
        }

        // Si no existe, lo agrega
        pedido.add(newItem);

        // Si estaba vacía y ahora agregamos el primer item, registramos la hora
        if (estabaVacio) {
            registrarPrimerPedido(numeroMesa);
        }
    }

    // Elimina todos los pedidos de una mesa
    public static void borrarPedido(int numeroMesa) {
        pedidosPorMesa.put(numeroMesa, new ArrayList<>());
        // removemos la hora registrada de la mesa
        horaPrimerPedido.remove(numeroMesa);
    }

    // Elimina todos los pedidos de una mesa (específico para el botón "Borrar Pedido")
    public static void borrarPedidoMesa(int numeroMesa) {
        pedidosPorMesa.put(numeroMesa, new ArrayList<>());
        horaPrimerPedido.remove(numeroMesa);
    }
    // Verifica si la mesa tiene algún pedido pendiente en la BASE DE DATOS (para cambiar el color del botón)
    public static boolean tienePedido(int numeroMesa) {
        try {
            // Conexión a la base de datos
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            String sql = "SELECT COUNT(*) as total FROM `mesa pedido` WHERE mesa = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numeroMesa);
            ResultSet rs = ps.executeQuery();

            boolean tiene = false;
            if (rs.next()) {
                tiene = rs.getInt("total") > 0;
            }

            rs.close();
            ps.close();
            con.close();

            return tiene;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retorna el total del pedido de la mesa
    public static double getTotalMesa(int numeroMesa) {
        List<ItemPedido> pedido = pedidosPorMesa.get(numeroMesa);
        if (pedido == null || pedido.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (ItemPedido item : pedido) {
            total += item.getSubtotal();
        }
        return total;
    }

    // Agregar este metodo a la clase ModeloPedidos
    public static List<ItemPedido> cargarPedidoDesdeBD(int numeroMesa) {
        // Crear una lista para almacenar los items del pedido
        List<ItemPedido> pedido = new ArrayList<>();

        // Conectar a la base de datos y cargar los items del pedido
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

            System.out.println("DEBUG: Consultando mesa " + numeroMesa);

            String sql = "SELECT mp.producto_id, mp.cantidad, p.nombre, p.precio " +
                    "FROM `mesa pedido` mp " +
                    "INNER JOIN productos p ON mp.producto_id = p.id " +
                    "WHERE mp.mesa = ?";

            // Ejecutar la query
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numeroMesa);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int idProducto = rs.getInt("producto_id");
                int cantidad = rs.getInt("cantidad");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");

                ItemPedido item = new ItemPedido(idProducto, nombre, precio, cantidad);
                pedido.add(item);

                System.out.println("DEBUG BD: " + nombre + " - $" + precio + " x " + cantidad);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            System.out.println("ERROR al cargar pedido:");
            e.printStackTrace();
        }

        return pedido;
    }

    // Registra la hora del primer pedido de una mesa en memoria
    public static void registrarPrimerPedido(int numeroMesa) {
        // Si ya está registrada, no hacemos nada
        horaPrimerPedido.computeIfAbsent(numeroMesa, k -> LocalDateTime.now());
    }

    // Obtiene la hora del primer pedido de una mesa desde la BD
    public static LocalDateTime getHoraPrimerPedido(int numeroMesa) {
        // Conectar a la base de datos y obtener la hora del primer pedido
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");
            String sql = "SELECT MIN(hora_pedido) as primera_hora FROM `mesa pedido` WHERE mesa = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, numeroMesa);
            ResultSet rs = ps.executeQuery();

            LocalDateTime hora = null;
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("primera_hora");
                if (ts != null) {
                    hora = ts.toLocalDateTime();
                }
            }

            rs.close();
            ps.close();
            con.close();

            return hora;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
