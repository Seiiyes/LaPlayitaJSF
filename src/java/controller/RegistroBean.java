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
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String password;
    private String confirmarPassword;
    private String rol = "VENDEDOR"; // por defecto

    private final UsuarioService service = new UsuarioService();

    /**
     * Registrar nuevo usuario
     */
    public String registrar() {
        try {
            Usuario u = new Usuario();
            u.setDocumento(documento != null ? documento.trim() : null);
            u.setNombres(nombres != null ? nombres.trim() : null);
            u.setApellidos(apellidos != null ? apellidos.trim() : null);
            u.setCorreo(correo != null ? correo.trim() : null);
            u.setTelefono(telefono != null ? telefono.trim() : null);

            int id = service.registrar(u, password, confirmarPassword, rol);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro exitoso (ID: " + id + ")", null));
            return "login.xhtml?faces-redirect=true";

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
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
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
