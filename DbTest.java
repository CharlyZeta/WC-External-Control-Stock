import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:file:./data/stockdual";
        String user = "sa";
        String password = "password";

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
