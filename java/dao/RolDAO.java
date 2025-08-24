package dao;

import java.sql.*;

public class RolDAO {
    public Integer findIdByNombre(String nombre) {
        String sql = "SELECT pk_id_roles FROM tbl_roles WHERE UPPER(desc_rol) = UPPER(?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando rol", e);
        }
        return null;
    }
}
