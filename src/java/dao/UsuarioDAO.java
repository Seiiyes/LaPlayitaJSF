package dao;

import model.Usuario;

import java.sql.*;
import java.util.Optional;

public class UsuarioDAO {

    public Optional<Usuario> findByCorreo(String correo) {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario por correo", e);
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByDocumento(String doc) {
        String sql = "SELECT * FROM usuario WHERE documento = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario por documento", e);
        }
        return Optional.empty();
    }

    public int crear(Usuario u) {
        String sql = "INSERT INTO usuario " +
                "(documento, nombres, apellidos, correo, telefono, contrasena, estadoUsuario, fechaCreacion, idRol) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getDocumento());
            ps.setString(2, u.getNombres());
            ps.setString(3, u.getApellidos());
            ps.setString(4, u.getCorreo());
            ps.setString(5, u.getTelefono());
            ps.setString(6, u.getContrasenaHash()); // BCrypt hash
            ps.setInt(7, u.getEstadoUsuario());     // 1 = activo
            ps.setInt(8, u.getIdRol());             // FK → rol

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("No se insertó usuario");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("❌ Correo o documento ya registrado", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error creando usuario", e);
        }
        return 0;
    }

    public void actualizarUltimoLogin(int idUsuario) {
        String sql = "UPDATE usuario SET ultimoLogin = CURRENT_TIMESTAMP WHERE idUsuario = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            // No es crítico, pero puedes loguear el error
        }
    }

    private Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setDocumento(rs.getString("documento"));
        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setTelefono(rs.getString("telefono"));
        u.setContrasenaHash(rs.getString("contrasena"));
        u.setEstadoUsuario(rs.getInt("estadoUsuario"));
        u.setFechaCreacion(rs.getTimestamp("fechaCreacion"));
        u.setUltimoLogin(rs.getTimestamp("ultimoLogin"));
        u.setIdRol(rs.getInt("idRol"));
        return u;
    }
}
