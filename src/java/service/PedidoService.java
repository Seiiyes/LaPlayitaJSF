package service;

import dao.PedidoDAO;
import model.Pedido;
import model.DetallePedido;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ApplicationScoped
public class PedidoService implements Serializable {

    @Inject
    private transient PedidoDAO pedidoDAO;

    public List<Pedido> listarTodos() {
        return pedidoDAO.findAll();
    }

    public Pedido getPedidoConDetalles(int id) {
        Pedido pedido = pedidoDAO.findById(id);
        if (pedido != null) {
            List<DetallePedido> detalles = pedidoDAO.findDetallesByPedidoId(id);
            pedido.setDetalles(detalles);
        }
        return pedido;
    }

    public int crearPedido(Pedido pedido) {
        // Lógica de negocio para crear un pedido
        return pedidoDAO.crear(pedido);
    }

    public void actualizarPedido(Pedido pedido) {
        // Lógica de negocio para actualizar un pedido
        pedidoDAO.actualizar(pedido);
    }

    public void eliminarPedido(int id) {
        // Lógica de negocio para eliminar un pedido
        pedidoDAO.eliminar(id);
    }
}
