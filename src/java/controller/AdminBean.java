package controller;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@ManagedBean
@SessionScoped
public class AdminBean implements Serializable {

    // Métodos para navegar a las páginas de administración
    public String irUsuarios() {
        return "/admin/usuarios?faces-redirect=true";
    }

    public String irRoles() {
        return "/admin/roles?faces-redirect=true";
    }

    public String irProductos() {
        return "/admin/productos?faces-redirect=true";
    }
}
