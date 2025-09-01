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

    private final UsuarioService usuarioService = new UsuarioService();
    private final RolDAO rolDAO = new RolDAO();

    // Campo temporal para nueva contraseña
    private String nuevaContrasena;

    @PostConstruct
    public void init() {
        try {
            // Cargar usuarios
            listaUsuarios = usuarioService.listarUsuarios();

            // Cargar roles en un mapa para acceso rápido
            rolesMap = new HashMap<>();
            for (Rol rol : rolDAO.findAll()) {
                rolesMap.put(rol.getIdRol(), rol.getDescripcionRol());
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al cargar usuarios o roles", e.getMessage()));
        }
    }

    // Eliminar usuario
    public void eliminar(Usuario u) {
        try {
            usuarioService.eliminarUsuario(u.getIdUsuario());
            listaUsuarios.remove(u);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Usuario eliminado", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se pudo eliminar", e.getMessage()));
        }
    }

    // Obtener descripción del rol de manera eficiente
    public String getDescripcionRol(int idRol) {
        return rolesMap.getOrDefault(idRol, "Desconocido");
    }

    // Agregar usuario
    public void agregarUsuario(Usuario u) {
        try {
            usuarioService.agregarUsuario(u, nuevaContrasena);
            listaUsuarios.add(u);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Usuario agregado", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se pudo agregar el usuario", e.getMessage()));
        }
    }

    // Actualizar usuario
    public void actualizarUsuario(Usuario u) {
        try {
            // Pasamos nuevaContrasena; si es null, no se cambia
            usuarioService.actualizarUsuario(u, nuevaContrasena);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Usuario actualizado", null));
            // Limpiar contraseña temporal
            nuevaContrasena = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "No se pudo actualizar el usuario", e.getMessage()));
        }
    }

    // Getters y Setters
    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public void setListaUsuarios(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
}
