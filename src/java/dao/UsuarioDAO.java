package dao;

import model.Usuario;
import model.Rol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario findByCorreo(String correo) {
        String sql = "SELECT u.*, r.descripcionRol " +
                     "FROM usuario u LEFT JOIN rol r ON u.idRol = r.idRol " +
                     "WHERE u.correo = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario por correo", e);
        }
        return null;
    }

    public Usuario findByDocumento(String doc) {
        String sql = "SELECT u.*, r.descripcionRol " +
                     "FROM usuario u LEFT JOIN rol r ON u.idRol = r.idRol " +
                     "WHERE u.documento = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario por documento", e);
        }
        return null;
    }

    public Usuario findById(int idUsuario) {
        String sql = "SELECT u.*, r.descripcionRol " +
                     "FROM usuario u LEFT JOIN rol r ON u.idRol = r.idRol " +
                     "WHERE u.idUsuario = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando usuario por ID", e);
        }
        return null;
    }

    public int crear(Usuario u) {
        String sql = "INSERT INTO usuario "
                + "(documento, nombres, apellidos, correo, telefono, contrasena, estadoUsuario, fechaCreacion, idRol) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getDocumento());
            ps.setString(2, u.getNombres());
            ps.setString(3, u.getApellidos());
            ps.setString(4, u.getCorreo());
            ps.setString(5, u.getTelefono());
            ps.setString(6, u.getContrasenaHash());
            ps.setInt(7, u.getEstadoUsuario());
            ps.setInt(8, u.getIdRol());

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("No se insertó usuario");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("⚠️ Correo o documento ya registrado", e);
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
            System.err.println("⚠️ No se pudo actualizar último login: " + e.getMessage());
        }
    }

    public List<Usuario> findAll() {
        String sql = "SELECT u.*, r.descripcionRol FROM usuario u LEFT JOIN rol r ON u.idRol = r.idRol ORDER BY u.idUsuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
        return lista;
    }

    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE idUsuario = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando usuario", e);
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

        // POBLAR EL OBJETO ROL (si existe la columna descripcionRol en ResultSet)
        try {
            String descripcionRol = null;
            try {
                descripcionRol = rs.getString("descripcionRol");
            } catch (SQLException ignore) { /* columna no presente */ }

            if (descripcionRol != null) {
                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("idRol"));
                rol.setDescripcionRol(descripcionRol);
                u.setRol(rol);
            }
        } catch (SQLException e) {
            // no crítico: si no se puede poblar el rol, dejamos idRol como está
        }

        return u;
    }
    public void actualizar(Usuario u) {
        String sql = "UPDATE usuario SET documento=?, nombres=?, apellidos=?, correo=?, telefono=?, idRol=?, estadoUsuario=? "
                + "WHERE idUsuario=?";
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getDocumento());
            ps.setString(2, u.getNombres());
            ps.setString(3, u.getApellidos());
            ps.setString(4, u.getCorreo());
            ps.setString(5, u.getTelefono());
            ps.setInt(6, u.getIdRol());
            ps.setInt(7, u.getEstadoUsuario());
            ps.setInt(8, u.getIdUsuario());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("No se actualizó ningún usuario");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("⚠️ Correo o documento ya registrado", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando usuario", e);
        }
    }

}
