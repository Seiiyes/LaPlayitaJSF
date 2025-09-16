package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import model.Producto;

@ApplicationScoped
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
        // Suma la cantidad al stock existente. Para restar, se pasaría una cantidad negativa.
        String sql = "UPDATE producto SET cantidadStock = cantidadStock + ? WHERE idProducto = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, idProducto);
            stmt.executeUpdate();
        }
    }
}