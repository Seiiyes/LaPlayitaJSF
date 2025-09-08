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
 * Filtro de autenticación y control de acceso.
 * Protege páginas restringidas según sesión y rol.
 */
@WebFilter("*.xhtml")
public class AuthFilter implements Filter {

    // Páginas públicas que no requieren autenticación
    private static final Set<String> PUBLIC_PAGES = new HashSet<>(
        Arrays.asList("login.xhtml", "registro.xhtml", "index.xhtml", "home.xhtml")
    );

    @Override
    public void init(FilterConfig filterConfig) {
        // No requiere inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false); // No crea sesión si no existe
        Usuario usuario = (session != null) ? (Usuario) session.getAttribute("usuario") : null;

        String reqURI = req.getRequestURI();
        boolean esRecursoEstatico = reqURI.contains("javax.faces.resource");

        boolean esPaginaPublica = false;
        for (String page : PUBLIC_PAGES) {
            if (reqURI.contains(page)) {
                esPaginaPublica = true;
                break;
            }
        }

        // Permitir recursos estáticos y páginas públicas
        if (esRecursoEstatico || esPaginaPublica) {
            chain.doFilter(request, response);
            return;
        }

        // Redirigir si no hay usuario autenticado
        if (usuario == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        // Redirigir si el usuario no tiene rol de administrador y accede a /admin/
        if (reqURI.startsWith(req.getContextPath() + "/admin/") && usuario.getIdRol() != 1) {
            res.sendRedirect(req.getContextPath() + "/home.xhtml");
            return;
        }

        // Todo correcto: continuar con la petición
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No requiere limpieza
    }
}
