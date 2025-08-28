package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO para la entidad Rol.
 */
public class RolDAO {

    /**
     * Busca el ID de un rol por su nombre de forma case-insensitive.
     *
     * @param nombre Nombre del rol (ej: "ADMIN", "VENDEDOR")
     * @return ID del rol si existe, o null si no se encuentra
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

        } catch (Exception e) {
            throw new RuntimeException("Error consultando rol", e);
        }

        return null; // no se encontró el rol
    }
}
