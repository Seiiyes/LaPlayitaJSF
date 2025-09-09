package controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;

@ManagedBean
@RequestScoped
public class AdminBean implements Serializable {

    public String irUsuarios() {
        return "/admin/usuarios?faces-redirect=true";
    }

    public String irRoles() {
        // Futura implementación
        return "/admin/roles?faces-redirect=true"; 
    }

    public String irProductos() {
        // Futura implementación
        return "/admin/productos?faces-redirect=true";
    }
}