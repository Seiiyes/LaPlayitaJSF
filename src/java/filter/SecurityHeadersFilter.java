package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro de cabeceras de seguridad HTTP.
 * Aplica a todas las peticiones.
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No requiere inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;

        // Cabeceras de seguridad
        res.setHeader("X-Content-Type-Options", "nosniff");  
        res.setHeader("X-Frame-Options", "DENY");            
        res.setHeader("X-XSS-Protection", "1; mode=block");  
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // HTTPS obligatorio (aplica si el servidor corre bajo HTTPS)
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Política de contenido (CSP) ajustada para JSF/PrimeFaces
                res.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                "img-src 'self' data:; " +
                "font-src 'self' data: https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                "connect-src 'self' https://cdn.jsdelivr.net; " +
                "frame-ancestors 'none';");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No requiere limpieza
    }
}
