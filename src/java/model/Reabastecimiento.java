package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Modelo que representa un registro de reabastecimiento.
 * Funciona como un DTO (Data Transfer Object) que combina información de las
 * tablas 'reabastecimiento', 'detallereabastecimiento', 'proveedor' y 'producto'
 * para facilitar su manejo en la vista.
 */
public class Reabastecimiento {

    private int idReabastecimiento;
    private Date fecha;
    private String estado; // Corresponde a 'estadoCompra' en la BD
    private String observaciones;
    private BigDecimal costoTotal; // Calculado o almacenado

    // Objetos relacionados para mostrar información en la tabla
    private Proveedor proveedor;
    private Producto producto;

    // Campos del detalle
    private int cantidad;
    private BigDecimal costoUnitario;

    // IDs para el formulario
    private int idProveedor;
    private int idProducto;

    public Reabastecimiento() {
        // Inicializar objetos para evitar NullPointerException en el formulario
        this.proveedor = new Proveedor();
        this.producto = new Producto();
        this.fecha = new Date(); // Pre-rellenar con la fecha actual
        this.estado = "Recibido"; // Valor por defecto
    }

    // Getters y Setters

    public int getIdReabastecimiento() {
        return idReabastecimiento;
    }

    public void setIdReabastecimiento(int idReabastecimiento) {
        this.idReabastecimiento = idReabastecimiento;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public BigDecimal getCostoTotal() {
        if (this.cantidad > 0 && this.costoUnitario != null) {
            return costoUnitario.multiply(new BigDecimal(cantidad));
        }
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }
}