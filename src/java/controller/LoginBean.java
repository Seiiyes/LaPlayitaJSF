package controller;

import model.Usuario;
import service.UsuarioService;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
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
     */
    public void login() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // Validación rápida
            if (correo == null || correo.trim().isEmpty() || password == null || password.isEmpty()) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe ingresar correo y contraseña", null));
                return; // usamos void, no null
            }

            Usuario u = service.login(correo.trim(), password);

            if (u == null) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Credenciales inválidas o usuario inactivo", null));
                return; // void evita error de navegación
            }

            this.usuarioSesion = u;

            // Guardar usuario en sesión
            context.getExternalContext().getSessionMap().put("usuario", u);
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            session.setMaxInactiveInterval(30 * 60); // 30 min

            password = null;

            // Redirigir según rol
            if (u.getIdRol() == 1) {
                context.getExternalContext().redirect("admin/adminHome.xhtml");
            } else {
                context.getExternalContext().redirect("home.xhtml");
            }

        } catch (IOException e) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al iniciar sesión", "Intente nuevamente más tarde."));
        } catch (Exception e) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error inesperado", e.getMessage()));
        }
    }

    /**
     * Cierra la sesión y redirige al login
     * @throws java.io.IOException
     */
    public void logout() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().invalidateSession();
        context.getExternalContext().redirect("login.xhtml");
    }

    public boolean isLogueado() {
        return usuarioSesion != null;
    }

    public String getNombreMostrado() {
        return isLogueado()
                ? usuarioSesion.getNombres() + " " + usuarioSesion.getApellidos()
                : "";
    }

    // Getters y Setters
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Usuario getUsuarioSesion() { return usuarioSesion; }
    public void setUsuarioSesion(Usuario usuarioSesion) { this.usuarioSesion = usuarioSesion; }
}
