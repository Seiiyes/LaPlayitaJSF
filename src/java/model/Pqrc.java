package model;

import java.sql.Date;

public class Pqrc {

    private int idPqrc;
    private String tipo;
    private String descripcion;
    private Date fecha;
    private String estado;
    private int idCliente;
    private int idUsuario;
    
    private Cliente cliente;
    private Usuario usuario;

    public Pqrc() {
    }

    public Pqrc(int idPqrc, String tipo, String descripcion, Date fecha, String estado, int idCliente, int idUsuario) {
        this.idPqrc = idPqrc;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estado = estado;
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
    }

    public int getIdPqrc() {
        return idPqrc;
    }

    public void setIdPqrc(int idPqrc) {
        this.idPqrc = idPqrc;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
