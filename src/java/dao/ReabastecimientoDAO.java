package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;

@ApplicationScoped
public class ReabastecimientoDAO {

    public List<Reabastecimiento> findAll() throws SQLException {
        List<Reabastecimiento> reabastecimientos = new ArrayList<>();
        String sql = "SELECT r.idReabastecimiento, r.fecha, r.estadoCompra, r.observaciones, " +
                     "p.idProveedor, p.nombres AS nombreProveedor, " +
                     "prod.idProducto, prod.nombreProducto, " +
                     "dr.cantidad, dr.costoUnitario " +
                     "FROM reabastecimiento r " +
                     "JOIN detallereabastecimiento dr ON r.idReabastecimiento = dr.idReabastecimiento " +
                     "JOIN proveedor p ON r.idProveedor = p.idProveedor " +
                     "JOIN producto prod ON dr.idProducto = prod.idProducto " +
                     "ORDER BY r.fecha DESC, r.idReabastecimiento DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reabastecimiento item = new Reabastecimiento();
                item.setIdReabastecimiento(rs.getInt("idReabastecimiento"));
                item.setFecha(rs.getDate("fecha"));
                item.setEstado(rs.getString("estadoCompra"));
                item.setObservaciones(rs.getString("observaciones"));
                item.setCantidad(rs.getInt("cantidad"));
                item.setCostoUnitario(rs.getBigDecimal("costoUnitario"));

                Proveedor prov = new Proveedor();
                prov.setIdProveedor(rs.getInt("idProveedor"));
                prov.setNombres(rs.getString("nombreProveedor"));
                item.setProveedor(prov);

                Producto prod = new Producto();
                prod.setIdProducto(rs.getInt("idProducto"));
                prod.setNombreProducto(rs.getString("nombreProducto"));
                item.setProducto(prod);
                
                reabastecimientos.add(item);
            }
        }
        return reabastecimientos;
    }

    public void save(Reabastecimiento reab) throws SQLException {
        // Determina si es una inserción o una actualización
        if (reab.getIdReabastecimiento() == 0) {
            insert(reab);
        } else {
            update(reab);
        }
    }

    private void insert(Reabastecimiento reab) throws SQLException {
        String sqlReab = "INSERT INTO reabastecimiento (fecha, hora, costo, estadoCompra, formaPago, observaciones, idProveedor) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detallereabastecimiento (idProducto, idReabastecimiento, cantidad, costoUnitario) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en la tabla maestra 'reabastecimiento'
            try (PreparedStatement stmtReab = conn.prepareStatement(sqlReab, Statement.RETURN_GENERATED_KEYS)) {
                stmtReab.setDate(1, new java.sql.Date(reab.getFecha().getTime()));
                stmtReab.setTime(2, new Time(System.currentTimeMillis()));
                stmtReab.setBigDecimal(3, reab.getCostoTotal());
                stmtReab.setString(4, reab.getEstado());
                stmtReab.setString(5, "Efectivo"); // Valor por defecto
                stmtReab.setString(6, reab.getObservaciones());
                stmtReab.setInt(7, reab.getIdProveedor());
                stmtReab.executeUpdate();

                // 2. Obtener el ID generado
                try (ResultSet generatedKeys = stmtReab.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reab.setIdReabastecimiento(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("No se pudo obtener el ID del reabastecimiento.");
                    }
                }
            }

            // 3. Insertar en la tabla de detalle 'detallereabastecimiento'
            try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                stmtDetalle.setInt(1, reab.getIdProducto());
                stmtDetalle.setInt(2, reab.getIdReabastecimiento());
                stmtDetalle.setInt(3, reab.getCantidad());
                stmtDetalle.setBigDecimal(4, reab.getCostoUnitario());
                stmtDetalle.executeUpdate();
            }

            conn.commit(); // Confirmar transacción

        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // Revertir en caso de error
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    private void update(Reabastecimiento reab) throws SQLException {
        String sqlReab = "UPDATE reabastecimiento SET fecha = ?, costo = ?, estadoCompra = ?, observaciones = ?, idProveedor = ? WHERE idReabastecimiento = ?";
        String sqlDetalle = "UPDATE detallereabastecimiento SET idProducto = ?, cantidad = ?, costoUnitario = ? WHERE idReabastecimiento = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtReab = conn.prepareStatement(sqlReab)) {
                stmtReab.setDate(1, new java.sql.Date(reab.getFecha().getTime()));
                stmtReab.setBigDecimal(2, reab.getCostoTotal());
                stmtReab.setString(3, reab.getEstado());
                stmtReab.setString(4, reab.getObservaciones());
                stmtReab.setInt(5, reab.getIdProveedor());
                stmtReab.setInt(6, reab.getIdReabastecimiento());
                stmtReab.executeUpdate();
            }

            try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle)) {
                stmtDetalle.setInt(1, reab.getIdProducto());
                stmtDetalle.setInt(2, reab.getCantidad());
                stmtDetalle.setBigDecimal(3, reab.getCostoUnitario());
                stmtDetalle.setInt(4, reab.getIdReabastecimiento());
                stmtDetalle.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (conn != null) conn.close();
        }
    }

    public void delete(int idReabastecimiento) throws SQLException {
        String sql = "DELETE FROM reabastecimiento WHERE idReabastecimiento = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReabastecimiento);
            stmt.executeUpdate();
        }
    }
}