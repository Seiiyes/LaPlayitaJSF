package filter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(true);

        // Generar token si no existe
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", UUID.randomUUID().toString());
        }

        // Validar token en POST
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String tokenSesion = (String) session.getAttribute("csrfToken");
            String tokenRequest = req.getParameter("csrfToken");

            // Si JSF renombra el campo, buscamos manualmente
            if (tokenRequest == null) {
                for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
                    if (entry.getKey().endsWith(":csrfToken")) {
                        String[] values = entry.getValue();
                        if (values != null && values.length > 0) {
                            tokenRequest = values[0];
                            break;
                        }
                    }
                }
            }

            // Validación final
            if (tokenSesion == null || tokenRequest == null || !tokenSesion.equals(tokenRequest)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token inválido.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
