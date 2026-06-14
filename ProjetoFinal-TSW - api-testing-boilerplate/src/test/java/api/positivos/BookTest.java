package api.positivos;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
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
import api.classes.Member;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@DisplayName("Testes da Entidade: Book")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class BookTest extends BaseTest {

    private static final AtomicLong ISBN_SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private Integer livroParaTesteId;
    private Integer membroParaTesteId;
    private String isbnParaTeste;

    // Este beforeEach cria um livro apenas para os testes que precisam de um livro existente(GET PUT DELETE).
    @BeforeEach
    void criarLivroAntesDeAtualizarOuApagar(TestInfo testInfo) {
        boolean testePrecisaDeLivroExistente = testInfo.getTestMethod()
            .map(method -> (method.getName().equals("deveListarLivrosComSucesso") 
                || (method.getName().equals("deveObterLivroPorIdComSucesso"))
                || (method.getName().equals("deveAtualizarLivroComSucesso"))
                || (method.getName().equals("deveApagarLivroComSucesso")))
                || (method.getName().equals("deveApagarLivroMesmoComReservaAtiva"))
            )
            .orElse(false);

        if (!testePrecisaDeLivroExistente) {
            return;
        }

        livroParaTesteId = criarLivro(criarLivroValido());
    }

    // Este afterEach é executado após cada teste, garantindo que qualquer livro criado durante o teste seja removido. Isso mantém o ambiente de teste limpo e evita interferências entre os testes.
    @AfterEach
    void limparLivrosCriados() {
        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId);
        }

        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
        }

        livroParaTesteId = null;
        membroParaTesteId = null;
    }

    private Integer criarLivro(Book livro) {
        return given()
            .contentType(ContentType.JSON)
            .body(livro)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);
    }

    private void apagarLivro(Integer livroId) {
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/book/{id}", livroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
    }

    private void apagarMembro(Integer membroId) {
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/member/{id}", membroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
    }

    private Book criarLivroValido() {
        isbnParaTeste = gerarIsbnValido();

        return new Book(
            "Effective Java",
            "Joshua Bloch",
            "Addison-Wesley",
            2018,
            "3",
            "Livro antes da atualização",
            isbnParaTeste,
            BookStatus.AVAILABLE
        );
    }

    private Integer criarMembro(Member membro) {
        return given()
            .contentType(ContentType.JSON)
            .body(membro)
        .when()
            .post("/member")
        .then()
            .statusCode(201)
            .extract()
            .as(Integer.class);
    }

    private Member criarMembroValido() {
        long valorUnico = ISBN_SEQUENCE.incrementAndGet() % 1_000_000;

        return new Member(
            "João",
            "Silva",
            "Rua A",
            "1234-567",
            "Lisboa",
            "Portugal",
            912345678,
            gerarNifValido(valorUnico),
            "joao.silva" + valorUnico + "@example.com",
            "1990-01-01",
            "2023-01-01"
        );
    }

    private Integer gerarNifValido(long valorUnico) {
        String base = "2" + String.format("%07d", valorUnico % 10_000_000);
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            int digito = Character.getNumericValue(base.charAt(i));
            soma += digito * (9 - i);
        }

        int digitoControlo = 11 - (soma % 11);
        if (digitoControlo >= 10) {
            digitoControlo = 0;
        }

        return Integer.parseInt(base + digitoControlo);
    }

    private void criarReservaParaTeste() {
        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroParaTesteId, livroParaTesteId)
        .then()
            .statusCode(201);
    }

    private String gerarIsbnValido() {
        String base = "978" + String.format("%09d", ISBN_SEQUENCE.incrementAndGet() % 1_000_000_000);
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            int digito = Character.getNumericValue(base.charAt(i));
            soma += (i % 2 == 0) ? digito : digito * 3;
        }

        int digitoControlo = (10 - (soma % 10)) % 10;
        return base + digitoControlo;
    }

    @Test
    @DisplayName("CT001 - Criar um livro com sucesso")
    public void deveCriarLivroComSucesso() {
        Book novoLivro = criarLivroValido();

        livroParaTesteId = criarLivro(novoLivro);
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
        criarReservaParaTeste();
        
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/book/{id}", livroParaTesteId)
        .then()
            .statusCode(204);

        livroParaTesteId = null;
    }
}
