package dao;

import model.Pedido;
import model.Cliente;
import model.Usuario;
import model.DetallePedido;
import model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public List<Pedido> findAll() {
        String sql = "SELECT p.*, c.nombres as cliente_nombres, c.apellidos as cliente_apellidos, u.nombres as usuario_nombres, u.apellidos as usuario_apellidos " +
                     "FROM pedido p " +
                     "JOIN cliente c ON p.idCliente = c.idCliente " +
                     "JOIN usuario u ON p.idUsuario = u.idUsuario " +
                     "ORDER BY p.fechaPedido DESC";
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pedidos.add(mapPedido(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedidos", e);
        }
        return pedidos;
    }

    public Pedido findById(int id) {
        String sql = "SELECT p.*, c.nombres as cliente_nombres, c.apellidos as cliente_apellidos, u.nombres as usuario_nombres, u.apellidos as usuario_apellidos " +
                     "FROM pedido p " +
                     "JOIN cliente c ON p.idCliente = c.idCliente " +
                     "JOIN usuario u ON p.idUsuario = u.idUsuario " +
                     "WHERE p.idPedido = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPedido(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el pedido", e);
        }
        return null;
    }

    public int crear(Pedido pedido) {
        String sql = "INSERT INTO pedido (fechaPedido, estadoPedido, direccionEnvio, idCliente, idUsuario) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, new java.sql.Date(pedido.getFechaPedido().getTime()));
            ps.setString(2, pedido.getEstadoPedido());
            ps.setString(3, pedido.getDireccionEnvio());
            ps.setInt(4, pedido.getIdCliente());
            ps.setInt(5, pedido.getIdUsuario());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el pedido", e);
        }
        return 0;
    }

    public void actualizar(Pedido pedido) {
        String sql = "UPDATE pedido SET fechaPedido = ?, estadoPedido = ?, direccionEnvio = ?, idCliente = ?, idUsuario = ? WHERE idPedido = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(pedido.getFechaPedido().getTime()));
            ps.setString(2, pedido.getEstadoPedido());
            ps.setString(3, pedido.getDireccionEnvio());
            ps.setInt(4, pedido.getIdCliente());
            ps.setInt(5, pedido.getIdUsuario());
            ps.setInt(6, pedido.getIdPedido());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el pedido", e);
        }
    }
    public void insertar(Pedido pedido) {
    String sql = "INSERT INTO pedido (fechaPedido, estadoPedido, direccionEnvio, idCliente, idUsuario) VALUES (?, ?, ?, ?, ?)";
    try (Connection con = Conexion.getConnection();
         PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setDate(1, new java.sql.Date(new java.util.Date().getTime())); // Fecha actual
        ps.setString(2, pedido.getEstadoPedido());
        ps.setString(3, pedido.getDireccionEnvio());
        ps.setInt(4, pedido.getIdCliente());
        ps.setInt(5, pedido.getIdUsuario());
        ps.executeUpdate();

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                pedido.setIdPedido(rs.getInt(1));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Error al insertar el pedido", e);
    }
}

    public void eliminar(int id) {
        String sql = "DELETE FROM pedido WHERE idPedido = ?";
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el pedido", e);
        }
    }

    public List<DetallePedido> findDetallesByPedidoId(int pedidoId) {
        String sql = "SELECT dp.*, pr.nombreProducto FROM detallepedido dp JOIN producto pr ON dp.idProducto = pr.idProducto WHERE dp.idPedido = ?";
        List<DetallePedido> detalles = new ArrayList<>();
        try (Connection con = Conexion.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido detalle = new DetallePedido();
                    detalle.setIdPedido(rs.getInt("idPedido"));
                    detalle.setIdProducto(rs.getInt("idProducto"));
                    detalle.setCantidad(rs.getInt("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    
                    Producto producto = new Producto();
                    producto.setIdProducto(rs.getInt("idProducto"));
                    producto.setNombreProducto(rs.getString("nombreProducto"));
                    detalle.setProducto(producto);

                    detalles.add(detalle);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar los detalles del pedido", e);
        }
        return detalles;
    }

    private Pedido mapPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(rs.getInt("idPedido"));
        pedido.setFechaPedido(rs.getDate("fechaPedido"));
        pedido.setEstadoPedido(rs.getString("estadoPedido"));
        pedido.setDireccionEnvio(rs.getString("direccionEnvio"));
        pedido.setIdCliente(rs.getInt("idCliente"));
        pedido.setIdUsuario(rs.getInt("idUsuario"));

        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("idCliente"));
        cliente.setNombres(rs.getString("cliente_nombres"));
        cliente.setApellidos(rs.getString("cliente_apellidos"));
        pedido.setCliente(cliente);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("idUsuario"));
        usuario.setNombres(rs.getString("usuario_nombres"));
        usuario.setApellidos(rs.getString("usuario_apellidos"));
        pedido.setUsuario(usuario);

        return pedido;
        
    }
}
