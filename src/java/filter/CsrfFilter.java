package filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("*.xhtml") // Add annotation to map to JSF requests
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_SESSION_ATTR_NAME = "CSRF_TOKEN";
    public static final String CSRF_TOKEN_REQUEST_PARAM_NAME = "CSRF_TOKEN";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No se necesita inicialización especial
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(true);

        // 1. Generar un token si no existe uno en la sesión.
        if (session.getAttribute(CSRF_TOKEN_SESSION_ATTR_NAME) == null) {
            String token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_TOKEN_SESSION_ATTR_NAME, token);
            System.out.println("[CSRF Filter] Generated new token for session " + session.getId() + ": " + token);
        }

        // Permitir acceso a recursos de JSF (CSS, JS, imágenes) que pueden ser POST
        if (request.getRequestURI().startsWith(request.getContextPath() + "/javax.faces.resource/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Validar el token en todas las peticiones POST.
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR_NAME);
            String requestToken = request.getParameter(CSRF_TOKEN_REQUEST_PARAM_NAME);

            System.out.println("[CSRF Filter] Validating POST to " + request.getRequestURI());
            System.out.println("[CSRF Filter] Session Token: " + sessionToken);
            System.out.println("[CSRF Filter] Request Token: " + requestToken);

            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                System.err.println("[CSRF Filter] TOKEN MISMATCH! Aborting request.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token inválido.");
                return; // Detiene la ejecución si el token es inválido.
            } else {
                System.out.println("[CSRF Filter] Token match. Proceeding.");
            }
        }

        // Si no es POST o el token es válido, continuar con la petición.
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No se necesitan acciones de destrucción.
    }
}