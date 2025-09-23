package dao;

import model.Pqrc;
import model.Cliente;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PqrcDAO {

    public void registrar(Pqrc pqrc) throws SQLException {
        String sql = "INSERT INTO pqrc (tipo, descripcion, fecha, estado, idCliente, idUsuario) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pqrc.getTipo());
            ps.setString(2, pqrc.getDescripcion());
            ps.setDate(3, pqrc.getFecha());
            ps.setString(4, pqrc.getEstado());
            ps.setInt(5, pqrc.getIdCliente());
            ps.setInt(6, pqrc.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public List<Pqrc> listarTodas() {
        List<Pqrc> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nombres as cliente_nombres, c.apellidos as cliente_apellidos, u.nombres as usuario_nombres " +
                     "FROM pqrc p " +
                     "JOIN cliente c ON p.idCliente = c.idCliente " +
                     "JOIN usuario u ON p.idUsuario = u.idUsuario";

        try (Connection conn = Conexion.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Pqrc pqrc = new Pqrc();
                pqrc.setIdPqrc(rs.getInt("idPqrc"));
                pqrc.setTipo(rs.getString("tipo"));
                pqrc.setDescripcion(rs.getString("descripcion"));
                pqrc.setFecha(rs.getDate("fecha"));
                pqrc.setEstado(rs.getString("estado"));
                pqrc.setIdCliente(rs.getInt("idCliente"));
                pqrc.setIdUsuario(rs.getInt("idUsuario"));

                Cliente cliente = new Cliente();
                cliente.setNombres(rs.getString("cliente_nombres"));
                cliente.setApellidos(rs.getString("cliente_apellidos"));
                pqrc.setCliente(cliente);

                Usuario usuario = new Usuario();
                usuario.setNombres(rs.getString("usuario_nombres"));
                pqrc.setUsuario(usuario);

                lista.add(pqrc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    public void actualizarEstado(int idPqrc, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pqrc SET estado = ? WHERE idPqrc = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPqrc);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idPqrc) throws SQLException {
        String sql = "DELETE FROM pqrc WHERE idPqrc = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPqrc);
            ps.executeUpdate();
        }
    }
}
