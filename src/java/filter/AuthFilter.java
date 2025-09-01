package filter;

import model.Usuario;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("*.xhtml")  // Se aplica a todas las páginas .xhtml
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Opcional: inicialización si necesitas
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Usuario guardado en sesión (lo setea tu LoginBean o UsuarioBean)
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");

        String reqURI = req.getRequestURI();

        // Reglas de acceso
        if (usuario == null &&
                (reqURI.contains("home.xhtml") || reqURI.contains("admin.xhtml"))) {
            // No está logueado → redirigir a login
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        } else {
            // Continuar con la request normal
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Opcional: liberar recursos
    }
}
