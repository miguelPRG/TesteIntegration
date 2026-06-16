package api.negativos;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import api.BaseTest;
import api.classes.Book;
import api.classes.BookStatus;
import static api.helpers.ApiActions.apagarLivro;
import static api.helpers.ApiActions.apagarMembro;
import static api.helpers.ApiActions.criarLivro;
import static api.helpers.ApiActions.criarMembro;
import static api.helpers.ApiActions.criarReserva;
import static api.helpers.DadosTesteFactory.criarLivroComDatatypesInvalidos;
import static api.helpers.DadosTesteFactory.criarLivroComStatusInvalido;
import static api.helpers.DadosTesteFactory.criarLivroValido;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static api.helpers.DadosTesteFactory.gerarIsbnValido;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Order(2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookTest extends BaseTest {

    private static Map<String, Object> livroComDatatypesInvalidos;

    private Integer livroParaTesteId;
    private Integer livroComReservaAtivaId;
    private Integer membroParaTesteId;
    private String isbnParaTeste;
    private Map<String, Object> livroComStatusInvalido;

    private int obterOrdemTeste(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(method -> method.getAnnotation(Order.class))
            .map(Order::value)
            .orElse(0);
    }

    @BeforeAll
    static void prepararDadosParaTestes() {
        // Isto trata-se do payload do livro com datatypes inválidos. Não criamos nada na base de dados ainda.
        livroComDatatypesInvalidos = criarLivroComDatatypesInvalidos();
    }

    @BeforeEach
    void prepararDadosParaTestesNaoCreate(TestInfo testInfo) {
        int ordemTeste = obterOrdemTeste(testInfo);

        if (ordemTeste == 9 || ordemTeste == 16 || ordemTeste == 17) {
            isbnParaTeste = gerarIsbnValido();
            livroParaTesteId = criarLivro(criarLivroValido(isbnParaTeste, "Livro antes da atualização"));
            livroComStatusInvalido = criarLivroComStatusInvalido(isbnParaTeste);
        }
    }

    @AfterEach
    void limparDadosCriados() {
        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId);
            livroParaTesteId = null;
        }

        if (livroComReservaAtivaId != null) {
            apagarLivro(livroComReservaAtivaId);
            livroComReservaAtivaId = null;
        }

        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
            membroParaTesteId = null;
        }
    }

    private Response criarLivroInvalido(Object livroInvalido) {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(livroInvalido)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            apagarLivro(response.as(Integer.class));
        }

        return response;
    }

    @Test
    @Order(8)
    @DisplayName("CT008 - Criar livro com campos de datatype inválidos deve falhar")
    public void deveFalharAoCriarLivroComCamposDatatypeInvalidos() {
        Response response = criarLivroInvalido(livroComDatatypesInvalidos);

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(9)
    @DisplayName("CT009 - Criar e atualizar livro sem body deve falhar")
    public void deveFalharAoCriarEAtualizarLivroSemBody() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/book")
        .then()
            .statusCode(400);

        given()
            .contentType(ContentType.JSON)
        .when()
            .put("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @Order(10)
    @DisplayName("CT010 - Criar livro com campos vazios ou null deve falhar")
    public void deveFalharAoCriarLivroComCamposVaziosOuNull() {
        Book livroComTituloVazio = criarLivroValido(gerarIsbnValido(), "Livro com título vazio");
        livroComTituloVazio.setTitle("");

        Response response = criarLivroInvalido(livroComTituloVazio);

        assertEquals(400, response.statusCode(), "Título vazio deveria falhar");

        Book livroComTituloEmBranco = criarLivroValido(gerarIsbnValido(), "Livro com título em branco");
        livroComTituloEmBranco.setTitle("   ");

        response = criarLivroInvalido(livroComTituloEmBranco);

        assertEquals(400, response.statusCode(), "Título em branco deveria falhar");

        Book livroComIsbnNull = criarLivroValido(gerarIsbnValido(), "Livro com ISBN null");
        livroComIsbnNull.setIsbn(null);

        response = criarLivroInvalido(livroComIsbnNull);

        assertEquals(400, response.statusCode(), "ISBN null deveria falhar");
    }

    @Test
    @Order(11)
    @DisplayName("CT011 - Criar livro com status inválido")
    public void deveFalharAoCriarLivroComStatusInvalido() {
        isbnParaTeste = gerarIsbnValido();
        livroComStatusInvalido = criarLivroComStatusInvalido(isbnParaTeste);

        Response response = criarLivroInvalido(livroComStatusInvalido);

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(12)
    @DisplayName("CT012 - Criar livro com ano inválido deve falhar")
    public void deveFalharAoCriarLivroComAnoInvalido() {
        Book livroInvalido = new Book(
            "Título de Teste",
            "Autor de Teste",
            "Editora Teste",
            -2024,
            "1",
            "Descrição do livro de teste",
            gerarIsbnValido(),
            null
        );

        Response response = criarLivroInvalido(livroInvalido);

        assertEquals(400, response.statusCode(), "Ano inválido (negativo) deveria falhar");

        // Ano no futuro
        livroInvalido.setEditionYear(LocalDate.now().getYear() + 1);

        response = criarLivroInvalido(livroInvalido);

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(13)
    @DisplayName("CT013 - Criar livro com ISBN inválido ou já existente deve falhar")
    public void deveFalharAoCriarLivroComIsbnInvalidoOuExistente() {
        Book livroInvalido = new Book(
            "Título de Teste",
            "Autor de Teste",
            "Editora Teste",
            2024,
            "1",
            "Descrição do livro de teste",
            "1234567890123",
            BookStatus.AVAILABLE
        );

        Response response = criarLivroInvalido(livroInvalido);

        assertEquals(400, response.statusCode(), "Deveria falhar ao criar livro com ISBN inválido");

        isbnParaTeste = gerarIsbnValido();
        livroParaTesteId = criarLivro(criarLivroValido(isbnParaTeste, "Livro original com ISBN repetido"));
        livroInvalido.setIsbn(isbnParaTeste);

        response = criarLivroInvalido(livroInvalido);

        assertEquals(400, response.statusCode(), "Deveria falhar ao criar livro com ISBN já existente");
    }

    @Test
    @Order(14)
    @DisplayName("CT014 - Obter livro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoObterLivroComIdInexistente() {
        given()
        .when()
            .get("/book/{id}", 999999)
        .then()
            .statusCode(404);

        given()
        .when()
            .get("/book/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(15)
    @DisplayName("CT015 - Atualizar livro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoAtualizarLivroComIdInexistente() {
        Book livroAtualizado = new Book(
            "Título de Teste Atualizado",
            "Autor de Teste Atualizado",
            "Editora Teste Atualizada",
            2023,
            "4",
            "Descrição de Teste Atualizada",
            "978-0134685991",
            BookStatus.AVAILABLE
        );

        given()
            .contentType(ContentType.JSON)
            .body(livroAtualizado)
        .when()
            .put("/book/{id}", 999999)
        .then()
            .statusCode(404);

        given()
            .contentType(ContentType.JSON)
            .body(livroAtualizado)
        .when()
            .put("/book/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(16)
    @DisplayName("CT016 - Atualizar livro com campos de datatype inválido deve falhar")
    public void deveFalharAoAtualizarLivroComCamposInvalidos() {
        given()
            .contentType(ContentType.JSON)
            .body(livroComDatatypesInvalidos)
        .when()
            .put("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @Order(17)
    @DisplayName("CT017 - Atualizar livro com status inválido deve falhar")
    public void deveFalharAoAtualizarLivroComStatusInvalido() {
        given()
            .contentType(ContentType.JSON)
            .body(livroComStatusInvalido)
        .when()
            .put("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @Order(18)
    @DisplayName("CT018 - Apagar livro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoApagarLivroComIdInexistente() {
        given()
        .when()
            .delete("/book/{id}", "idInvalido")
        .then()
            .statusCode(400);

        given()
        .when()
            .delete("/book/{id}", 99999)
        .then()
            .statusCode(404);
    }

    @Test
    @Order(19)
    @DisplayName("CT019 - Apagar livro associados a uma reserva sem usar forceRemove deve falhar")
    public void deveFalharAoApagarLivroAssociadoSemForceRemove() {
        livroComReservaAtivaId = criarLivro(criarLivroValido());
        membroParaTesteId = criarMembro(criarMembroValido());
        criarReserva(membroParaTesteId, livroComReservaAtivaId);

        Response response = given()
        .when()
            .delete("/book/{id}", livroComReservaAtivaId);

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroComReservaAtivaId = null;
        }

        assertEquals(409, response.statusCode());
    }
}
