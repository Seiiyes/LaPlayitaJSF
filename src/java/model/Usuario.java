package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Usuario implements Serializable {
    private int idUsuario;
    private String documento;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String contrasenaHash; // BCrypt
    private int estadoUsuario;     // FK a estadoUsuario (1=activo)
    private Timestamp fechaCreacion;
    private Timestamp ultimoLogin;
    private int idRol;             // FK a rol

    // NUEVO: referencia al objeto Rol
    private Rol rol;

    // Constructor por defecto
    public Usuario() {
    }

    // Constructor de copia para clonar objetos de forma segura
    public Usuario(Usuario original) {
        this.idUsuario = original.idUsuario;
        this.documento = original.documento;
        this.nombres = original.nombres;
        this.apellidos = original.apellidos;
        this.correo = original.correo;
        this.telefono = original.telefono;
        this.contrasenaHash = original.contrasenaHash;
        this.estadoUsuario = original.estadoUsuario;
        this.idRol = original.idRol;
        this.rol = original.rol; // Copia también el objeto anidado
    }

    // Getters y setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // alias conveniente
    public int getId() { return idUsuario; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public int getEstadoUsuario() { return estadoUsuario; }
    public void setEstadoUsuario(int estadoUsuario) { this.estadoUsuario = estadoUsuario; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Timestamp getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(Timestamp ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    // NUEVO: getter/setter para el objeto Rol
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", documento='" + documento + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", correo='" + correo + '\'' +
                ", estadoUsuario=" + estadoUsuario +
                ", idRol=" + idRol +
                '}';
    }
}
