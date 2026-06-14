package api.negativos;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
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

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class BookTest extends BaseTest {

    private Integer livroParaTesteId;
    private Integer membroParaTesteId;
    private String isbnParaTeste;
    private Map<String, Object> livroComDatatypesInvalidos;
    private Map<String, Object> livroComStatusInvalido;

    @BeforeEach
    void operacoesAntes(TestInfo testInfo) {
        boolean testeUsaLivroComDatatypesInvalidos = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoCriarLivroComCamposDatatypeInvalidos")
                || method.getName().equals("deveFalharAoAtualizarLivroComCamposInvalidos")
            )
            .orElse(false);

        boolean testeUsaLivroComStatusInvalido = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoCriarLivroComStatusInvalido")
                || method.getName().equals("deveFalharAoAtualizarLivroComStatusInvalido")
            )
            .orElse(false);

        boolean testePrecisaDeLivroExistente = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoCriarLivroComIsbnExistente")
                || method.getName().equals("deveFalharAoAtualizarLivroComCamposInvalidos")
                || method.getName().equals("deveFalharAoAtualizarLivroComStatusInvalido")
                || method.getName().equals("deveFalharAoApagarLivroAssociadoSemForceRemove")
                || method.getName().equals("deveApagarLivroMesmoComReservaAtiva")
            )
            .orElse(false);

        boolean testePrecisaDeReservaAtiva = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoApagarLivroAssociadoSemForceRemove")
                || method.getName().equals("deveApagarLivroMesmoComReservaAtiva")
            )
            .orElse(false);

        if (testeUsaLivroComDatatypesInvalidos) {
            livroComDatatypesInvalidos = criarLivroComDatatypesInvalidos();
        }

        if (!testePrecisaDeLivroExistente) {
            if (testeUsaLivroComStatusInvalido) {
                livroComStatusInvalido = criarLivroComStatusInvalido(gerarIsbnValido());
            }
            return;
        }

        isbnParaTeste = gerarIsbnValido();
        livroParaTesteId = criarLivro(criarLivroValido(isbnParaTeste, "Livro antes da atualização"));

        if (testeUsaLivroComStatusInvalido) {
            livroComStatusInvalido = criarLivroComStatusInvalido(isbnParaTeste);
        }

        if (testePrecisaDeReservaAtiva) {
            membroParaTesteId = criarMembro(criarMembroValido());
            criarReserva(membroParaTesteId, livroParaTesteId);
        }
    }

    @AfterEach
    void limparDadosCriados() {
        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId);
        }

        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
        }

        livroParaTesteId = null;
        membroParaTesteId = null;
        livroComDatatypesInvalidos = null;
        livroComStatusInvalido = null;
    }

    @Test
    @DisplayName("CT006 - Criar livro com campos de datatype inválidos deve falhar")
    public void deveFalharAoCriarLivroComCamposDatatypeInvalidos() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(livroComDatatypesInvalidos)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT007 - Criar livro com status inválido")
    public void deveFalharAoCriarLivroComStatusInvalido() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(livroComStatusInvalido)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT008 - Criar livro com ano inválido deve falhar")
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

        Response response = given()
            .contentType(ContentType.JSON)
            .body(livroInvalido)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());

        livroInvalido.setEditionYear(LocalDate.now().getYear() + 1);

        response = given()
            .contentType(ContentType.JSON)
            .body(livroInvalido)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT009 - Criar livro com ISBN inválido deve falhar")
    public void deveFalharAoCriarLivroComIsbnInvalido() {
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

        Response response = given()
            .contentType(ContentType.JSON)
            .body(livroInvalido)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT010 - Criar livro com ISBN já existente deve falhar")
    public void deveFalharAoCriarLivroComIsbnExistente() {
        Book livroInvalido = new Book(
            "Título de Teste",
            "Autor de Teste",
            "Editora Teste",
            2024,
            "1",
            "Descrição do livro de teste",
            isbnParaTeste,
            BookStatus.AVAILABLE
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(livroInvalido)
        .when()
            .post("/book");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            apagarLivro(response.as(Integer.class));
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT011 - Obter livro com ID inexistente ou inválido deve falhar")
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
    @DisplayName("CT012 - Atualizar livro com ID inexistente ou inválido deve falhar")
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
    @DisplayName("CT013 - Atualizar livro com campos de datatype inválido deve falhar")
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
    @DisplayName("CT013 - Atualizar livro com status inválido deve falhar")
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
    @DisplayName("CT014 - Apagar livro com ID inexistente ou inválido deve falhar")
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
    @DisplayName("CT015 - Apagar livro associados a uma reserva sem usar forceRemove deve falhar")
    public void deveFalharAoApagarLivroAssociadoSemForceRemove() {
        Response response = given()
        .when()
            .delete("/book/{id}", livroParaTesteId);

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            livroParaTesteId = null;
        }

        assertEquals(409, response.statusCode());
    }
}
