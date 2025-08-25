package dao;

import model.Usuario;

import java.sql.*;
import java.util.Optional;

public class UsuarioDAO {

    public Optional<Usuario> findByCorreo(String correo) {
        String sql = "SELECT * FROM tbl_usuario WHERE correo_u = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando usuario por correo", e);
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByDocumento(String doc) {
        String sql = "SELECT * FROM tbl_usuario WHERE documento_u = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando usuario por documento", e);
        }
        return Optional.empty();
    }

    public int crear(Usuario u) {
        String sql = "INSERT INTO tbl_usuario " +
                "(documento_u, p_nombre_u, s_nombre_u, p_apellido_u, s_apellido_u, " +
                "correo_u, telefono_u, contrasena, estado_usuario, fecha_creacion, fk_id_roles) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getDocumento());
            ps.setString(2, u.getpNombre());
            ps.setString(3, u.getsNombre());
            ps.setString(4, u.getpApellido());
            ps.setString(5, u.getsApellido());
            ps.setString(6, u.getCorreo());
            ps.setString(7, u.getTelefono());
            ps.setString(8, u.getContrasenaHash()); // BCrypt hash
            ps.setInt(9, u.getEstadoUsuario());     // 1 = activo
            ps.setInt(10, u.getIdRol());            // FK a tbl_roles

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("No se inserto usuario");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                throw new RuntimeException("Correo o documento ya registrado", e);
            }
            throw new RuntimeException("Error creando usuario", e);
        }
        return 0;
    }

    public void actualizarUltimoLogin(int idUsuario) {
        String sql = "UPDATE tbl_usuario SET ultimo_login = CURRENT_TIMESTAMP WHERE pk_id_usuario = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (Exception e) {
            // No es crítico para bloquear el login
        }
    }

    private Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("pk_id_usuario"));
        u.setDocumento(rs.getString("documento_u"));
        u.setpNombre(rs.getString("p_nombre_u"));
        u.setsNombre(rs.getString("s_nombre_u"));
        u.setpApellido(rs.getString("p_apellido_u"));
        u.setsApellido(rs.getString("s_apellido_u"));
        u.setCorreo(rs.getString("correo_u"));
        u.setTelefono(rs.getString("telefono_u"));
        u.setContrasenaHash(rs.getString("contrasena"));
        u.setEstadoUsuario(rs.getInt("estado_usuario"));
        u.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        u.setUltimoLogin(rs.getTimestamp("ultimo_login"));
        u.setIdRol(rs.getInt("fk_id_roles"));
        return u;
    }
}
