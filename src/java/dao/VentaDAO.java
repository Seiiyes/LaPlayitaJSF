package dao;

import model.DetalleVenta;
import model.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class VentaDAO {

    private final ProductoDAO productoDAO;

    public VentaDAO() {
        this.productoDAO = new ProductoDAO();
    }

    public void registrar(Venta venta, List<DetalleVenta> detalles) {
        Connection conn = null;
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;
        ResultSet rs = null;

        String sqlVenta = "INSERT INTO venta (fechaVenta, horaVenta, total, idCliente, idUsuario) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleventa (idVenta, idProducto, cantidad, subtotal) VALUES (?, ?, ?, ?)";

        try {
            conn = Conexion.getConnection();
            // Iniciar transacción
            conn.setAutoCommit(false);

            // 1. Insertar la venta y obtener el ID generado
            psVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setDate(1, venta.getFechaVenta());
            psVenta.setTime(2, venta.getHoraVenta());
            psVenta.setBigDecimal(3, venta.getTotal());
            psVenta.setInt(4, venta.getIdCliente());
            psVenta.setInt(5, venta.getIdUsuario());
            psVenta.executeUpdate();

            rs = psVenta.getGeneratedKeys();
            int idVentaGenerado = -1;
            if (rs.next()) {
                idVentaGenerado = rs.getInt(1);
            }

            if (idVentaGenerado == -1) {
                throw new SQLException("No se pudo obtener el ID de la venta generada.");
            }

            // 2. Insertar los detalles de la venta y registrar movimiento de inventario
            psDetalle = conn.prepareStatement(sqlDetalle);

            for (DetalleVenta detalle : detalles) {
                // Insertar detalle
                detalle.setIdVenta(idVentaGenerado);
                psDetalle.setInt(1, detalle.getIdVenta());
                psDetalle.setInt(2, detalle.getIdProducto());
                psDetalle.setInt(3, detalle.getCantidad());
                psDetalle.setBigDecimal(4, detalle.getSubtotal());
                psDetalle.addBatch();

                // Registrar movimiento de inventario
                productoDAO.registrarMovimiento(conn, detalle.getIdProducto(), detalle.getCantidad(), "SALIDA", "Venta #" + idVentaGenerado);
            }

            psDetalle.executeBatch();

            // Si todo fue exitoso, confirmar la transacción
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Error al registrar la venta: " + e.getMessage());
            if (conn != null) {
                try {
                    // Revertir la transacción en caso de error
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            // Relanzar la excepción para que la capa de servicio la maneje
            throw new RuntimeException("Error en la transacción de registro de venta", e);
        } finally {
            // 3. Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (psVenta != null) psVenta.close();
                if (psDetalle != null) psDetalle.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    public List<ProductoVendido> findBestSellingProducts(int limit) throws SQLException {
        List<ProductoVendido> productos = new ArrayList<>();
        String sql = "SELECT p.nombreProducto, SUM(dv.cantidad) AS totalVendido " +
                     "FROM detalleventa dv " +
                     "JOIN producto p ON dv.idProducto = p.idProducto " +
                     "GROUP BY p.nombreProducto " +
                     "ORDER BY totalVendido DESC " +
                     "LIMIT ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nombreProducto = rs.getString("nombreProducto");
                    int totalVendido = rs.getInt("totalVendido");
                    productos.add(new ProductoVendido(nombreProducto, totalVendido));
                }
            }
        }
        return productos;
    }