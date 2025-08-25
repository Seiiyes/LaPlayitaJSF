package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Usuario implements Serializable {
    private int id;
    private String documento;
    private String pNombre;
    private String sNombre;
    private String pApellido;
    private String sApellido;
    private String correo;
    private String telefono;
    private String contrasenaHash; // BCrypt
    private int estadoUsuario;     // FK a estado_usuario (1=activo)
    private Timestamp fechaCreacion;
    private Timestamp ultimoLogin;
    private int idRol;             // FK a tbl_roles

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getpNombre() { return pNombre; }
    public void setpNombre(String pNombre) { this.pNombre = pNombre; }

    public String getsNombre() { return sNombre; }
    public void setsNombre(String sNombre) { this.sNombre = sNombre; }

    public String getpApellido() { return pApellido; }
    public void setpApellido(String pApellido) { this.pApellido = pApellido; }

    public String getsApellido() { return sApellido; }
    public void setsApellido(String sApellido) { this.sApellido = sApellido; }

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
}
