package filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro de logging: registra accesos con usuario, IP, tiempo de ejecución y errores.
 */
@WebFilter("/*")
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No requiere inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String uri = req.getRequestURI();
        String ip = request.getRemoteAddr();
        String usuario = (session != null && session.getAttribute("usuario") != null)
                ? session.getAttribute("usuario").toString()
                : "ANONIMO";

        long inicio = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } catch (ServletException | IOException e) {
            // Log de errores
            System.out.println("[LOG][ERROR] URI: " + uri 
                    + " | IP: " + ip 
                    + " | Usuario: " + usuario 
                    + " | Error: " + e.getMessage());
            throw e; // re-lanzar para que el contenedor maneje la excepción
        } finally {
            long tiempo = System.currentTimeMillis() - inicio;
            System.out.println("[LOG] URI: " + uri 
                    + " | IP: " + ip 
                    + " | Usuario: " + usuario 
                    + " | Tiempo: " + tiempo + " ms");
        }
    }

    @Override
    public void destroy() {
        // No requiere limpieza
    }
}
