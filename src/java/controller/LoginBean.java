package controller;

import model.Usuario;
import service.UsuarioService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.io.IOException;

@ManagedBean
@SessionScoped
public class LoginBean implements Serializable {

    private String correo;
    private String password;
    private Usuario usuarioSesion;

    private final UsuarioService service = new UsuarioService();

    /**
     * Inicia sesión y guarda el usuario en sesión.
     * @return 
     */
    public String login() {
        try {
            // Validación rápida antes de ir al servicio
            if (correo == null || correo.trim().isEmpty() || password == null || password.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe ingresar correo y contraseña", null));
                return null;
            }

            Usuario u = service.login(correo.trim(), password);

            if (u == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Credenciales inválidas o usuario inactivo", null));
                return null;
            }

            this.usuarioSesion = u;

            // Guardamos en la sesión HTTP para el filtro
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuario", u);

            // Limpieza de password por seguridad
            password = null;

            // Redirigir según rol (se puede mejorar con enum o tabla de rutas)
            if (u.getIdRol() == 1) {
                return "/admin/home.xhtml?faces-redirect=true";
            } else {
                return "/home.xhtml?faces-redirect=true";
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al iniciar sesión", "Intente nuevamente más tarde."));
            return null;
        }
    }

    /**
     * Cierra la sesión y redirige al login
     * @throws java.io.IOException
     */
    public void logout() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
    }

    // Helpers
    public boolean isLogueado() { return usuarioSesion != null; }

    public String getNombreMostrado() {
        return isLogueado()
                ? usuarioSesion.getNombres() + " " + usuarioSesion.getApellidos()
                : "";
    }
    // Getters & Setters
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Usuario getUsuarioSesion() { return usuarioSesion; }
    public void setUsuarioSesion(Usuario usuarioSesion) { this.usuarioSesion = usuarioSesion; }
}
