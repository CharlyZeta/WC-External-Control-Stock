import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Archivo de prueba para conexión con base de datos.
 * ADVERTENCIA DE SEGURIDAD: Nunca commitear credenciales reales.
 * Usar variables de entorno para las credenciales de BD.
 */
public class DbTest {
    public static void main(String[] args) throws Exception {
        // CONFIGURACIÓN: Usar variables de entorno en producción
        String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://localhost:5433/stockdual";
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "stockdual";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "TU_PASSWORD";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sku, stock_wc, image_url, wc_category FROM product LIMIT 5")) {

            while (rs.next()) {
                System.out.println("SKU: " + rs.getString("sku")
                    + " | Stock WC: " + rs.getInt("stock_wc")
                    + " | IMG: " + rs.getString("image_url")
                    + " | CAT: " + rs.getString("wc_category"));
            }
        }
    }
}
