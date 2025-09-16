package service;

import dao.ProductoDAO;
import dao.ProveedorDAO;
import dao.ReabastecimientoDAO;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;

@ApplicationScoped
public class ReabastecimientoService {

    // Inyectamos los DAOs necesarios. Asegúrate de que también sean beans de CDI.
    @Inject
    private ReabastecimientoDAO reabastecimientoDAO;
    @Inject
    private ProveedorDAO proveedorDAO;
    @Inject
    private ProductoDAO productoDAO;

    public List<Reabastecimiento> listar() throws SQLException {
        return reabastecimientoDAO.findAll();
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedorDAO.findAll();
    }

    public List<Producto> listarProductos() throws SQLException {
        return productoDAO.findAll();
    }

    public void guardar(Reabastecimiento reab) throws SQLException, IllegalArgumentException {
        // Validaciones de negocio
        if (reab.getIdProveedor() == 0 || reab.getIdProducto() == 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor y un producto.");
        }
        if (reab.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (reab.getCostoUnitario() == null || reab.getCostoUnitario().doubleValue() < 0) {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo.");
        }

        // Lógica de negocio: Actualizar stock si el estado es "Recibido"
        // Esta lógica es más compleja en una actualización (calcular diferencia),
        // pero para una inserción es directa.
        boolean esNuevo = reab.getIdReabastecimiento() == 0;
        if (esNuevo && "Recibido".equals(reab.getEstado())) {
            productoDAO.actualizarStock(reab.getIdProducto(), reab.getCantidad());
        }
        
        // TODO: Implementar lógica de actualización de stock para ediciones.
        // Por ejemplo, si se cambia la cantidad o el estado de un reabastecimiento existente.

        reabastecimientoDAO.save(reab);
    }

    public void eliminar(int idReabastecimiento) throws SQLException {
        // TODO: Implementar lógica para revertir el stock si se elimina un
        // reabastecimiento que ya había sido "Recibido".
        // Esto requiere obtener los datos del reabastecimiento antes de borrarlo.
        
        reabastecimientoDAO.delete(idReabastecimiento);
    }
}