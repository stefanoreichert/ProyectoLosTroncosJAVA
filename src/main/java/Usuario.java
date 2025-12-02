// Clase que representa un usuario del sistema con diferentes roles y permisos.
public class Usuario {
    private int id;
    private String nombreUsuario;
    private String nombre;
    private String contrasena;
    private String rol; // "ADMIN", "MOZO", "COCINA"

    // Constructor
    public Usuario(int id, String nombreUsuario, String nombre, String contrasena, String rol) { // Constructor
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters
    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombre() { return nombre; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public void setRol(String rol) { this.rol = rol; }

    // Métodos de verificación de permisos
    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rol);
    }

    // Metodo para verificar si el usuario es mozo
    public boolean esMozo() {
        return "MOZO".equalsIgnoreCase(rol);
    }

    // Metodo para verificar si el usuario es cocina
    public boolean esCocina() {
        return "COCINA".equalsIgnoreCase(rol);
    }

    // Metodo toString para representar al usuario
    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}

