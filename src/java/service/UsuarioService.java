package service;

import dao.RolDAO;
import dao.UsuarioDAO;
import model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.regex.Pattern;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Usuario login(String correo, String passwordPlano) {
        if (correo == null || passwordPlano == null) return null;
        Optional<Usuario> opt = usuarioDAO.findByCorreo(correo.trim());
        if (!opt.isPresent()) return null;
        Usuario u = opt.get();
        if (u.getEstadoUsuario() != 1) return null; // solo activos

        // comparar BCrypt
        if (BCrypt.checkpw(passwordPlano, u.getContrasenaHash())) {
            usuarioDAO.actualizarUltimoLogin(u.getId());
            return u;
        }
        return null;
    }

    public int registrar(Usuario u, String passPlano, String passConfirma, String rolNombre) {
        validarRegistro(u, passPlano, passConfirma);

        Integer idRol = rolDAO.findIdByNombre(rolNombre);
        if (idRol == null) throw new IllegalArgumentException("Rol inválido: " + rolNombre);

        // unicidad de correo y documento
        if (usuarioDAO.findByCorreo(u.getCorreo()).isPresent())
            throw new IllegalArgumentException("El correo ya está registrado");
        if (usuarioDAO.findByDocumento(u.getDocumento()).isPresent())
            throw new IllegalArgumentException("El documento ya está registrado");

        String hash = BCrypt.hashpw(passPlano, BCrypt.gensalt(10));
        u.setContrasenaHash(hash);
        u.setEstadoUsuario(1); // activo
        u.setIdRol(idRol);

        return usuarioDAO.crear(u);
    }

    private void validarRegistro(Usuario u, String pass, String confirm) {
        if (u.getDocumento() == null || u.getDocumento().trim().isEmpty())
            throw new IllegalArgumentException("Documento es obligatorio");
        if (u.getpNombre() == null || u.getpNombre().trim().isEmpty())
            throw new IllegalArgumentException("Primer nombre es obligatorio");
        if (u.getpApellido() == null || u.getpApellido().trim().isEmpty())
            throw new IllegalArgumentException("Primer apellido es obligatorio");
        if (u.getCorreo() == null || !EMAIL_RE.matcher(u.getCorreo()).matches())
            throw new IllegalArgumentException("Correo inválido");

        if (pass == null || pass.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        if (!pass.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("La contraseña debe incluir al menos una mayúscula");
        if (!pass.matches(".*[a-z].*"))
            throw new IllegalArgumentException("La contraseña debe incluir al menos una minúscula");
        if (!pass.matches(".*\\d.*"))
            throw new IllegalArgumentException("La contraseña debe incluir al menos un dígito");
        if (!pass.equals(confirm))
            throw new IllegalArgumentException("Las contraseñas no coinciden");
    }
}
