package api.negativos;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import api.BaseTest;
import api.classes.Book;
import api.classes.BookStatus;
import api.classes.Member;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


public class BookTest extends BaseTest {
        
    private static final AtomicLong ISBN_SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private Integer livroParaTesteId;
    private Integer membroParaTesteId;
    private String isbnParaTeste;
    private Map<String, Object> livroComDatatypesInvalidos;
    private Map<String, Object> livroComStatusInvalido;

    // Este beforeEach cria um livro apenas para os testes que precisam de um livro existente(GET PUT DELETE).
    @BeforeEach
    void operacoesAntes(TestInfo testInfo) {
        
        //Verifica se o teste vai testar body com datatypes invalidos
        boolean testeUsaLivroComDatatypesInvalidos = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoCriarLivroComCamposDatatypeInvalidos")
                || method.getName().equals("deveFalharAoAtualizarLivroComCamposInvalidos")
            )
            .orElse(false);
        
        // Verifica se o teste vai testar body com status invalido
        boolean testeUsaLivroComStatusInvalido = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoCriarLivroComStatusInvalido")
                || method.getName().equals("deveFalharAoAtualizarLivroComStatusInvalido")
            )
            .orElse(false);
        
        // Estes testes precisam obrigatoriamente de um livro existente
        boolean testePrecisaDeLivroExistente = testInfo.getTestMethod()
        .map(method -> (method.getName().equals("deveFalharAoCriarLivroComIsbnExistente"))
            || (method.getName().equals("deveFalharAoAtualizarLivroComCamposInvalidos"))
            || (method.getName().equals("deveFalharAoAtualizarLivroComStatusInvalido"))
            || (method.getName().equals("deveFalharAoApagarLivroAssociadoSemForceRemove"))
            || (method.getName().equals("deveApagarLivroMesmoComReservaAtiva"))
        )
            .orElse(false);

        // Estes testes precisam obrigatoriamente de um livro existente e uma reserva ativa associada a esse livro
        boolean testePrecisaDeReservaAtiva = testInfo.getTestMethod()
        .map(method -> (method.getName().equals("deveFalharAoApagarLivroAssociadoSemForceRemove"))
            || (method.getName().equals("deveApagarLivroMesmoComReservaAtiva"))
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

        Book livroOriginal = new Book(
            "Effective Java",
            "Joshua Bloch",
            "Addison-Wesley",
            2018,
            "3",
            "Livro antes da atualização",
            isbnParaTeste,
            BookStatus.AVAILABLE
        );

        livroParaTesteId = given()
            .contentType(ContentType.JSON)
            .body(livroOriginal)
        .when()
            .post("/book")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);

        if (testeUsaLivroComStatusInvalido) {
            livroComStatusInvalido = criarLivroComStatusInvalido(isbnParaTeste);
        }

        if (testePrecisaDeReservaAtiva) {
            criarMembroParaTeste();
            criarReservaParaTeste();
        }
    }

    // Este afterEach é executado após cada teste, garantindo que qualquer livro criado durante o teste ou reserva seja removido.
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
        livroComDatatypesInvalidos = null;
        livroComStatusInvalido = null;
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

    private void criarMembroParaTeste() {
        long valorUnico = ISBN_SEQUENCE.incrementAndGet() % 1_000_000;
        int nifValido = gerarNifValido(valorUnico);

        Member membro = new Member(
            "João",
            "Silva",
            "Rua A",
            "1234-567",
            "Lisboa",
            "Portugal",
            912345678,
            nifValido,
            "joao.silva" + valorUnico + "@example.com",
            "1990-01-01",
            "2023-01-01"
        );

        membroParaTesteId = given()
            .contentType(ContentType.JSON)
            .body(membro)
        .when()
            .post("/member")
        .then()
            .statusCode(201)
            .extract()
            .as(Integer.class);
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

    // Método auxiliar para gerar um ISBN válido
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

    private Map<String, Object> criarLivroComDatatypesInvalidos() {
        Map<String, Object> livroInvalido = new HashMap<>();
        livroInvalido.put("title", 12345); // Título com datatype inválido
        livroInvalido.put("author", true); // Autor com datatype inválido
        livroInvalido.put("publisher", 67890); // Editora com datatype inválido
        livroInvalido.put("editionYear", "Ano inválido"); // Ano de publicação com datatype inválido
        livroInvalido.put("edition", 1); // Edição com datatype inválido
        livroInvalido.put("description", 12345); // Descrição com datatype inválido
        livroInvalido.put("isbn", 9780134685991L); // ISBN com datatype inválido
        livroInvalido.put("status", 123); // Status com datatype inválido
        return livroInvalido;
    }

    private Map<String, Object> criarLivroComStatusInvalido(String isbn) {
        Map<String, Object> livroInvalido = new HashMap<>();
        livroInvalido.put("title", "Título de Teste");
        livroInvalido.put("author", "Autor de Teste");
        livroInvalido.put("publisher", "Editora Teste");
        livroInvalido.put("editionYear", 2024);
        livroInvalido.put("edition", "1");
        livroInvalido.put("description", "Descrição do livro de teste");
        livroInvalido.put("isbn", isbn);
        livroInvalido.put("status", "INVALID_STATUS"); // Status inválido
        return livroInvalido;
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
            -2024,  // Ano de publicação inválido
            "1",
            "Descrição do livro de teste",
            isbnParaTeste,
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


        // Vamos verificar se dá erro caso o ano de publicação seja no futuro
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
            "1234567890123", // ISBN inválido
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
            isbnParaTeste, // Usar o ISBN do livro existente
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
    
    /*
        Obter:
            - Tentar obter um livro com um ID que não existe e verificar se a API retorna o erro adequado (ex: 404 Not Found).
    */

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
    @DisplayName("CT016 - Atualizar livro com status inválido deve falhar")
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
