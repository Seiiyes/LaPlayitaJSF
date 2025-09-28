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
        System.out.println("--- INTENTO DE GUARDAR NUEVO PROVEEDOR ---");
        try {
            service.guardar(proveedor);
            // Limpiar el objeto para el siguiente uso
            init(); 
            System.out.println("--- PROVEEDOR GUARDADO CON ÉXITO ---");
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Proveedor guardado correctamente."));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage("formProveedor:proveedorMessages", 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (java.sql.SQLException e) {
            String msg;
            if (e.getSQLState().startsWith("23")) { // Código para violación de integridad (ej. unique constraint)
                msg = "Ya existe un proveedor con ese correo o teléfono.";
            } else {
                msg = "Error de base de datos al intentar guardar el proveedor.";
            }
            FacesContext.getCurrentInstance().addMessage("formProveedor:proveedorMessages", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
            e.printStackTrace(); // Mantener para el log del servidor
        } catch (Exception e) {
                        FacesContext.getCurrentInstance().addMessage("formProveedor:proveedorMessages", 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error inesperado al guardar el proveedor: " + e.getMessage()));
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