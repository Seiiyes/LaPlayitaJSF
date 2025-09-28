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
import model.DetalleReabastecimiento;
import model.EstadoReabastecimiento; // Importar el enum
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;

@ApplicationScoped
public class ReabastecimientoDAO implements java.io.Serializable {

    public List<Reabastecimiento> findAllMasters() throws SQLException {
        List<Reabastecimiento> reabastecimientos = new ArrayList<>();
        String sql = "SELECT r.idReabastecimiento, r.fecha, r.hora, r.costo, r.estadoCompra, r.observaciones, r.formaPago, " +
                     "p.idProveedor, p.nombres AS nombreProveedor, " +
                     "dr.cantidad, dr.costoUnitario, prod.idProducto, prod.nombreProducto " +
                     "FROM reabastecimiento r " +
                     "JOIN proveedor p ON r.idProveedor = p.idProveedor " +
                     "LEFT JOIN detallereabastecimiento dr ON r.idReabastecimiento = dr.idReabastecimiento " +
                     "LEFT JOIN producto prod ON dr.idProducto = prod.idProducto " +
                     "ORDER BY r.fecha DESC, r.idReabastecimiento DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Reabastecimiento currentReabastecimiento = null;
            while (rs.next()) {
                int reabastecimientoId = rs.getInt("idReabastecimiento");

                if (currentReabastecimiento == null || currentReabastecimiento.getIdReabastecimiento() != reabastecimientoId) {
                    currentReabastecimiento = new Reabastecimiento();
                    currentReabastecimiento.setIdReabastecimiento(reabastecimientoId);
                    currentReabastecimiento.setFecha(rs.getDate("fecha"));
                    currentReabastecimiento.setHora(rs.getTime("hora"));
                    currentReabastecimiento.setEstado(EstadoReabastecimiento.fromDisplayValue(rs.getString("estadoCompra")));
                    currentReabastecimiento.setObservaciones(rs.getString("observaciones"));
                    currentReabastecimiento.setCostoTotal(rs.getBigDecimal("costo"));
                    currentReabastecimiento.setFormaPago(rs.getString("formaPago"));

                    Proveedor prov = new Proveedor();
                    prov.setIdProveedor(rs.getInt("idProveedor"));
                    prov.setNombres(rs.getString("nombreProveedor"));
                    currentReabastecimiento.setProveedor(prov);
                    
                    currentReabastecimiento.setDetalles(new ArrayList<>());
                    reabastecimientos.add(currentReabastecimiento);
                }

                // Add details if they exist
                if (rs.getObject("idProducto") != null) { // Check if there's a detail row
                    DetalleReabastecimiento det = new DetalleReabastecimiento();
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setCostoUnitario(rs.getBigDecimal("costoUnitario"));

                    Producto prod = new Producto();
                    prod.setIdProducto(rs.getInt("idProducto"));
                    prod.setNombreProducto(rs.getString("nombreProducto"));
                    det.setProducto(prod);
                    
                    currentReabastecimiento.getDetalles().add(det);
                }
            }
        }
        return reabastecimientos;
    }

    public Reabastecimiento findMasterById(int idReabastecimiento) throws SQLException {
        Reabastecimiento item = null;
        String sql = "SELECT r.idReabastecimiento, r.fecha, r.hora, r.costo, r.estadoCompra, r.observaciones, " +
                     "p.idProveedor, p.nombres AS nombreProveedor " +
                     "FROM reabastecimiento r " +
                     "JOIN proveedor p ON r.idProveedor = p.idProveedor " +
                     "WHERE r.idReabastecimiento = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReabastecimiento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    item = new Reabastecimiento();
                    item.setIdReabastecimiento(rs.getInt("idReabastecimiento"));
                    item.setFecha(rs.getDate("fecha"));
                    item.setHora(rs.getTime("hora")); // Retrieve hora
                    item.setEstado(EstadoReabastecimiento.fromDisplayValue(rs.getString("estadoCompra")));
                    item.setObservaciones(rs.getString("observaciones"));
                    item.setCostoTotal(rs.getBigDecimal("costo"));

                    Proveedor prov = new Proveedor();
                    prov.setIdProveedor(rs.getInt("idProveedor"));
                    prov.setNombres(rs.getString("nombreProveedor"));
                    item.setProveedor(prov);
                }
            }
        }
        return item;
    }

    public List<DetalleReabastecimiento> findDetailsByReabastecimientoId(int idReabastecimiento) throws SQLException {
        List<DetalleReabastecimiento> detalles = new ArrayList<>();
        String sql = "SELECT d.cantidad, d.costoUnitario, p.idProducto, p.nombreProducto " +
                     "FROM detallereabastecimiento d " +
                     "JOIN producto p ON d.idProducto = p.idProducto " +
                     "WHERE d.idReabastecimiento = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReabastecimiento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DetalleReabastecimiento det = new DetalleReabastecimiento();
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setCostoUnitario(rs.getBigDecimal("costoUnitario"));

                    Producto prod = new Producto();
                    prod.setIdProducto(rs.getInt("idProducto"));
                    prod.setNombreProducto(rs.getString("nombreProducto"));
                    det.setProducto(prod);
                    
                    detalles.add(det);
                }
            }
        }
        return detalles;
    }

    public void saveMaestroDetalle(Reabastecimiento reab) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            // Guardar cabecera
            if (reab.getIdReabastecimiento() == 0) {
                insertMaster(conn, reab);
            } else {
                updateMaster(conn, reab);
            }

            // Borrar detalles existentes
            deleteDetails(conn, reab.getIdReabastecimiento());

            // Insertar nuevos detalles
            insertDetails(conn, reab);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private void insertMaster(Connection conn, Reabastecimiento reab) throws SQLException {
        String sql = "INSERT INTO reabastecimiento (fecha, hora, costo, estadoCompra, formaPago, observaciones, idProveedor) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, new java.sql.Date(reab.getFecha().getTime()));
            stmt.setTime(2, reab.getHora() != null ? reab.getHora() : new java.sql.Time(System.currentTimeMillis()));
            stmt.setBigDecimal(3, reab.getCostoTotal());
            stmt.setString(4, reab.getEstado().getDisplayValue());
            stmt.setString(5, reab.getFormaPago());
            stmt.setString(6, reab.getObservaciones());
            stmt.setInt(7, reab.getProveedor().getIdProveedor());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reab.setIdReabastecimiento(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del reabastecimiento.");
                }
            }
        }
    }

    private void updateMaster(Connection conn, Reabastecimiento reab) throws SQLException {
        String sql = "UPDATE reabastecimiento SET fecha = ?, hora = ?, costo = ?, estadoCompra = ?, formaPago = ?, observaciones = ?, idProveedor = ? WHERE idReabastecimiento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(reab.getFecha().getTime()));
            stmt.setTime(2, reab.getHora() != null ? reab.getHora() : new java.sql.Time(System.currentTimeMillis()));
            stmt.setBigDecimal(3, reab.getCostoTotal());
            stmt.setString(4, reab.getEstado().getDisplayValue());
            stmt.setString(5, reab.getFormaPago());
            stmt.setString(6, reab.getObservaciones());
            stmt.setInt(7, reab.getProveedor().getIdProveedor());
            stmt.setInt(8, reab.getIdReabastecimiento());
            stmt.executeUpdate();
        }
    }

    private void deleteDetails(Connection conn, int idReabastecimiento) throws SQLException {
        String sql = "DELETE FROM detallereabastecimiento WHERE idReabastecimiento = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReabastecimiento);
            stmt.executeUpdate();
        }
    }

    private void insertDetails(Connection conn, Reabastecimiento reab) throws SQLException {
        String sql = "INSERT INTO detallereabastecimiento (idProducto, idReabastecimiento, cantidad, costoUnitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (DetalleReabastecimiento det : reab.getDetalles()) {
                stmt.setInt(1, det.getProducto().getIdProducto());
                stmt.setInt(2, reab.getIdReabastecimiento());
                stmt.setInt(3, det.getCantidad());
                stmt.setBigDecimal(4, det.getCostoUnitario());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void delete(int idReabastecimiento) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            // Primero, eliminar los detalles asociados
            deleteDetails(conn, idReabastecimiento);

            // Luego, eliminar el registro maestro
            String sql = "DELETE FROM reabastecimiento WHERE idReabastecimiento = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idReabastecimiento);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}