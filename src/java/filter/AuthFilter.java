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
            Arrays.asList("/login.xhtml", "/registro.xhtml", "/home.xhtml", "/index.xhtml", "/recuperar.xhtml", "/resetear.xhtml")
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
            Arrays.asList("/ventas/registrar.xhtml", "/ventas/historial.xhtml", "/pedidos/gestion.xhtml", "/inventario/inventario.xhtml")
    // A medida que crees más páginas para el personal, añádelas aquí.
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.startsWith("/javax.faces.resource/")) {
            chain.doFilter(request, response);
            return;
        }

        if (PUBLIC_PAGES.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        boolean isAjax = "partial/ajax".equals(req.getHeader("Faces-Request"));

        if (usuario != null && (path.equals("/login.xhtml") || path.equals("/registro.xhtml"))) {
            String redirectUrl = req.getContextPath() + (usuario.getIdRol() == ROLE_ADMIN ? "/admin/adminHome.xhtml" : "/home.xhtml");
            handleRedirect(res, redirectUrl, isAjax);
            return;
        }

        if (usuario == null) {
            handleRedirect(res, req.getContextPath() + "/login.xhtml", isAjax);
            return;
        }

        int userRoleId = usuario.getIdRol();
        if (userRoleId == 0) {
            handleRedirect(res, req.getContextPath() + "/access-denied.xhtml", isAjax);
            return;
        }

        if (ADMIN_PAGES.contains(path)) {
            if (userRoleId == ROLE_ADMIN) {
                chain.doFilter(request, response);
            } else {
                handleRedirect(res, req.getContextPath() + "/access-denied.xhtml", isAjax);
            }
            return;
        }

        if (STAFF_PAGES.contains(path)) {
            if (userRoleId == ROLE_ADMIN || userRoleId == ROLE_VENDEDOR) {
                chain.doFilter(request, response);
            } else {
                handleRedirect(res, req.getContextPath() + "/access-denied.xhtml", isAjax);
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private void handleRedirect(HttpServletResponse res, String url, boolean isAjax)
            throws IOException {
        if (isAjax) {
            res.setContentType("text/xml;charset=UTF-8");
            res.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            res.getWriter().append("<partial-response><redirect url=\"").append(url).append("\"></redirect></partial-response>");
        } else {
            res.sendRedirect(url);
        }
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