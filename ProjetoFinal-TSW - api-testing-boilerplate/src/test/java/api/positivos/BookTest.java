package api.positivos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import api.BaseTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@DisplayName("Testes da Entidade: Book")
public class BookTest extends BaseTest {

    private static final AtomicLong ISBN_SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private final List<Integer> livrosCriados = new ArrayList<>();

    @AfterEach
    void limparLivrosCriados() {
        for (Integer livroId : livrosCriados) {
            given()
            .when()
                .delete("/book/{id}", livroId)
            .then()
                .statusCode(anyOf(equalTo(204), equalTo(404)));
        }

        livrosCriados.clear();
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

    private String criarPayloadLivro(String titulo, String autor, String editora, int ano, String edicao,
            String descricao) {
        return """
            {
                "title": "%s",
                "author": "%s",
                "publisher": "%s",
                "editionYear": %d,
                "edition": "%s",
                "description": "%s",
                "isbn": "%s",
                "status": "AVAILABLE"
            }
            """.formatted(titulo, autor, editora, ano, edicao, descricao, gerarIsbnValido());
    }

    @Test
    @DisplayName("CT001 - Criar um livro com sucesso")
    public void deveCriarLivroComSucesso() {
        String novoLivro = criarPayloadLivro(
            "O Principezinho",
            "Antoine de Saint-Exupéry",
            "Editorial Presença",
            2024,
            "1",
            "Livro de teste"
        );

        Integer livroId = given()
            .contentType(ContentType.JSON)
            .body(novoLivro)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);

        livrosCriados.add(livroId);

        given()
        .when()
            .get("/book/{id}", livroId)
        .then()
            .statusCode(200)
            .body("title", equalTo("O Principezinho"))
            .body("author", equalTo("Antoine de Saint-Exupéry"))
            .body("publisher", equalTo("Editorial Presença"))
            .body("editionYear", equalTo(2024))
            .body("edition", equalTo("1"))
            .body("description", equalTo("Livro de teste"))
            .body("status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("CT002 - Obter um livro existente por id")
    public void deveObterLivroPorIdComSucesso() {
        String novoLivro = criarPayloadLivro(
            "Clean Code",
            "Robert C. Martin",
            "Prentice Hall",
            2008,
            "1",
            "Livro criado para testar consulta por id"
        );

        Integer livroId = given()
            .contentType(ContentType.JSON)
            .body(novoLivro)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);

        livrosCriados.add(livroId);

        given()
        .when()
            .get("/book/{id}", livroId)
        .then()
            .statusCode(200)
            .body("id", equalTo(livroId))
            .body("title", equalTo("Clean Code"))
            .body("author", equalTo("Robert C. Martin"))
            .body("status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("CT003 - Listar livros com sucesso")
    public void deveListarLivrosComSucesso() {
        String novoLivro = criarPayloadLivro(
            "Refactoring",
            "Martin Fowler",
            "Addison-Wesley",
            2018,
            "2",
            "Livro criado para testar listagem"
        );

        Integer livroId = given()
            .contentType(ContentType.JSON)
            .body(novoLivro)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);

        livrosCriados.add(livroId);

        given()
        .when()
            .get("/book")
        .then()
            .statusCode(200)
            .body("id", hasItem(livroId))
            .body("title", hasItem("Refactoring"))
            .body("author", hasItem("Martin Fowler"));
    }

    @Test
    @DisplayName("CT004 - Atualizar um livro com sucesso")
    public void deveAtualizarLivroComSucesso() {
        String livroOriginal = criarPayloadLivro(
            "Effective Java",
            "Joshua Bloch",
            "Addison-Wesley",
            2018,
            "3",
            "Livro antes da atualização"
        );

        Integer livroId = given()
            .contentType(ContentType.JSON)
            .body(livroOriginal)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);

        livrosCriados.add(livroId);

        String livroAtualizado = criarPayloadLivro(
            "Effective Java - Updated",
            "Joshua Bloch",
            "Pearson",
            2020,
            "3",
            "Livro atualizado por teste automatizado"
        );

        given()
            .contentType(ContentType.JSON)
            .body(livroAtualizado)
        .when()
            .put("/book/{id}", livroId)
        .then()
            .statusCode(200)
            .body(equalTo(livroId.toString()));

        given()
        .when()
            .get("/book/{id}", livroId)
        .then()
            .statusCode(200)
            .body("title", equalTo("Effective Java - Updated"))
            .body("publisher", equalTo("Pearson"))
            .body("editionYear", equalTo(2020))
            .body("description", equalTo("Livro atualizado por teste automatizado"))
            .body("status", equalTo("AVAILABLE"));
    }

    @Test
    @DisplayName("CT005 - Apagar um livro com sucesso")
    public void deveApagarLivroComSucesso() {
        String novoLivro = criarPayloadLivro(
            "Domain-Driven Design",
            "Eric Evans",
            "Addison-Wesley",
            2003,
            "1",
            "Livro criado para testar remoção"
        );

        Integer livroId = given()
            .contentType(ContentType.JSON)
            .body(novoLivro)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);

        livrosCriados.add(livroId);

        given()
        .when()
            .delete("/book/{id}", livroId)
        .then()
            .statusCode(204);

        livrosCriados.remove(livroId);

        given()
        .when()
            .get("/book/{id}", livroId)
        .then()
            .statusCode(404);
    }
}
