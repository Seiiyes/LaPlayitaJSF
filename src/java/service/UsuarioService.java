package service;

import dao.RolDAO;
import dao.UsuarioDAO;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.regex.Pattern;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ---------------- LOGIN ----------------
    public Usuario login(String correo, String passwordPlano) {
        if (correo == null || passwordPlano == null) return null;

        Usuario u = usuarioDAO.findByCorreo(correo.trim());
        if (u == null) return null;

        if (u.getEstadoUsuario() != 1) return null; // solo usuarios activos

        if (BCrypt.checkpw(passwordPlano, u.getContrasenaHash())) {
            usuarioDAO.actualizarUltimoLogin(u.getIdUsuario());
            return u;
        }
        return null;
    }

    // ---------------- REGISTRO ----------------
    public int registrar(Usuario u, String passPlano, String passConfirma, String rolNombre) {
        validarRegistro(u, passPlano, passConfirma);

        Integer idRol = rolDAO.findIdByNombre(rolNombre);
        if (idRol == null) {
            throw new IllegalArgumentException("Rol inválido: " + rolNombre);
        }

        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(10));
        u.setContrasenaHash(hash);
        u.setEstadoUsuario(1); // activo por defecto
        u.setIdRol(idRol);

        try {
            return usuarioDAO.crear(u);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null) {
                if (msg.toLowerCase().contains("correo")) {
                    throw new IllegalArgumentException("El correo ya está registrado");
                }
                if (msg.toLowerCase().contains("documento")) {
                    throw new IllegalArgumentException("El documento ya está registrado");
                }
            }
            throw e;
        }
    }

    // ---------------- VALIDACIONES ----------------
    private void validarRegistro(Usuario u, String pass, String confirm) {
        if (u.getDocumento() == null || u.getDocumento().trim().isEmpty())
            throw new IllegalArgumentException("Documento es obligatorio");
        if (u.getNombres() == null || u.getNombres().trim().isEmpty())
            throw new IllegalArgumentException("Nombre es obligatorio");
        if (u.getApellidos() == null || u.getApellidos().trim().isEmpty())
            throw new IllegalArgumentException("Apellido es obligatorio");
        if (u.getCorreo() == null || !EMAIL_RE.matcher(u.getCorreo()).matches())
            throw new IllegalArgumentException("Correo inválido");

        if (pass == null || pass.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        if (!pass.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("Debe incluir al menos una mayúscula");
        if (!pass.matches(".*[a-z].*"))
            throw new IllegalArgumentException("Debe incluir al menos una minúscula");
        if (!pass.matches(".*\\d.*"))
            throw new IllegalArgumentException("Debe incluir al menos un dígito");
        if (!pass.equals(confirm))
            throw new IllegalArgumentException("Las contraseñas no coinciden");
    }

    // ---------------- LISTAR ----------------
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.findAll();
    }

    // ---------------- ELIMINAR ----------------
    public void eliminarUsuario(int idUsuario) {
        usuarioDAO.eliminar(idUsuario);
    }

    // ---------------- AGREGAR USUARIO ----------------
    public void agregarUsuario(Usuario u, String nuevacontrasena) {
        if (u == null) throw new IllegalArgumentException("Usuario inválido");
        if (nuevacontrasena != null && !nuevacontrasena.isEmpty()) {
            u.setContrasenaHash(BCrypt.hashpw(nuevacontrasena, BCrypt.gensalt(10)));
        } else if (u.getContrasenaHash() == null || u.getContrasenaHash().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        usuarioDAO.crear(u);
    }

    // ---------------- ACTUALIZAR USUARIO ----------------
    public void actualizarUsuario(Usuario u, String nuevaContrasena) {
        if (u == null || u.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Usuario inválido");
        }

        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
            // validación básica
            if (nuevaContrasena.length() < 8)
                throw new IllegalArgumentException("La nueva contraseña debe tener mínimo 8 caracteres");
            u.setContrasenaHash(BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt(10)));
        }

        usuarioDAO.actualizar(u);
    }
}
