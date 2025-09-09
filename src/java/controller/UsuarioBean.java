package controller;

import dao.RolDAO;
import model.Usuario;
import model.Rol;
import service.UsuarioService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private List<Usuario> listaUsuarios;
    private Map<Integer, String> rolesMap;
    private List<Rol> listaRoles;

    private Usuario usuarioSeleccionado;
    private String nuevaContrasena;

    private final UsuarioService usuarioService = new UsuarioService();
    private final RolDAO rolDAO = new RolDAO();

    @PostConstruct
    public void init() {
        try {
            listaUsuarios = usuarioService.listarUsuarios();
            listaRoles = rolDAO.findAll();

            rolesMap = new HashMap<>();
            for (Rol rol : listaRoles) {
                rolesMap.put(rol.getIdRol(), rol.getDescripcionRol());
            }
            // Inicializa el objeto para evitar errores de NullPointerException en la carga inicial de la página
            this.usuarioSeleccionado = new Usuario();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar datos iniciales", e.getMessage()));
        }
    }

    public void prepararNuevoUsuario() {
        this.usuarioSeleccionado = new Usuario();
        this.nuevaContrasena = null;
    }

    public void prepararEdicion(Usuario usuario) {
        // Se clona el objeto para evitar modificar la tabla directamente antes de guardar
        this.usuarioSeleccionado = new Usuario();
        this.usuarioSeleccionado.setIdUsuario(usuario.getIdUsuario());
        this.usuarioSeleccionado.setDocumento(usuario.getDocumento());
        this.usuarioSeleccionado.setNombres(usuario.getNombres());
        this.usuarioSeleccionado.setApellidos(usuario.getApellidos());
        this.usuarioSeleccionado.setCorreo(usuario.getCorreo());
        this.usuarioSeleccionado.setTelefono(usuario.getTelefono());
        this.usuarioSeleccionado.setEstadoUsuario(usuario.getEstadoUsuario());
        this.usuarioSeleccionado.setIdRol(usuario.getIdRol());
        this.nuevaContrasena = null;
    }
    
    public void guardarCambios() {
        try {
            boolean esNuevo = (usuarioSeleccionado.getIdUsuario() == 0);

            if (esNuevo) {
                usuarioService.agregarUsuario(usuarioSeleccionado, nuevaContrasena);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Usuario agregado con éxito", null));
            } else {
                usuarioService.actualizarUsuario(usuarioSeleccionado, nuevaContrasena);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Usuario actualizado con éxito", null));
            }

            // Recargar la lista desde la BD para reflejar los cambios en la tabla
            listaUsuarios = usuarioService.listarUsuarios();

            // Limpiar el formulario y dejarlo listo para un nuevo ingreso
            prepararNuevoUsuario();

        } catch (Exception e) {
            // En caso de error, no limpiamos el formulario para que el usuario pueda corregir los datos
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage()));
        }
    }
    
    public void eliminar(Usuario u) {
        try {
            usuarioService.eliminarUsuario(u.getIdUsuario());
            listaUsuarios.remove(u);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Usuario eliminado", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo eliminar", e.getMessage()));
        }
    }

    // Getters y Setters (sin cambios)
    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public void setListaUsuarios(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public List<Rol> getListaRoles() {
        return listaRoles;
    }

    public Usuario getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(Usuario usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getDescripcionRol(int idRol) {
        return rolesMap.getOrDefault(idRol, "Desconocido");
    }
}
