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
    private String rol = "VENDEDOR"; // valor por defecto

    private final UsuarioService service = new UsuarioService();

    /**
     * Registrar nuevo usuario
     * @return 
     */
    public String registrar() {
        try {
            // Validación rápida antes de ir al servicio
            if (password == null || confirmarPassword == null || !password.equals(confirmarPassword)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Las contraseñas no coinciden", null));
                return null;
            }

            Usuario u = new Usuario();
            u.setDocumento(trimOrNull(documento));
            u.setNombres(trimOrNull(nombres));
            u.setApellidos(trimOrNull(apellidos));
            u.setCorreo(trimOrNull(correo));
            u.setTelefono(trimOrNull(telefono));

            int id = service.registrar(u, password, confirmarPassword, rol);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro exitoso (ID: " + id + ")", null));

            resetForm(); // limpiamos campos
            return "login.xhtml?faces-redirect=true";

        } catch (IllegalArgumentException ie) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, ie.getMessage(), null));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al registrar", e.getMessage()));
            return null;
        } finally {
            // Seguridad: limpiar contraseñas en memoria
            password = null;
            confirmarPassword = null;
        }
    }

    // Utilidad para evitar repetir trim
    private String trimOrNull(String value) {
        return (value != null) ? value.trim() : null;
    }

    // Reset de formulario tras registrar
    private void resetForm() {
        documento = null;
        nombres = null;
        apellidos = null;
        correo = null;
        telefono = null;
        rol = "VENDEDOR";
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
