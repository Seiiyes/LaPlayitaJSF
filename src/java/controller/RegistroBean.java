package controller;

import model.Usuario;
import service.UsuarioService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class RegistroBean {

    // Campos del formulario
    private String documento;
    private String pNombre;
    private String sNombre;
    private String pApellido;
    private String sApellido;
    private String correo;
    private String telefono;
    private String password;
    private String confirmarPassword;
    private String rol = "VENDEDOR"; // por defecto, o usa un selectOneMenu

    private final UsuarioService service = new UsuarioService();

    public String registrar() {
        try {
            Usuario u = new Usuario();
            u.setDocumento(documento != null ? documento.trim() : null);
            u.setpNombre(pNombre != null ? pNombre.trim() : null);
            u.setsNombre(sNombre != null ? sNombre.trim() : null);
            u.setpApellido(pApellido != null ? pApellido.trim() : null);
            u.setsApellido(sApellido != null ? sApellido.trim() : null);
            u.setCorreo(correo != null ? correo.trim() : null);
            u.setTelefono(telefono != null ? telefono.trim() : null);

            int id = service.registrar(u, password, confirmarPassword, rol);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro exitoso (ID: " + id + ")", null));
            return "index.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException ie) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, ie.getMessage(), null));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar", e.getMessage()));
            return null;
        }
    }

    // Getters & Setters
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getpNombre() { return pNombre; }
    public void setpNombre(String pNombre) { this.pNombre = pNombre; }
    public String getsNombre() { return sNombre; }
    public void setsNombre(String sNombre) { this.sNombre = sNombre; }
    public String getpApellido() { return pApellido; }
    public void setpApellido(String pApellido) { this.pApellido = pApellido; }
    public String getsApellido() { return sApellido; }
    public void setsApellido(String sApellido) { this.sApellido = sApellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
