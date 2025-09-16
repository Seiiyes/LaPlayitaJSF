package controller;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import model.Producto;
import model.Proveedor;
import model.Reabastecimiento;
import service.ReabastecimientoService;

@Named("reabastecimientoBean")
@ViewScoped
public class ReabastecimientoBean implements Serializable {

    @Inject
    private ReabastecimientoService service;

    private List<Reabastecimiento> listaReabastecimientos;
    private List<Reabastecimiento> listaFiltrada; // Para el filtro de la tabla
    private List<Proveedor> listaProveedores;
    private List<Producto> listaProductos;

    private Reabastecimiento seleccionado;

    @PostConstruct
    public void init() {
        try {
            listaReabastecimientos = service.listar();
            listaProveedores = service.listarProveedores();
            listaProductos = service.listarProductos();
            prepararNuevo();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los datos iniciales."));
            e.printStackTrace();
        }
    }

    public void prepararNuevo() {
        seleccionado = new Reabastecimiento();
    }

    public void prepararEdicion(Reabastecimiento item) {
        this.seleccionado = item;
        // Asignar IDs para que los selectOneMenu se pre-seleccionen
        this.seleccionado.setIdProveedor(item.getProveedor().getIdProveedor());
        this.seleccionado.setIdProducto(item.getProducto().getIdProducto());
    }

    public void guardar() {
        try {
            service.guardar(seleccionado);
            
            // Recargar la lista y limpiar el formulario
            listaReabastecimientos = service.listar();
            prepararNuevo();
            
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro guardado correctamente."));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al guardar."));
            e.printStackTrace();
        }
    }

    public void eliminar(Reabastecimiento item) {
        try {
            service.eliminar(item.getIdReabastecimiento());
            listaReabastecimientos.remove(item); // Optimización para no recargar toda la lista
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Registro eliminado."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el registro."));
            e.printStackTrace();
        }
    }

    // --- Getters y Setters ---

    public List<Reabastecimiento> getListaReabastecimientos() {
        return listaReabastecimientos;
    }

    public void setListaReabastecimientos(List<Reabastecimiento> listaReabastecimientos) {
        this.listaReabastecimientos = listaReabastecimientos;
    }

    public List<Reabastecimiento> getListaFiltrada() {
        return listaFiltrada;
    }

    public void setListaFiltrada(List<Reabastecimiento> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

    public List<Proveedor> getListaProveedores() {
        return listaProveedores;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public Reabastecimiento getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(Reabastecimiento seleccionado) {
        this.seleccionado = seleccionado;
    }
}