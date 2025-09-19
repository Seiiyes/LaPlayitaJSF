package model;

import java.math.BigDecimal;

public class DetalleReabastecimiento {

    private int idDetalle; // Opcional, si la tabla detalle tiene su propio PK
    private int idReabastecimiento;
    
    private Producto producto;
    private int cantidad;
    private BigDecimal costoUnitario;

    // Constructor
    public DetalleReabastecimiento() {
        this.producto = new Producto(); // Evitar NullPointerException en la vista
    }

    // Getters y Setters

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdReabastecimiento() {
        return idReabastecimiento;
    }

    public void setIdReabastecimiento(int idReabastecimiento) {
        this.idReabastecimiento = idReabastecimiento;
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
    
    // Método para calcular el subtotal
    public BigDecimal getSubtotal() {
        if (costoUnitario != null && cantidad > 0) {
            return costoUnitario.multiply(new BigDecimal(cantidad));
        }
        return BigDecimal.ZERO;
    }
}
