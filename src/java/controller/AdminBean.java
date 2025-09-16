package controller;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * Backing bean para gestionar la navegación desde el panel de administrador.
 */
@Named("adminBean")
@RequestScoped
public class AdminBean implements Serializable {

    public String irUsuarios() {
        return "/admin/usuarios?faces-redirect=true";
    }

    public String irCompras() {
        return "/compras/gestion?faces-redirect=true";
    }
    
    public String irProductos() {
        return "#"; // Deshabilitado por ahora
    }

    public String irRoles() {
        return "#"; // Deshabilitado por ahora
    }
}