package service;

import dao.ProductoDAO;
import java.sql.SQLException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import model.Producto;

@ApplicationScoped
public class ProductoService implements java.io.Serializable {

    @Inject
    private transient ProductoDAO productoDAO;

    /**
     * Devuelve todos los productos con su stock actual calculado.
     * @return Lista de productos con el campo stockActual poblado.
     */
    public List<Producto> obtenerTodosConStock() {
        try {
            return productoDAO.findAllWithStock();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener la lista de productos con stock", e);
        }
    }

    public Producto findById(int idProducto) {
        try {
            return productoDAO.findById(idProducto);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar producto por ID", e);
        }
    }

    /**
     * Inserta un nuevo producto y su stock inicial.
     * @param producto El objeto producto a insertar.
     * @param stockInicial La cantidad de stock inicial.
     */
    public void crearProducto(Producto producto, int stockInicial) {
        try {
            // Aquí se pueden añadir validaciones de negocio adicionales
            if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del producto es obligatorio.");
            }
            productoDAO.insert(producto, stockInicial);
        } catch (SQLException e) {
            e.printStackTrace();
            // Lanza una excepción más específica si es una violación de constraint
            if (e.getSQLState().startsWith("23")) {
                 throw new RuntimeException("Ya existe un producto con ese nombre.", e);
            }
            throw new RuntimeException("Error al insertar el producto", e);
        }
    }

    /**
     * Registra un nuevo movimiento de inventario (ENTRADA o SALIDA).
     * @param idProducto El ID del producto a afectar.
     * @param cantidad La cantidad a mover (siempre un número positivo).
     * @param tipoMovimiento El tipo de movimiento: "ENTRADA" o "SALIDA".
     * @param descripcion Motivo del movimiento (ej. "Venta a cliente #123").
     */
    public void registrarMovimientoInventario(int idProducto, int cantidad, String tipoMovimiento, String descripcion) {
        try {
            if (!"ENTRADA".equals(tipoMovimiento) && !"SALIDA".equals(tipoMovimiento)) {
                throw new IllegalArgumentException("El tipo de movimiento debe ser 'ENTRADA' o 'SALIDA'");
            }
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser un número positivo.");
            }
            productoDAO.registrarMovimiento(idProducto, cantidad, tipoMovimiento, descripcion);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar el movimiento de inventario", e);
        }
    }

    public List<model.MovimientoInventario> obtenerTodosLosMovimientos() {
        try {
            return productoDAO.findAllMovimientos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener los movimientos de inventario", e);
        }
    }

    public List<model.MovimientoInventario> obtenerMovimientosPorProducto(int idProducto) {
        try {
            return productoDAO.findMovimientosByProductoId(idProducto);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener los movimientos de inventario para el producto", e);
        }
    }
}