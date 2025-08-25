package dao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {

    private static final String JNDI = "jdbc/PlayitaDS"; // Recurso en GlassFish
    // Fallback directo (solo si no hay JNDI; opcional)
    private static final String URL = "jdbc:mysql://localhost:3306/playita?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; // <-- si usas clave, ponla

    public static Connection getConnection() {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(JNDI);
            return ds.getConnection();
        } catch (Exception ignore) {
            // Fallback a DriverManager (útil en pruebas locales)
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(URL, USER, PASS);
            } catch (Exception e) {
                throw new RuntimeException("No se pudo obtener conexión a BD", e);
            }
        }
    }
}