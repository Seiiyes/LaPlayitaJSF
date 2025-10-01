package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Producto;

public class ProductoDAO implements java.io.Serializable {

    public Producto findById(int idProducto) throws SQLException {
        String sql = "SELECT * FROM producto WHERE idProducto = ?";
        Producto producto = null;
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    producto = new Producto();
                    producto.setIdProducto(rs.getInt("idProducto"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    producto.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    producto.setFechaCaducidad(rs.getDate("fechaCaducidad"));
                    producto.setDescripcion(rs.getString("descripcion"));
                    producto.setIdCategoria(rs.getInt("idCategoria"));
                }
            }
        }
        return producto;
    }

    /**
     * Busca todos los productos y calcula su stock actual desde la tabla de inventario.
     * @return Lista de productos con el campo stockActual poblado.
     * @throws SQLException 
     */
    public List<Producto> findAllWithStock() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.nombreProducto, p.precioUnitario, p.fechaCaducidad, p.descripcion, p.idCategoria, " +
                     "COALESCE(SUM(CASE WHEN i.tipoMovimiento = 'ENTRADA' THEN i.cantidad WHEN i.tipoMovimiento = 'SALIDA' THEN -i.cantidad ELSE 0 END), 0) AS stock_actual " +
                     "FROM producto p " +
                     "LEFT JOIN inventario i ON p.idProducto = i.idProducto " +
                     "GROUP BY p.idProducto " +
                     "ORDER BY p.nombreProducto ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombreProducto(rs.getString("nombreProducto"));
                p.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                p.setFechaCaducidad(rs.getDate("fechaCaducidad"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setIdCategoria(rs.getInt("idCategoria"));
                p.setStockActual(rs.getInt("stock_actual"));
                productos.add(p);
            }
        }
        return productos;
    }

    /**
     * Inserta un nuevo producto y su movimiento de inventario inicial de forma transaccional.
     * @param producto El objeto producto a insertar (sin ID).
     * @param stockInicial La cantidad inicial para el primer registro de inventario.
     * @throws SQLException 
     */
    public void insert(Producto producto, int stockInicial) throws SQLException {
        String sqlProducto = "INSERT INTO producto (nombreProducto, precioUnitario, fechaCaducidad, descripcion, idCategoria) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmtProducto = null;
        ResultSet generatedKeys = null;

        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            stmtProducto = conn.prepareStatement(sqlProducto, Statement.RETURN_GENERATED_KEYS);
            stmtProducto.setString(1, producto.getNombreProducto());
            stmtProducto.setBigDecimal(2, producto.getPrecioUnitario());
            if (producto.getFechaCaducidad() != null) {
                stmtProducto.setDate(3, new java.sql.Date(producto.getFechaCaducidad().getTime()));
            } else {
                stmtProducto.setNull(3, java.sql.Types.DATE);
            }
            stmtProducto.setString(4, producto.getDescripcion());
            stmtProducto.setInt(5, producto.getIdCategoria());
            stmtProducto.executeUpdate();

            generatedKeys = stmtProducto.getGeneratedKeys();
            if (generatedKeys.next()) {
                int idProducto = generatedKeys.getInt(1);
                producto.setIdProducto(idProducto);

                if (stockInicial > 0) {
                    registrarMovimiento(conn, idProducto, stockInicial, "ENTRADA", "Stock inicial");
                }
            } else {
                throw new SQLException("La inserción del producto falló, no se obtuvo ID.");
            }

            conn.commit(); // Finalizar transacción
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (stmtProducto != null) stmtProducto.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Registra un movimiento en la tabla de inventario.
     * @param conn La conexión a la base de datos (puede ser parte de una transacción existente).
     * @param idProducto El ID del producto.
     * @param cantidad La cantidad del movimiento (siempre positivo).
     * @param tipoMovimiento 'ENTRADA' o 'SALIDA'.
     * @param descripcion Una descripción del motivo del movimiento.
     * @throws SQLException 
     */
    public void registrarMovimiento(Connection conn, int idProducto, int cantidad, String tipoMovimiento, String descripcion) throws SQLException {
        String sql = "INSERT INTO inventario (idProducto, cantidad, tipoMovimiento, descripcionMovimiento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            stmt.setInt(2, cantidad);
            stmt.setString(3, tipoMovimiento);
            stmt.setString(4, descripcion);
            stmt.executeUpdate();
        }
    }

    /**
     * Registra un movimiento de inventario como una operación única y autogestionada.
     */
    public void registrarMovimiento(int idProducto, int cantidad, String tipoMovimiento, String descripcion) throws SQLException {
        String sql = "INSERT INTO inventario (idProducto, cantidad, tipoMovimiento, descripcionMovimiento) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            stmt.setInt(2, cantidad);
            stmt.setString(3, tipoMovimiento);
            stmt.setString(4, descripcion);
            stmt.executeUpdate();
        }
    }

    public List<model.MovimientoInventario> findAllMovimientos() throws SQLException {
        List<model.MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT i.idInventario, i.idProducto, p.nombreProducto, i.cantidad, i.tipoMovimiento, i.descripcionMovimiento, i.fechaMovimiento " +
                     "FROM inventario i " +
                     "JOIN producto p ON i.idProducto = p.idProducto " +
                     "ORDER BY i.fechaMovimiento DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.MovimientoInventario m = new model.MovimientoInventario();
                m.setIdInventario(rs.getInt("idInventario"));
                m.setIdProducto(rs.getInt("idProducto"));
                m.setNombreProducto(rs.getString("nombreProducto"));
                m.setCantidad(rs.getInt("cantidad"));
                m.setTipoMovimiento(rs.getString("tipoMovimiento"));
                m.setDescripcionMovimiento(rs.getString("descripcionMovimiento"));
                m.setFechaMovimiento(rs.getTimestamp("fechaMovimiento"));
                movimientos.add(m);
            }
        }
        return movimientos;
    }

    public List<model.MovimientoInventario> findMovimientosByProductoId(int idProducto) throws SQLException {
        List<model.MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT i.idInventario, i.idProducto, p.nombreProducto, i.cantidad, i.tipoMovimiento, i.descripcionMovimiento, i.fechaMovimiento " +
                     "FROM inventario i " +
                     "JOIN producto p ON i.idProducto = p.idProducto " +
                     "WHERE i.idProducto = ? " +
                     "ORDER BY i.fechaMovimiento DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    model.MovimientoInventario m = new model.MovimientoInventario();
                    m.setIdInventario(rs.getInt("idInventario"));
                    m.setIdProducto(rs.getInt("idProducto"));
                    m.setNombreProducto(rs.getString("nombreProducto"));
                    m.setCantidad(rs.getInt("cantidad"));
                    m.setTipoMovimiento(rs.getString("tipoMovimiento"));
                    m.setDescripcionMovimiento(rs.getString("descripcionMovimiento"));
                    m.setFechaMovimiento(rs.getTimestamp("fechaMovimiento"));
                    movimientos.add(m);
                }
            }
        }
        return movimientos;
    }
}