import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;

import java.io.FileReader;
import java.io.IOException;

public class TestConfig {

    @BeforeAll
    public static void setUp() {
        // Carrega as variáveis de ambiente do Postman
        loadVariablesFromJson("Localhost.postman_environment.json");

        // Restante do seu código de configuração
        // ...

    }

    private static void loadVariablesFromJson(String filePath) {
        try {
            JSONObject jsonObject = new JSONObject(new FileReader(filePath));
            JSONArray values = jsonObject.getJSONArray("values");
            for (int i = 0; i < values.length(); i++) {
                JSONObject variable = values.getJSONObject(i);
                String key = variable.getString("key");
                String value = variable.getString("value");
                System.setProperty(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
