package service;

import dao.ClienteDAO;
import model.Cliente;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ClienteService implements java.io.Serializable {

    @Inject
    private transient ClienteDAO clienteDAO;

    public List<Cliente> listarTodos() {
        // Este método simplemente llama al método correspondiente en el DAO.
        // En el futuro, se podría añadir aquí lógica de negocio adicional.
        return clienteDAO.listarTodos();
    }
}
