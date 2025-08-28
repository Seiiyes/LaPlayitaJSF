package controller;

import model.Usuario;
import service.UsuarioService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private String username;  // Puede ser correo o documento
    private String password;

    private final UsuarioService service = new UsuarioService();

    @PostConstruct
    public void init() {
        username = "";
        password = "";
    }

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /**
     * Login seguro del usuario
     */
    public String login() {
        try {
            Usuario u = service.login(username, password);

            if (u != null) {
                // Guardar usuario en sesión
                FacesContext.getCurrentInstance()
                        .getExternalContext().getSessionMap().put("usuario", u);

                // Redireccionar según rol
                if (u.getIdRol() == 1) { // Administrador
                    return "admin/home?faces-redirect=true";
                } else { // Vendedor u otro
                    return "home?faces-redirect=true";
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Usuario o contraseña incorrectos", null));
                return null;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL,
                            "Error al iniciar sesión", e.getMessage()));
            return null;
        }
    }

    /**
     * Cierra la sesión del usuario
     * @return 
     */
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index?faces-redirect=true";
    }

    /**
     * Retorna el usuario actualmente logueado
     * @return 
     */
    public Usuario getUsuarioLogueado() {
        return (Usuario) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuario");
    }

    /**
     * Verifica si hay usuario logueado
     * @return 
     */
    public boolean isLoggedIn() {
        return getUsuarioLogueado() != null;
    }
}
