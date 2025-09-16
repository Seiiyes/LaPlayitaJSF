package filter;

import model.Usuario;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro de autenticación y control de acceso por roles a nivel de página.
 * Protege páginas restringidas según la sesión y el rol del usuario.
 */
@WebFilter("*.xhtml")
public class AuthFilter implements Filter {

    // Páginas completamente públicas (no requieren login)
    private static final Set<String> PUBLIC_PAGES = new HashSet<>(
            Arrays.asList("/login.xhtml", "/registro.xhtml", "/home.xhtml", "/index.xhtml")
    );

    // IDs de Roles para una comprobación más robusta y segura
    private static final int ROLE_ADMIN = 1;
    private static final int ROLE_VENDEDOR = 2;
    // private static final int ROLE_PRACTICANTE = 3; // Descomentar si se usa

    // Páginas que solo los Admins (Rol 1) pueden ver
    private static final Set<String> ADMIN_PAGES = new HashSet<>(
            Arrays.asList("/admin/usuarios.xhtml", "/compras/gestion.xhtml", "/pqrc/gestion.xhtml", "/admin/adminHome.xhtml")
    // A medida que crees más páginas de admin, añádelas aquí.
    );

    // Páginas que el personal (Admins Rol 1 y Vendedores Rol 2) pueden ver
    private static final Set<String> STAFF_PAGES = new HashSet<>(
            Arrays.asList("/ventas/registrar.xhtml", "/ventas/historial.xhtml", "/pedidos/gestion.xhtml")
    // A medida que crees más páginas para el personal, añádelas aquí.
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Obtenemos la ruta relativa a la aplicación (ej. /login.xhtml)
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Permitir acceso a recursos de JSF (CSS, JS, imágenes)
        if (path.startsWith("/javax.faces.resource/")) {
            chain.doFilter(request, response);
            return;
        }

        // Si la página es pública, permitir el acceso a todos
        if (PUBLIC_PAGES.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        // Si la página no es pública y no hay un usuario logueado, redirigir al login
        if (usuario == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        // Si llegamos aquí, el usuario ESTÁ logueado. Ahora verificamos sus permisos.
        int userRoleId = usuario.getIdRol();

        // Si el ID del rol es 0, significa que no se asignó correctamente.
        if (userRoleId == 0) {
            res.sendRedirect(req.getContextPath() + "/access-denied.xhtml");
            return;
        }

        // Regla 1: ¿La página es exclusiva para Admins?
        if (ADMIN_PAGES.contains(path)) {
            if (userRoleId == ROLE_ADMIN) {
                chain.doFilter(request, response); // Es Admin, tiene acceso
            } else {
                res.sendRedirect(req.getContextPath() + "/access-denied.xhtml"); // No es Admin, acceso denegado
            }
            return;
        }

        // Regla 2: ¿La página es para el personal (Admin o Vendedor)?
        if (STAFF_PAGES.contains(path)) {
            if (userRoleId == ROLE_ADMIN || userRoleId == ROLE_VENDEDOR) {
                chain.doFilter(request, response); // Es Admin o Vendedor, tiene acceso
            } else {
                res.sendRedirect(req.getContextPath() + "/access-denied.xhtml"); // No tiene el rol adecuado, acceso denegado
            }
            return;
        }

        // Si la página no está en ninguna lista de protección, permitimos el acceso
        // (Esto es útil para páginas como un "dashboard" de vendedor que no son públicas pero son para todos los roles logueados)
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No se requiere inicialización
    }

    @Override
    public void destroy() {
        // No se requiere limpieza
    }
}
