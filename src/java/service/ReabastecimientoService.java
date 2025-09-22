package service;

import dao.ProductoDAO;
import dao.ProveedorDAO;
import dao.ReabastecimientoDAO;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.DetalleReabastecimiento;
import model.EstadoReabastecimiento; // Importar el enum
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;

@ApplicationScoped
public class ReabastecimientoService {

    @Inject
    private ReabastecimientoDAO reabastecimientoDAO;
    @Inject
    private ProveedorDAO proveedorDAO;
    @Inject
    private ProductoDAO productoDAO;

    public List<Reabastecimiento> listar() throws SQLException {
        return reabastecimientoDAO.findAllMasters();
    }

    public Reabastecimiento obtenerCompleto(int idReabastecimiento) throws SQLException {
        Reabastecimiento reab = reabastecimientoDAO.findMasterById(idReabastecimiento);
        if (reab != null) {
            List<DetalleReabastecimiento> detalles = reabastecimientoDAO.findDetailsByReabastecimientoId(idReabastecimiento);
            reab.setDetalles(detalles);
        }
        return reab;
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedorDAO.findAll();
    }

    public List<Producto> listarProductos() throws SQLException {
        return productoDAO.findAll();
    }

    public void registrarProducto(Producto producto) throws SQLException {
        // Aquí se podrían añadir validaciones de negocio para el producto
        productoDAO.insert(producto);
    }

    public void guardarMaestroDetalle(Reabastecimiento reab) throws SQLException, IllegalArgumentException {
        // Validaciones de negocio
        if (reab.getProveedor() == null || reab.getProveedor().getIdProveedor() == 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor.");
        }
        if (reab.getDetalles() == null || reab.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe tener al menos un producto.");
        }

        // Lógica de actualización de stock
        actualizarStock(reab);

        reabastecimientoDAO.saveMaestroDetalle(reab);
    }

    private void actualizarStock(Reabastecimiento reab) throws SQLException {
        boolean esNuevo = reab.getIdReabastecimiento() == 0;

        if (esNuevo) {
            // Si es nuevo y se recibe, sumar todo al stock
            if (reab.getEstado() == EstadoReabastecimiento.RECIBIDO) {
                for (DetalleReabastecimiento det : reab.getDetalles()) {
                    productoDAO.actualizarStock(det.getProducto().getIdProducto(), det.getCantidad());
                }
            }
        } else {
            // Si es una edición, la lógica es más compleja
            Reabastecimiento estadoAnterior = obtenerCompleto(reab.getIdReabastecimiento());
            
            // Caso 1: Se cancela una compra que estaba recibida -> Revertir stock
            if (reab.getEstado() == EstadoReabastecimiento.CANCELADO && estadoAnterior.getEstado() == EstadoReabastecimiento.RECIBIDO) {
                for (DetalleReabastecimiento det : estadoAnterior.getDetalles()) {
                    productoDAO.actualizarStock(det.getProducto().getIdProducto(), -det.getCantidad());
                }
            }
            // Caso 2: Se recibe una compra que estaba como pedida -> Sumar stock
            else if (reab.getEstado() == EstadoReabastecimiento.RECIBIDO && estadoAnterior.getEstado() != EstadoReabastecimiento.RECIBIDO) {
                 for (DetalleReabastecimiento det : reab.getDetalles()) {
                    productoDAO.actualizarStock(det.getProducto().getIdProducto(), det.getCantidad());
                }
            }
            // Caso 3: Se edita una compra ya recibida -> Ajustar diferencias
            else if (reab.getEstado() == EstadoReabastecimiento.RECIBIDO && estadoAnterior.getEstado() == EstadoReabastecimiento.RECIBIDO) {
                // Lógica de ajuste fino (simplificada por ahora): revertir lo viejo, aplicar lo nuevo
                for (DetalleReabastecimiento detAntiguo : estadoAnterior.getDetalles()) {
                     productoDAO.actualizarStock(detAntiguo.getProducto().getIdProducto(), -detAntiguo.getCantidad());
                }
                for (DetalleReabastecimiento detNuevo : reab.getDetalles()) {
                     productoDAO.actualizarStock(detNuevo.getProducto().getIdProducto(), detNuevo.getCantidad());
                }
            }
        }
    }

    public void eliminar(int idReabastecimiento) throws SQLException {
        Reabastecimiento reab = obtenerCompleto(idReabastecimiento);
        if (reab != null && reab.getEstado() == EstadoReabastecimiento.RECIBIDO) {
            // Revertir el stock de todos los productos de la compra
            for (DetalleReabastecimiento det : reab.getDetalles()) {
                productoDAO.actualizarStock(det.getProducto().getIdProducto(), -det.getCantidad());
            }
        }
        reabastecimientoDAO.delete(idReabastecimiento);
    }
}
