import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WcTest {
    public static void main(String[] args) throws Exception {
        String urlString = "http://localhost:10014/wp-json/wc/v3/products?sku=16462791483863";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        String auth = "ck_ce4401bb74dc7ea7a6ac4869c6fc96c2162aa773:cs_9c2afbf1cbe2f87ee157b2e9dff783777f98555e";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        
        System.out.println("Response Code: " + conn.getResponseCode());
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        System.out.println(response.toString());
    }
}
