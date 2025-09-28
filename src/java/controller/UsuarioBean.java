package controller;

import dao.RolDAO;
import model.Usuario;
import model.Rol;
import javax.inject.Inject; // CDI: Importar Inject
import service.UsuarioService;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Named; // CDI: Reemplaza a ManagedBean
import javax.faces.view.ViewScoped; // CDI: Usar este ViewScoped
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("usuarioBean") // CDI: Anotación estándar para nombrar un bean
@ViewScoped
public class UsuarioBean implements Serializable {

    private List<Usuario> listaUsuarios;
    private Map<Integer, String> rolesMap;
    private List<Rol> listaRoles;

    private Usuario usuarioSeleccionado;
    private String nuevaContrasena;

    // CDI: Inyecta las dependencias en lugar de crearlas manualmente.
    // Para que esto funcione, UsuarioService y RolDAO deben ser beans de CDI
    // (ej. anotados con @ApplicationScoped o @RequestScoped).
    @Inject
    private UsuarioService usuarioService;
        @Inject
    private transient RolDAO rolDAO;

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
        // Usamos el constructor de copia para clonar el objeto de forma segura.
        // Esto evita modificar la fila de la tabla visualmente antes de guardar.
        this.usuarioSeleccionado = new Usuario(usuario);
        this.nuevaContrasena = null;
    }
    
    public void guardarCambios() {
        boolean guardadoExitosamente = false;
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
            guardadoExitosamente = true;

        } catch (IllegalArgumentException e) {
            // Error de validación de negocio (ej: correo duplicado). Mostramos el mensaje específico.
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", e.getMessage()));
        } catch (Exception e) {
            // Error inesperado del sistema. No limpiamos el formulario para que el usuario pueda corregir.
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error inesperado al guardar", "Ocurrió un error. Revise los logs."));
            // Es una buena práctica registrar el error completo en el log del servidor para diagnóstico.
            // Para un proyecto real, usa un framework de logging como SLF4J o Log4j.
            e.printStackTrace();
        }
        
        // Para un mejor control en la vista (especialmente con diálogos de PrimeFaces),
        // enviamos un parámetro de vuelta al cliente AJAX para saber si la operación fue exitosa.
        PrimeFaces.current().ajax().addCallbackParam("guardado", guardadoExitosamente);
    }
    
    public void eliminar(Usuario u) {
        try {
            usuarioService.eliminarUsuario(u.getIdUsuario());
            listaUsuarios.remove(u);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Usuario eliminado", null));
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No se pudo eliminar", e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error inesperado al eliminar", "Contacte al administrador."));
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
