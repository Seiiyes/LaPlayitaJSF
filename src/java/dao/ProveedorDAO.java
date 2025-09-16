package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import model.Proveedor;

@ApplicationScoped
public class ProveedorDAO {

    public List<Proveedor> findAll() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedor ORDER BY nombres ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setIdProveedor(rs.getInt("idProveedor"));
                p.setNombres(rs.getString("nombres"));
                p.setApellidos(rs.getString("apellidos"));
                p.setTelefono(rs.getString("telefono"));
                p.setCorreo(rs.getString("correo"));
                p.setDireccion(rs.getString("direccion"));
                proveedores.add(p);
            }
        }
        return proveedores;
    }
}