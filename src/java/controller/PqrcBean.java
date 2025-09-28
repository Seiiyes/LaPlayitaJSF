package controller;

import dao.ClienteDAO;
import model.Cliente;
import model.Pqrc;
import model.Usuario;
import service.PqrcService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Named
@ViewScoped
public class PqrcBean implements Serializable {

    @Inject
    private PqrcService pqrcService;
    @Inject
    private transient ClienteDAO clienteDAO;

    private Pqrc nuevaPqrc;
    private List<Pqrc> pqrcs;
    private List<String> tiposPqrc;
    private List<Cliente> clientes;
    private int idClienteSeleccionado;

    @PostConstruct
    public void init() {
        
        nuevaPqrc = new Pqrc();
        pqrcs = pqrcService.obtenerTodosPqrc();
        clientes = clienteDAO.listarTodos();
        tiposPqrc = Arrays.asList("PETICION", "QUEJA", "RECLAMO", "COMENTARIO");
    }

    public void registrar() {
        Usuario usuarioLogueado = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
        if (usuarioLogueado == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Sesión", "No se pudo identificar al usuario.");
            return;
        }

        if (idClienteSeleccionado == 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un cliente.");
            return;
        }

        try {
            nuevaPqrc.setIdCliente(idClienteSeleccionado);
            nuevaPqrc.setIdUsuario(usuarioLogueado.getIdUsuario());
            nuevaPqrc.setFecha(new Date(System.currentTimeMillis()));
            nuevaPqrc.setEstado("Pendiente");

            pqrcService.registrarPqrc(nuevaPqrc);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "PQRC registrada correctamente.");
            
            // Recargar la lista y limpiar el formulario
            pqrcs = pqrcService.obtenerTodosPqrc();
            nuevaPqrc = new Pqrc();
            idClienteSeleccionado = 0;

        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Error Crítico", "No se pudo registrar la PQRC: " + e.getMessage());
        }
    }

    public void cambiarEstado(Pqrc pqrc, String nuevoEstado) {
        try {
            String estadoParaDB = nuevoEstado.replace("_", " ");
            pqrcService.actualizarEstadoPqrc(pqrc.getIdPqrc(), estadoParaDB);
            pqrc.setEstado(estadoParaDB); // Actualizar el estado en la vista
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "El estado de la PQRC ha sido actualizado.");
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar el estado.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters

    public Pqrc getNuevaPqrc() {
        return nuevaPqrc;
    }

    public void setNuevaPqrc(Pqrc nuevaPqrc) {
        this.nuevaPqrc = nuevaPqrc;
    }

    public List<Pqrc> getPqrcs() {
        return pqrcs;
    }

    public List<String> getTiposPqrc() {
        return tiposPqrc;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public int getIdClienteSeleccionado() {
        return idClienteSeleccionado;
    }

    public void setIdClienteSeleccionado(int idClienteSeleccionado) {
        this.idClienteSeleccionado = idClienteSeleccionado;
    }

    public void limpiarFormulario() {
        nuevaPqrc = new Pqrc();
        idClienteSeleccionado = 0;
    }

    public void eliminarPqrc(Pqrc pqrc) {
        try {
            pqrcService.eliminarPqrc(pqrc.getIdPqrc());
            pqrcs.remove(pqrc);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "PQRC eliminada correctamente.");
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_FATAL, "Error Crítico", "No se pudo eliminar la PQRC: " + e.getMessage());
        }
    }

    public boolean isEsAdmin() {
        Usuario usuarioLogueado = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
        return usuarioLogueado != null && usuarioLogueado.getIdRol() == 1; // Suponiendo que 1 es el ID del rol de Administrador
    }
}
