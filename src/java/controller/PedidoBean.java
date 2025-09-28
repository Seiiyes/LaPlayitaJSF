package controller;

import model.Pedido;
import model.Cliente;
import service.PedidoService;
import dao.ClienteDAO;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("pedidoBean")
@ViewScoped
public class PedidoBean implements Serializable {

    @Inject
    private PedidoService pedidoService;

        @Inject
    private transient ClienteDAO clienteDAO;

    @Inject
    private LoginBean loginBean;

    private List<Pedido> pedidos;
    private Pedido selectedPedido;
    private List<Cliente> clientes;

    @PostConstruct
    public void init() {
        pedidos = pedidoService.listarTodos();
        clientes = clienteDAO.listarTodos();
        System.out.println("Clientes cargados: " + (clientes != null ? clientes.size() : 0));
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public Pedido getSelectedPedido() {
        return selectedPedido;
    }

    public void setSelectedPedido(Pedido selectedPedido) {
        this.selectedPedido = selectedPedido;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void openNew() {
        this.selectedPedido = new Pedido();
        this.selectedPedido.setCliente(new Cliente());
        this.selectedPedido.setFechaPedido(new java.util.Date()); // Inicializar con la fecha actual
        this.selectedPedido.setEstadoPedido("solicitado"); // Estado inicial
    }

    public void save() {
        if (this.selectedPedido.getCliente() != null) {
            this.selectedPedido.setIdCliente(this.selectedPedido.getCliente().getIdCliente());
        }
        // Set the idUsuario from the logged-in user
        if (loginBean.isLogueado() && loginBean.getUsuarioSesion() != null) {
            this.selectedPedido.setUsuario(loginBean.getUsuarioSesion());
            this.selectedPedido.setIdUsuario(loginBean.getUsuarioSesion().getIdUsuario());
        }

        if (this.selectedPedido.getIdPedido() == 0) {
            pedidoService.crearPedido(this.selectedPedido);
        } else {
            pedidoService.actualizarPedido(this.selectedPedido);
        }
        pedidos = pedidoService.listarTodos();
    }

    public void delete() {
        pedidoService.eliminarPedido(this.selectedPedido.getIdPedido());
        this.selectedPedido = null;
        pedidos = pedidoService.listarTodos();
    }
}