package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo que representa la cabecera de una operación de reabastecimiento (maestro).
 */
public class Reabastecimiento {

    private int idReabastecimiento;
    private Date fecha;
    private EstadoReabastecimiento estado; // Corresponde a 'estadoCompra' en la BD
    private String observaciones;
    private BigDecimal costoTotal;
    private String formaPago; // Añadido para consistencia con la BD
    private java.sql.Time hora; // Añadido para consistencia con la BD
    
    private Proveedor proveedor;
    private List<DetalleReabastecimiento> detalles;

    public Reabastecimiento() {
        this.proveedor = new Proveedor();
        this.detalles = new ArrayList<>();
        this.fecha = new Date();
        this.estado = EstadoReabastecimiento.RECIBIDO; // Usar el enum
        this.costoTotal = BigDecimal.ZERO;
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

    public EstadoReabastecimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoReabastecimiento estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public List<DetalleReabastecimiento> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleReabastecimiento> detalles) {
        this.detalles = detalles;
    }

    public BigDecimal getCostoTotal() {
        // Si la lista de detalles está poblada (ej. en el diálogo de edición),
        // calcula el total dinámicamente para reflejar los cambios en tiempo real.
        if (this.detalles != null && !this.detalles.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (DetalleReabastecimiento det : detalles) {
                if (det.getSubtotal() != null) {
                    total = total.add(det.getSubtotal());
                }
            }
            return total;
        }
        // Si no, devuelve el valor almacenado (ej. para la vista de tabla principal).
        // Esto evita que se muestre 0 cuando los detalles no han sido cargados.
        return this.costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public java.sql.Time getHora() {
        return hora;
    }

    public void setHora(java.sql.Time hora) {
        this.hora = hora;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }
}
