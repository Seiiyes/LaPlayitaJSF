package service;

import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.Producto;

@ApplicationScoped
public class ProductoService {

    @Inject
    private ProductoDAO productoDAO;

    /**
     * Devuelve todos los productos de la base de datos.
     */
    public List<Producto> obtenerTodos() {
        try {
            return productoDAO.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener la lista de productos", e);
        }
    }

    /**
     * Inserta un nuevo producto en la base de datos.
     */
    public void crearProducto(Producto producto) {
        try {
            productoDAO.insert(producto);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al insertar el producto", e);
        }
    }

    /**
     * Actualiza el stock de un producto específico.
     * @param idProducto ID del producto
     * @param cantidad   cantidad a sumar (si es negativa, resta stock)
     */
    public void actualizarStock(int idProducto, int cantidad) {
        try {
            productoDAO.actualizarStock(idProducto, cantidad);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar el stock del producto", e);
        }
    }

   
    public Producto buscarPorId(int idProducto) {
        try {
            return productoDAO.findById(idProducto);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar el producto con ID: " + idProducto, e);
        }
    }

}
