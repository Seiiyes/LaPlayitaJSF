package model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class DetalleReabastecimiento {

    private int idDetalle; // Opcional, si la tabla detalle tiene su propio PK
    private int idReabastecimiento;
    
    private Producto producto;
    private int cantidad;
    private BigDecimal costoUnitario;
    
    private transient String uuid; // Para identificar objetos nuevos en la vista

    // Constructor
    public DetalleReabastecimiento() {
        this.uuid = UUID.randomUUID().toString(); // Generar UUID para cada nueva instancia
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
    
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // Método para calcular el subtotal
    public BigDecimal getSubtotal() {
        if (costoUnitario != null && cantidad > 0) {
            return costoUnitario.multiply(new BigDecimal(cantidad));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleReabastecimiento that = (DetalleReabastecimiento) o;
        if (idDetalle != 0 && that.idDetalle != 0) {
            return idDetalle == that.idDetalle;
        }
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        if (idDetalle != 0) {
            return Objects.hash(idDetalle);
        }
        return Objects.hash(uuid);
    }
}
