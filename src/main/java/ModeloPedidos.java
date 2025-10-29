import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.*;

// Clase para gestionar todos los pedidos de todas las mesas en memoria
public class ModeloPedidos {
    private static final ConcurrentHashMap<Integer, List<ItemPedido>> pedidosPorMesa = new ConcurrentHashMap<>();

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


            System.out.println("DEBUG: agregarOActualizarItem mesa=" + numeroMesa + " producto=" + newItem.getIdProducto() + " cantidad=" + newItem.getCantidad());
            System.out.println("DEBUG: tienePedido despues add=" + ModeloPedidos.tienePedido(numeroMesa));

        }

        // Buscar si el producto ya existe (por ID)
        for (ItemPedido item : pedido) {
            if (item.getIdProducto() == newItem.getIdProducto()) {
                item.setCantidad(item.getCantidad() + newItem.getCantidad());
                return;
            }
        }

        // Si no existe, lo agrega
        pedido.add(newItem);
    }

    // Elimina un pedido completo de una mesa (ahora limpia la lista en vez de remover la key)
    public static void borrarPedido(int numeroMesa) {
        pedidosPorMesa.put(numeroMesa, new ArrayList<>());
    }

    // Elimina todos los pedidos de una mesa
    public static void borrarPedidoMesa(int numeroMesa) {
        pedidosPorMesa.put(numeroMesa, new ArrayList<>());


        System.out.println("DEBUG: borrarPedidoMesa mesa=" + numeroMesa);
        System.out.println("DEBUG: tienePedido despues borrar=" + ModeloPedidos.tienePedido(numeroMesa));

    }

    // Verifica si la mesa tiene algún pedido pendiente (para cambiar el color del botón)
// Verifica si la mesa tiene algún pedido pendiente en la BASE DE DATOS
    public static boolean tienePedido(int numeroMesa) {
        try {
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
    // Agregar este método a la clase ModeloPedidos
    public static List<ItemPedido> cargarPedidoDesdeBD(int numeroMesa) {
        List<ItemPedido> pedido = new ArrayList<>();

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/los_troncos", "root", "");

            System.out.println("DEBUG: Consultando mesa " + numeroMesa);

            String sql = "SELECT mp.producto_id, mp.cantidad, p.nombre, p.precio " +
                    "FROM `mesa pedido` mp " +
                    "INNER JOIN productos p ON mp.producto_id = p.id " +
                    "WHERE mp.mesa = ?";

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



}
