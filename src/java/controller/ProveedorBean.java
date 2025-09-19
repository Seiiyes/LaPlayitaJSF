package controller;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import model.Proveedor;
import service.ProveedorService;

@Named("proveedorBean")
@ViewScoped
public class ProveedorBean implements Serializable {

    @Inject
    private ProveedorService service;

    private Proveedor proveedor;

    @PostConstruct
    public void init() {
        proveedor = new Proveedor();
    }

    public void guardar() {
        try {
            service.guardar(proveedor);
            // Limpiar el objeto para el siguiente uso
            init(); 
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Proveedor guardado correctamente."));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage("formProveedor", 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("formProveedor", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al guardar el proveedor."));
            e.printStackTrace();
        }
    }

    // --- Getters y Setters ---

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}
