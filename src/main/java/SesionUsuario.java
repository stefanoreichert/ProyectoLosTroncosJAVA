public class SesionUsuario {
    private static SesionUsuario instancia;
    private Usuario usuarioActual;

    // Constructor privado para singleton
    private SesionUsuario() {
        // Constructor privado para singleton
    }

    // Metodo para obtener la instancia única
    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    // Métodos para manejar la sesión del usuario
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    // Metodo para verificar permisos de usuario, según su rol
    public boolean tienePermiso(String permiso) {
        if (usuarioActual == null) return false;

        switch (permiso.toUpperCase()) { // Normalizar a mayúsculas para evitar problemas del case
            case "VER_REPORTES":
            case "GESTIONAR_PRODUCTOS":
            case "GESTIONAR_USUARIOS":
            case "CERRAR_DIA":
            case "CERRAR_MES":
                return usuarioActual.esAdmin();

            case "VER_MESAS":
            case "TOMAR_PEDIDOS":
            case "CERRAR_MESA":
                return usuarioActual.esAdmin() || usuarioActual.esMozo();

            case "VER_COCINA":
                return usuarioActual.esAdmin() || usuarioActual.esCocina();

            default:
                return false;
        }
    }
}

