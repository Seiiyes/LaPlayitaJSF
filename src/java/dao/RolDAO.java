package dao;

import javax.enterprise.context.ApplicationScoped;
import model.Rol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RolDAO {

    /**
     * Devuelve el idRol si existe, o null si no lo encuentra
     * @param nombre
     * @return 
     */
    public Integer findIdByNombre(String nombre) {
        String sql = "SELECT idRol FROM rol WHERE UPPER(descripcionRol) = UPPER(?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idRol");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando rol", e);
        }
        return null;
    }

    /**
     * Lista todos los roles
     * @return 
     */
    public List<Rol> findAll() {
        String sql = "SELECT idRol, descripcionRol FROM rol ORDER BY descripcionRol";
        List<Rol> roles = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("idRol"));
                rol.setDescripcionRol(rs.getString("descripcionRol"));
                roles.add(rol);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando roles", e);
        }
        return roles;
    }
    public Rol findById(int idRol) {
        String sql = "SELECT idRol, descripcionRol FROM rol WHERE idRol = ?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol rol = new Rol();
                    rol.setIdRol(rs.getInt("idRol"));
                    rol.setDescripcionRol(rs.getString("descripcionRol"));
                    return rol;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando rol por ID", e);
        }
        return null;
    }

}
