package service;

import dao.ProveedorDAO;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.Proveedor;

@ApplicationScoped
public class ProveedorService {

    @Inject
    private ProveedorDAO proveedorDAO;

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

        // Llamada al DAO para persistir el proveedor
        proveedorDAO.save(proveedor);
    }
}
