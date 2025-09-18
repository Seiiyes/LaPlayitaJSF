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

    public void registrar(Venta venta, List<DetalleVenta> detalles) {
        Connection conn = null;
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psUpdateStock = null;
        ResultSet rs = null;

        String sqlVenta = "INSERT INTO venta (fechaVenta, horaVenta, total, idCliente, idUsuario) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalleventa (idVenta, idProducto, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE producto SET cantidadStock = cantidadStock - ? WHERE idProducto = ?";

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

            // 2. Insertar los detalles de la venta y actualizar el stock
            psDetalle = conn.prepareStatement(sqlDetalle);
            psUpdateStock = conn.prepareStatement(sqlUpdateStock);

            for (DetalleVenta detalle : detalles) {
                // Insertar detalle
                detalle.setIdVenta(idVentaGenerado);
                psDetalle.setInt(1, detalle.getIdVenta());
                psDetalle.setInt(2, detalle.getIdProducto());
                psDetalle.setInt(3, detalle.getCantidad());
                psDetalle.setBigDecimal(4, detalle.getSubtotal());
                psDetalle.addBatch();

                // Actualizar stock
                psUpdateStock.setInt(1, detalle.getCantidad());
                psUpdateStock.setInt(2, detalle.getIdProducto());
                psUpdateStock.addBatch();
            }

            psDetalle.executeBatch();
            psUpdateStock.executeBatch();

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
                if (psUpdateStock != null) psUpdateStock.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }
}
