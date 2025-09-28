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

    public Proveedor findById(int id) throws SQLException {
        String sql = "SELECT * FROM proveedor WHERE idProveedor = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Proveedor p = new Proveedor();
                    p.setIdProveedor(rs.getInt("idProveedor"));
                    p.setNombres(rs.getString("nombres"));
                    p.setApellidos(rs.getString("apellidos"));
                    p.setTelefono(rs.getString("telefono"));
                    p.setCorreo(rs.getString("correo"));
                    p.setDireccion(rs.getString("direccion"));
                    return p;
                }
            }
        }
        return null;
    }

    public void save(Proveedor proveedor) throws SQLException {
        if (proveedor.getIdProveedor() == null || proveedor.getIdProveedor() == 0) {
            insert(proveedor);
        } else {
            update(proveedor);
        }
    }

    private void insert(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedor (nombres, apellidos, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, proveedor.getNombres());
            stmt.setString(2, proveedor.getApellidos());
            stmt.setString(3, proveedor.getTelefono());
            stmt.setString(4, proveedor.getCorreo());
            stmt.setString(5, proveedor.getDireccion());
            stmt.executeUpdate();
        }
    }

    private void update(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE proveedor SET nombres = ?, apellidos = ?, telefono = ?, correo = ?, direccion = ? WHERE idProveedor = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, proveedor.getNombres());
            stmt.setString(2, proveedor.getApellidos());
            stmt.setString(3, proveedor.getTelefono());
            stmt.setString(4, proveedor.getCorreo());
            stmt.setString(5, proveedor.getDireccion());
            stmt.setInt(6, proveedor.getIdProveedor());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE idProveedor = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
