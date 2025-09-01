package dao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.NamingException;

/**
 * Clase Conexion
 * Intenta obtener la conexión vía JNDI (pool configurado en GlassFish),
 * y si no está disponible, usa DriverManager como respaldo.
 */
public class Conexion {

    private static final String JNDI = "jdbc/PlayitaDB";

    // Configuración fallback DriverManager
    private static final String URL = "jdbc:mysql://localhost:3306/laplayita?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; // Ajustar según tu BD

    /**
     * Obtiene una conexión a la base de datos.
     *
     * @return Connection válida
     */
    public static Connection getConnection() {
        try {
            // Intentar JNDI (GlassFish)
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(JNDI);
            return ds.getConnection();
        } catch (SQLException | NamingException jndiEx) {
            // Fallback DriverManager si JNDI no disponible
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException | SQLException ex) {
                throw new RuntimeException("No se pudo obtener conexión a la BD", ex);
            }
        }
    }
}
