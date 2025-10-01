package service;

import dao.VentaDAO;
import model.DetalleVenta;
import model.ProductoVendido;
import model.Venta;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class VentaService implements Serializable {

    private transient VentaDAO ventaDAO;

    public VentaService() {
        this.ventaDAO = new VentaDAO();
    }

    /**
     * Orquesta el proceso de registrar una nueva venta.
     * Llama al DAO para ejecutar la transacción en la base de datos.
     *
     * @param venta    El objeto Venta con la información general de la venta.
     * @param detalles La lista de detalles de la venta.
     * @throws RuntimeException si ocurre un error durante la transacción.
     */
    public void realizarVenta(Venta venta, List<DetalleVenta> detalles) {
        if (venta == null || detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("Los datos de la venta no pueden ser nulos o vacíos.");
        }

        try {
            ventaDAO.registrar(venta, detalles);
        } catch (RuntimeException e) {
            // Aquí se podría añadir logging adicional o manejar la excepción de forma específica
            // Por ahora, la relanzamos para que la capa superior (el Bean) la capture.
            System.err.println("VentaService: Ocurrió un error al intentar realizar la venta.");
            throw e;
        }
    }

    public List<ProductoVendido> getBestSellingProducts(int limit) {
        try {
            return ventaDAO.findBestSellingProducts(limit);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener los productos más vendidos", e);
        }
    }
}