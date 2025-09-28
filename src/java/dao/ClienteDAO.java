package dao;

import model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClienteDAO implements java.io.Serializable {

    public List<Cliente> listarTodos() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT idCliente, documento, nombres, apellidos, correo, telefono FROM cliente ORDER BY nombres, apellidos";

        try {
            conn = Conexion.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setIdCliente(rs.getInt("idCliente"));
                cliente.setDocumento(rs.getString("documento"));
                cliente.setNombres(rs.getString("nombres"));
                cliente.setApellidos(rs.getString("apellidos"));
                cliente.setCorreo(rs.getString("correo"));
                cliente.setTelefono(rs.getString("telefono"));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los clientes: " + e.getMessage());
            throw new RuntimeException("No se pudo consultar la lista de clientes", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        return clientes;
    }
}
