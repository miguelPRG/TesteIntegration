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
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class BookTest extends BaseTest {

    private Integer livroParaTesteId;
    private Integer membroParaTesteId;
    private String isbnParaTeste;

    @BeforeEach
    void criarLivroAntesDeAtualizarOuApagar(TestInfo testInfo) {
        boolean testePrecisaDeLivroExistente = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveListarLivrosComSucesso")
                || method.getName().equals("deveObterLivroPorIdComSucesso")
                || method.getName().equals("deveAtualizarLivroComSucesso")
                || method.getName().equals("deveApagarLivroComSucesso")
                || method.getName().equals("deveApagarLivroMesmoComReservaAtiva")
            )
            .orElse(false);

        if (!testePrecisaDeLivroExistente) {
            return;
        }

        isbnParaTeste = gerarIsbnValido();
        livroParaTesteId = criarLivro(criarLivroValido(isbnParaTeste, "Livro antes da atualização"));
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
    }

    @Test
    @DisplayName("CT001 - Criar um livro com sucesso")
    public void deveCriarLivroComSucesso() {
        livroParaTesteId = criarLivro(criarLivroValido());
    }

    @Test
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

        assertTrue(livros.stream().anyMatch(livro -> livroParaTesteId.equals(livro.getId())));
    }

    @Test
    @DisplayName("CT003 - Obter um livro existente por id")
    public void deveObterLivroPorIdComSucesso() {
        Book livroObtido = given()
        .when()
            .get("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Book.class);

        assertAll(
            () -> assertNotNull(livroObtido),
            () -> assertEquals(livroParaTesteId, livroObtido.getId()),
            () -> assertEquals("Effective Java", livroObtido.getTitle()),
            () -> assertEquals("Joshua Bloch", livroObtido.getAuthor()),
            () -> assertEquals("Addison-Wesley", livroObtido.getPublisher()),
            () -> assertEquals(2018, livroObtido.getEditionYear()),
            () -> assertEquals("3", livroObtido.getEdition()),
            () -> assertEquals("Livro antes da atualização", livroObtido.getDescription()),
            () -> assertEquals(isbnParaTeste, livroObtido.getIsbn()),
            () -> assertEquals(BookStatus.AVAILABLE, livroObtido.getStatus())
        );
    }

    @Test
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
        bookUpdated.setStatus(BookStatus.UNAVAILABLE);

        Integer livroObtidoId = given()
            .contentType(ContentType.JSON)
            .body(bookUpdated)
        .when()
            .put("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Integer.class);

        assertAll(
            () -> assertNotNull(livroObtidoId),
            () -> assertTrue(livroObtidoId > 0, "O ID do livro atualizado deve ser maior que zero")
        );

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
            () -> assertEquals("Livro antes da atualização", livroObtido.getDescription()),
            () -> assertEquals(bookUpdated.getIsbn(), livroObtido.getIsbn()),
            () -> assertEquals(BookStatus.UNAVAILABLE, livroObtido.getStatus())
        );
    }

    @Test
    @DisplayName("CT005 - Tentar apagar um livro com forceRemove true mesmo que haja uma reserva ativa")
    public void deveApagarLivroMesmoComReservaAtiva() {
        membroParaTesteId = criarMembro(criarMembroValido());
        criarReserva(membroParaTesteId, livroParaTesteId);

        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(204);

        livroParaTesteId = null;
    }
}
