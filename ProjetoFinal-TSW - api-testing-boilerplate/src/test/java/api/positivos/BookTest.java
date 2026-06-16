package api.positivos;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static api.helpers.DadosTesteFactory.criarLivroValido;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static api.helpers.DadosTesteFactory.gerarIsbnValido;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@DisplayName("Testes da Entidade: Book")
@Order(1)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookTest extends BaseTest {

    private Integer livroParaTesteId;
    private Integer livroComReservaAtivaId;
    private Integer membroComReservaAtivaId;
    private String isbnParaTeste;

    private int obterOrdemTeste(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(method -> method.getAnnotation(Order.class))
            .map(Order::value)
            .orElse(0);
    }

    private void assertLivroTemCamposObrigatorios(Book livro) {
        assertAll(
            () -> assertNotNull(livro.getId()),
            () -> assertNotNull(livro.getTitle()),
            () -> assertNotNull(livro.getAuthor()),
            () -> assertNotNull(livro.getPublisher()),
            () -> assertNotNull(livro.getEditionYear()),
            () -> assertNotNull(livro.getEdition()),
            () -> assertNotNull(livro.getDescription()),
            () -> assertNotNull(livro.getIsbn()),
            () -> assertNotNull(livro.getStatus())
        );
    }

    private void assertLivroCriadoNoSetup(Book livro) {
        assertAll(
            () -> assertNotNull(livro),
            () -> assertEquals(livroParaTesteId, livro.getId()),
            () -> assertEquals("Effective Java", livro.getTitle()),
            () -> assertEquals("Joshua Bloch", livro.getAuthor()),
            () -> assertEquals("Addison-Wesley", livro.getPublisher()),
            () -> assertEquals(2018, livro.getEditionYear()),
            () -> assertEquals("3", livro.getEdition()),
            () -> assertEquals("Livro antes da atualização", livro.getDescription()),
            () -> assertEquals(isbnParaTeste, livro.getIsbn()),
            () -> assertEquals(BookStatus.AVAILABLE, livro.getStatus())
        );
    }

    @BeforeEach
    void prepararDadosParaTestesNaoCreate(TestInfo testInfo) {
        int ordemTeste = obterOrdemTeste(testInfo);

        if (ordemTeste >= 2 && ordemTeste <= 5) {
            isbnParaTeste = gerarIsbnValido();
            livroParaTesteId = criarLivro(criarLivroValido(isbnParaTeste, "Livro antes da atualização"));
        }

        if (ordemTeste == 6 || ordemTeste == 7) {
            membroComReservaAtivaId = criarMembro(criarMembroValido());
            livroComReservaAtivaId = criarLivro(criarLivroValido(gerarIsbnValido(), "Livro com reserva ativa"));
            criarReserva(membroComReservaAtivaId, livroComReservaAtivaId);
        }
    }

    @AfterEach
    void limparDadosCriados() {
        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId, true);
            livroParaTesteId = null;
        }

        if (livroComReservaAtivaId != null) {
            apagarLivro(livroComReservaAtivaId, true);
            livroComReservaAtivaId = null;
        }

        if (membroComReservaAtivaId != null) {
            apagarMembro(membroComReservaAtivaId, true);
            membroComReservaAtivaId = null;
        }
    }

    @Test
    @Order(1)
    @DisplayName("CT001 - Criar um livro com sucesso")
    public void deveCriarLivroComSucesso() {
        isbnParaTeste = gerarIsbnValido();
        livroParaTesteId = criarLivro(criarLivroValido(isbnParaTeste, "Livro antes da atualização"));

        assertTrue(livroParaTesteId > 0);
    }

    @Test
    @Order(2)
    @DisplayName("CT002 - Listar livros com sucesso")
    public void deveListarLivrosComSucesso() {
        List<Book> livros = given()
        .when()
            .get("/book")
        .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("", Book.class);

        assertFalse(livros.isEmpty());

        for (Book livro : livros) {
            assertLivroTemCamposObrigatorios(livro);
        }

        assertTrue(livros.stream().anyMatch(livro -> livroParaTesteId.equals(livro.getId())));
    }

    @Test
    @Order(3)
    @DisplayName("CT003 - Obter um livro existente por id")
    public void deveObterLivroPorIdComSucesso() {
        Book livroObtido = given()
        .when()
            .get("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Book.class);

        assertLivroCriadoNoSetup(livroObtido);
    }

    @Test
    @Order(4)
    @DisplayName("CT004 - Atualizar um livro com sucesso")
    public void deveAtualizarLivroComSucesso() {
        Book bookUpdated = given()
        .when()
            .get("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Book.class);

        bookUpdated.setAuthor("Miguel Gonçalves");
        bookUpdated.setDescription("Livro atualizado com sucesso");
        bookUpdated.setStatus(BookStatus.NOT_AVAILABLE);
        // Ignorar o ID do livro ao atualizar, pois ele não deve ser alterado.
        bookUpdated.setId(null);

        String livroObtidoIdResponse = given()
            .contentType(ContentType.JSON)
            .body(bookUpdated)
        .when()
            .put("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .asString();

        Integer livroObtidoId = Integer.valueOf(livroObtidoIdResponse.trim());

        Book livroObtido = given()
        .when()
            .get("/book/{id}", livroObtidoId)
        .then()
            .statusCode(200)
            .extract()
            .as(Book.class);

        assertAll(
            () -> assertNotNull(livroObtido),
            () -> assertEquals(livroParaTesteId, livroObtido.getId()),
            () -> assertEquals("Effective Java", livroObtido.getTitle()),
            () -> assertEquals("Miguel Gonçalves", livroObtido.getAuthor()),
            () -> assertEquals("Addison-Wesley", livroObtido.getPublisher()),
            () -> assertEquals(2018, livroObtido.getEditionYear()),
            () -> assertEquals("3", livroObtido.getEdition()),
            () -> assertEquals("Livro atualizado com sucesso", livroObtido.getDescription()),
            () -> assertEquals(bookUpdated.getIsbn(), livroObtido.getIsbn()),
            () -> assertEquals(BookStatus.NOT_AVAILABLE, livroObtido.getStatus())
        );
    }

    @Test
    @Order(5)
    @DisplayName("CT005 - Apagar um livro com sucesso")
    public void deveApagarLivroComSucesso() {
        given()
        .when()
            .delete("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(204);

        livroParaTesteId = null;
    }

    @Test
    @Order(6)
    @DisplayName("CT006 - Tentar apagar um livro com forceRemove true mesmo que haja uma reserva ativa")
    public void deveApagarLivroMesmoComReservaAtiva() {

        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/book/{id}", livroComReservaAtivaId)
        .then()
            .statusCode(204);

        livroComReservaAtivaId = null;
    }

    @Test
    @Order(7)
    @DisplayName("CT007 - Verificar se o status do livro é atualizado para RESERVED quando há uma reserva ativa")
    public void deveAtualizarStatusDoLivroParaReservedQuandoHaReservaAtiva() {
        Book livroObtido = given()
        .when()
            .get("/book/{id}", livroComReservaAtivaId)
        .then()
            .statusCode(200)
            .extract()
            .as(Book.class);

        assertEquals(BookStatus.RESERVED, livroObtido.getStatus());
    }
}
