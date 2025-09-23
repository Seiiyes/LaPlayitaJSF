package controller;

import model.Usuario;
import service.UsuarioService;
import service.EmailService;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

@Named("recuperarClaveBean")
@ViewScoped
public class RecuperarClaveBean implements Serializable {

    private String email;
    private String token;
    private String nuevaContrasena;
    private boolean tokenValido;

    @Inject
    private UsuarioService usuarioService;

    private EmailService emailService = new EmailService();

    public void solicitarReinicio() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Usuario usuario = usuarioService.buscarPorEmail(email);
            if (usuario != null) {
                String resetToken = UUID.randomUUID().toString();
                long expiryTime = System.currentTimeMillis() + (24 * 3600 * 1000); // 24 horas de validez
                Timestamp expiryDate = new Timestamp(expiryTime);

                usuarioService.guardarTokenReinicio(usuario.getIdUsuario(), resetToken, expiryDate);

                // Construir URL de reinicio
                HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
                String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/resetear.xhtml?token=" + resetToken;

                emailService.enviarCorreoReinicio(email, url);

                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correo enviado", "Si el correo está registrado, recibirás un enlace para reiniciar tu contraseña."));
            } else {
                // Por seguridad, no revelar si el correo existe o no
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correo enviado", "Si el correo está registrado, recibirás un enlace para reiniciar tu contraseña."));
            }
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo procesar la solicitud. Inténtalo de nuevo."));
        }
    }

    public void validarToken() {
        FacesContext context = FacesContext.getCurrentInstance();
        this.tokenValido = false;
        if (token == null || token.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Token inválido", "El enlace no contiene un token válido."));
            return;
        }
        Usuario usuario = usuarioService.buscarPorToken(token);
        if (usuario == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Token inválido", "El enlace para restablecer la contraseña no es válido."));
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (usuario.getResetTokenExpiry() == null || usuario.getResetTokenExpiry().before(now)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Token expirado", "El enlace para restablecer la contraseña ha expirado."));
            return;
        }
        this.tokenValido = true;
    }

    public String resetearClave() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (!tokenValido || token == null || token.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La sesión de reinicio no es válida."));
            return null;
        }

        try {
            usuarioService.resetearClave(token, nuevaContrasena);
            // Limpiar token y estado tras éxito
            this.tokenValido = false;
            this.token = null;
            this.nuevaContrasena = null;
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Tu contraseña ha sido actualizada. Ya puedes iniciar sesión."));
            return "/login.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar la contraseña."));
            return null;
        }
    }

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNuevaContrasena() { return nuevaContrasena; }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }
    public boolean isTokenValido() { return tokenValido; }
    public void setTokenValido(boolean tokenValido) { this.tokenValido = tokenValido; }
}