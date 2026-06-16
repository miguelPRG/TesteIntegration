package api;

import org.junit.jupiter.api.BeforeAll;

import io.restassured.RestAssured;

public class BaseTest {

    @BeforeAll
    public static void setup() {
        // Configuração global da API antes de iniciar qualquer teste
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

}
