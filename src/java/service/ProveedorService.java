package service;

import dao.ProveedorDAO;
import java.io.Serializable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.Proveedor;
import javax.enterprise.inject.spi.CDI;

@ApplicationScoped
public class ProveedorService implements Serializable {

    @Inject
    private transient ProveedorDAO proveedorDAO;

    public void guardar(Proveedor proveedor) throws SQLException, IllegalArgumentException {
        // Validaciones de negocio básicas
        if (proveedor.getNombres() == null || proveedor.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio.");
        }
        if (proveedor.getTelefono() == null || proveedor.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono del proveedor es obligatorio.");
        }
        if (proveedor.getCorreo() == null || proveedor.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo del proveedor es obligatorio.");
        }
        if (proveedor.getDireccion() == null || proveedor.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección del proveedor es obligatoria.");
        }

        // Persistencia
        proveedorDAO.save(proveedor);
    }

    public List<Proveedor> listarTodos() throws SQLException {
        return proveedorDAO.findAll();
    }

    public Proveedor buscarPorId(int id) throws SQLException {
        return proveedorDAO.findById(id);
    }

    public void eliminar(int id) throws SQLException {
        proveedorDAO.delete(id);
    }

    public void actualizar(Proveedor proveedor) throws SQLException {
        if (proveedor.getIdProveedor() == null) {
            throw new IllegalArgumentException("El ID del proveedor es obligatorio para actualizar.");
        }
        proveedorDAO.save(proveedor);
    }

    // Re-inyección al deserializar
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.proveedorDAO = CDI.current().select(ProveedorDAO.class).get();
    }
}
