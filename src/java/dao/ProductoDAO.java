package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Producto;

public class ProductoDAO {

    public List<Producto> findAll() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY nombreProducto ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setIdProducto(rs.getInt("idProducto"));
                p.setNombreProducto(rs.getString("nombreProducto"));
                p.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                p.setIva(rs.getDouble("iva"));
                p.setCantidadStock(rs.getInt("cantidadStock"));
                p.setFechaCaducidad(rs.getDate("fechaCaducidad"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setIdCategoria(rs.getInt("idCategoria"));
                productos.add(p);
            }
        }
        return productos;
    }

    public void actualizarStock(int idProducto, int cantidad) throws SQLException {
        String sql = "UPDATE producto SET cantidadStock = cantidadStock + ? WHERE idProducto = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, idProducto);
            stmt.executeUpdate();
        }
    }

    public void insert(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (nombreProducto, precioUnitario, iva, cantidadStock, fechaCaducidad, descripcion, idCategoria) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getNombreProducto());
            stmt.setBigDecimal(2, producto.getPrecioUnitario());
            stmt.setDouble(3, producto.getIva());
            stmt.setInt(4, producto.getCantidadStock());

            if (producto.getFechaCaducidad() != null) {
                stmt.setDate(5, new java.sql.Date(producto.getFechaCaducidad().getTime()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setString(6, producto.getDescripcion());
            stmt.setInt(7, producto.getIdCategoria()); // ✅ ya dinámico
            stmt.executeUpdate();
        }
    }
    public Producto findById(int idProducto) throws SQLException {
        String sql = "SELECT * FROM producto WHERE idProducto = ?";
        try (Connection conn = Conexion.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("idProducto"));
                    p.setNombreProducto(rs.getString("nombreProducto"));
                    p.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    p.setIva(rs.getDouble("iva"));
                    p.setCantidadStock(rs.getInt("cantidadStock"));
                    p.setFechaCaducidad(rs.getDate("fechaCaducidad"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setIdCategoria(rs.getInt("idCategoria"));
                    return p;
                }
            }
        }
        return null;
    }

}
