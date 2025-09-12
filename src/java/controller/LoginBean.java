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
import javax.servlet.http.HttpServletRequest;
@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String correo;
    private String password;
    private Usuario usuarioSesion;

    private final UsuarioService service = new UsuarioService();
    /**
     * Inicia sesión y guarda el usuario en sesión usando rutas absolutas.
     */
    public void login() {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String contextPath = request.getContextPath();

        try {
            if (correo == null || correo.trim().isEmpty() || password == null || password.isEmpty()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Debe ingresar correo y contraseña", null));
                return;
            }

            Usuario u = service.login(correo.trim(), password);

            if (u == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales inválidas o usuario inactivo", null));
                return;
            }

            this.usuarioSesion = u;
            context.getExternalContext().getSessionMap().put("usuario", u);
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            session.setMaxInactiveInterval(30 * 60);

            password = null;

            if (u.getIdRol() == 1) { // Administrador
                context.getExternalContext().redirect(contextPath + "/admin/adminHome.xhtml");
            } else { // Otros roles
                context.getExternalContext().redirect(contextPath + "/home.xhtml");
            }

        } catch (IOException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error de Redirección", "No se pudo navegar a la página de inicio."));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error Inesperado", e.getMessage()));
        }
    }

    /**
     * Cierra la sesión y redirige al login usando una ruta absoluta. (Este
     * método ya estaba correcto)
     * @throws java.io.IOException
     */
    public void logout() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String contextPath = request.getContextPath();

        context.getExternalContext().invalidateSession();
        context.getExternalContext().redirect(contextPath + "/login.xhtml");
    }

    public boolean isLogueado() {
        return usuarioSesion != null;
    }

    public String getNombreMostrado() {
        if (isLogueado()) {
            return usuarioSesion.getNombres() + " " + usuarioSesion.getApellidos();
        }
        return "";
    }

    // Getters y Setters
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public void setUsuarioSesion(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }
}
