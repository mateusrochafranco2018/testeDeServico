import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UsuarioTests {

    private static String generatedEmail;
    private static String generatedUserId;
    private static String userToken;

    @BeforeAll
    public static void setUp() {
        baseURI = "http://localhost:3000";
        Map<String, String> userInfo = testCriarUsuario();
        generatedEmail = userInfo.get("email");
        generatedUserId = userInfo.get("_id");
        userToken = testLoginUsuarioAleatorio(generatedEmail);
    }

    private static Map<String, String> testCriarUsuario() {
        Faker faker = new Faker();
        String nomeUsuario = faker.name().fullName();
        String emailUsuario = nomeUsuario.replaceAll("\\s+", "").toLowerCase() + "@aula.com.br";

        String requestBody = "{\n" +
                "  \"nome\": \"" + nomeUsuario + "\",\n" +
                "  \"email\": \"" + emailUsuario + "\",\n" +
                "  \"password\": \"senhaaleatoria\",\n" +
                "  \"administrador\": \"true\"\n" +
                "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/usuarios")
                .then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"))
                .extract()
                .response();

        generatedUserId = response.jsonPath().getString("_id");

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", emailUsuario);
        userInfo.put("_id", generatedUserId);
        userInfo.put("authorization", response.header("authorization"));

        return userInfo;
    }


    private static String testLoginUsuarioAleatorio(String email) {
        String loginBody = "{\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"password\": \"senhaaleatoria\"\n" +
                "}";

        Response loginResponse = given()
                .contentType(ContentType.JSON)
                .body(loginBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        return loginResponse.jsonPath().getString("authorization");
    }

    @Test
    public void buscarTodosUsuarios() {
        Response response = RestAssured
                .given()
                .baseUri(baseURI)
                .when()
                .get("/usuarios")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, String>> users = response.jsonPath().getList("usuarios");

        if (!users.isEmpty()) {
            for (Map<String, String> user : users) {
                System.out.println("ID: " + user.get("_id"));
                System.out.println("Nome: " + user.get("nome"));
                System.out.println("E-mail: " + user.get("email"));
                System.out.println("Senha: " + user.get("password"));
                System.out.println("Administrador: " + user.get("administrador"));
                System.out.println("---------------------------");
            }
        } else {
            System.out.println("Nenhum usuário encontrado na resposta.");
        }
        System.out.println(response.then().log().all()); // Print the response details
    }




    private static String criarProduto(String userToken) {
        Faker faker = new Faker();

        String productName = faker.commerce().productName();


        String productDescription = faker.lorem().sentence();
        int productPrice = faker.number().numberBetween(1, 100); // Gera um número aleatório entre 1 e 100
        int productQuantity = faker.number().numberBetween(10, 1000); // Gera um número aleatório entre 10 e 1000




        String requestBody = "{\n" +
                "  \"nome\": \"" + productName + "\",\n" +
                "  \"preco\": " + productPrice + ",\n" +
                "  \"descricao\": \"" + productDescription + "\",\n" +
                "  \"quantidade\": " + productQuantity + "\n" +
                "}";

        RequestSpecification requestSpec = given()
                .contentType(ContentType.JSON)
                .header("Authorization", userToken) // Using the user token
                .body(requestBody);
        Response response = requestSpec
                .when()
                .post("/produtos");

        response.then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"));
        String productId = response.path("_id");
        return productId;
    }

    public String testCriarCarrinho(String productId) {
        Faker faker = new Faker();
        int productQuantity = faker.number().numberBetween(1, 5);

        String requestBody = "{\n" +
                "  \"produtos\": [\n" +
                "    {\n" +
                "      \"idProduto\": \"" + productId + "\",\n" +
                "      \"quantidade\": " + productQuantity + "\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        RequestSpecification requestSpec = given()
                .baseUri(baseURI)
                .contentType(ContentType.JSON)
                .header("Authorization", userToken)
                .body(requestBody);

        System.out.println(requestSpec.log().all());

        Response response = requestSpec
                .when()
                .post("/carrinhos");
        response.then()
                .statusCode(201)
                .body("message", equalTo("Cadastro realizado com sucesso"));
        return response.path("_id");
    }

    private void testExcluirUsuario(String userId) {
        System.out.println("---------------------");
        System.out.println(userId);
        RequestSpecification requestSpec = RestAssured
                .given()
                .baseUri(baseURI);

        System.out.println(requestSpec.log().all());

        Response response = requestSpec
                .when()
                .delete("/usuarios/" + userId)
                .then()
                .statusCode(400)
                .body("message", equalTo("Não é permitido excluir usuário com carrinho cadastrado"))
                .extract()
                .response();
    }

    @Test
    public void criarUsuario() {
        String userId = testCriarUsuario().toString(); // Armazene o ID retornado
        System.out.println("Novo usuário criado com ID: " + userId);
    }

    @Test
    public void testLogin() {
        // Já temos o email e o token armazenados, basta usá-los no método de login
        testLoginUsuarioAleatorio(generatedEmail);
        System.out.println(generatedUserId);
    }

    @Test
    public void testCriarProduto() {
        criarProduto(userToken); // Supondo que 'userToken' seja uma variável estática com o token de autorização
        System.out.println(generatedUserId);
    }

    @Test
    public void testCriarCarrinhoComProdutoExistente() {
        String productId = criarProduto(userToken); // Create a product and get its ID
        testCriarCarrinho(productId); // Pass the product ID to the testCriarCarrinho method
        System.out.println(generatedUserId);
        System.out.println(productId);
    }

    @Test
    public void testExcluirUsuarioCriado() {
    testExcluirUsuario("wyNY4FFumbzxWyEN");
    }

    // Your other test methods



}
