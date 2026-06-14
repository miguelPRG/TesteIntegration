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
import io.restassured.response.Response;

@DisplayName("Testes Negativos da Entidade: Member")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MemberTest extends BaseTest {

    private static final AtomicLong MEMBER_SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private Integer membroParaTesteId;
    private Integer livroParaTesteId;
    private Map<String, Object> membroComDatatypesInvalidos;

    @BeforeEach
    void prepararDadosQuandoNecessario(TestInfo testInfo) {
        boolean testeUsaMembroComDatatypesInvalidos = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoCriarMembroComCamposDatatypeInvalidos")
                || method.getName().equals("deveFalharAoAtualizarMembroComCamposDatatypeInvalidos")
            )
            .orElse(false);

        boolean testePrecisaDeMembroExistente = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoAtualizarMembroComCamposDatatypeInvalidos")
                || method.getName().equals("deveFalharAoAtualizarMembroComDatasNoFuturo")
                || method.getName().equals("deveFalharAoAtualizarMembroComCamposFormatoInvalido")
                || method.getName().equals("deveFalharAoApagarMembroAssociadoSemForceRemove")
            )
            .orElse(false);

        boolean testePrecisaDeReservaAtiva = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveFalharAoApagarMembroAssociadoSemForceRemove"))
            .orElse(false);

        if (testeUsaMembroComDatatypesInvalidos) {
            membroComDatatypesInvalidos = criarMembroComDatatypesInvalidos();
        }

        if (!testePrecisaDeMembroExistente) {
            return;
        }

        membroParaTesteId = criarMembro(criarMembroValido());

        if (testePrecisaDeReservaAtiva) {
            livroParaTesteId = criarLivro(criarLivroValido());
            criarReservaParaTeste();
        }
    }

    @AfterEach
    void limparMembroCriado() {
        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
        }

        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId);
        }

        membroParaTesteId = null;
        livroParaTesteId = null;
        membroComDatatypesInvalidos = null;
    }

    private Integer criarMembro(Member membro) {
        return given()
            .contentType(ContentType.JSON)
            .body(membro)
        .when()
            .post("/member")
        .then()
            .statusCode(201)
            .body(notNullValue())
            .extract()
            .as(Integer.class);
    }

    private void apagarMembro(Integer membroId) {
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/member/{id}", membroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
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

    private Member criarMembroValido() {
        long valorUnico = MEMBER_SEQUENCE.incrementAndGet() % 1_000_000;

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

    private Book criarLivroValido() {
        return new Book(
            "Effective Java",
            "Joshua Bloch",
            "Addison-Wesley",
            2018,
            "3",
            "Livro para reserva",
            gerarIsbnValido(),
            BookStatus.AVAILABLE
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

    private String gerarIsbnValido() {
        String base = "978" + String.format("%09d", MEMBER_SEQUENCE.incrementAndGet() % 1_000_000_000);
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            int digito = Character.getNumericValue(base.charAt(i));
            soma += (i % 2 == 0) ? digito : digito * 3;
        }

        int digitoControlo = (10 - (soma % 10)) % 10;
        return base + digitoControlo;
    }

    private void criarReservaParaTeste() {
        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroParaTesteId, livroParaTesteId)
        .then()
            .statusCode(201);
    }

    private Map<String, Object> criarMembroComDatatypesInvalidos() {
        Map<String, Object> membroInvalido = new HashMap<>();
        membroInvalido.put("firstName", 12345);
        membroInvalido.put("lastName", true);
        membroInvalido.put("address", 67890);
        membroInvalido.put("postalCode", false);
        membroInvalido.put("city", 12345);
        membroInvalido.put("country", 67890);
        membroInvalido.put("phoneNumber", "telefone inválido");
        membroInvalido.put("nif", "nif inválido");
        membroInvalido.put("email", 12345);
        membroInvalido.put("birthDate", 12345);
        membroInvalido.put("registrationDate", true);
        return membroInvalido;
    }

    private Member criarMembroComCamposFormatoInvalido() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setPostalCode("1234567");
        membroInvalido.setCity("Lisboa123");
        membroInvalido.setCountry("Portugal123");
        membroInvalido.setPhoneNumber(12345);
        membroInvalido.setNif(123456780);
        membroInvalido.setEmail("email-invalido");
        return membroInvalido;
    }

    @Test
    @DisplayName("CT022 - Criar membro com campos de datatype inválidos deve falhar")
    public void deveFalharAoCriarMembroComCamposDatatypeInvalidos() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroComDatatypesInvalidos)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT023 - Criar membro com datas inválidas deve falhar")
    public void deveFalharAoCriarMembroComDatasInvalidas() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setBirthDate("data inválida");
        membroInvalido.setRegistrationDate("data inválida");

        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT024 - Criar membro com datas de nascimento ou registro no futuro deve falhar")
    public void deveFalharAoCriarMembroComDatasNoFuturo() {
        Member membroInvalido = criarMembroValido();
        String dataFutura = LocalDate.now().plusDays(1).toString();
        membroInvalido.setBirthDate(dataFutura);
        membroInvalido.setRegistrationDate(dataFutura);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT025 - Criar membro com postal code, cidade, país, telefone, NIF ou email inválido deve falhar")
    public void deveFalharAoCriarMembroComCamposFormatoInvalido() {
        Member membroInvalido = criarMembroComCamposFormatoInvalido();

        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroParaTesteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("CT026 - Obter membro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoObterMembroComIdInexistenteOuInvalido() {
        given()
        .when()
            .get("/member/{id}", 999999)
        .then()
            .statusCode(404);

        given()
        .when()
            .get("/member/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CT027 - Atualizar membro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoAtualizarMembroComIdInexistenteOuInvalido() {
        Member membroAtualizado = criarMembroValido();

        given()
            .contentType(ContentType.JSON)
            .body(membroAtualizado)
        .when()
            .put("/member/{id}", 999999)
        .then()
            .statusCode(404);

        given()
            .contentType(ContentType.JSON)
            .body(membroAtualizado)
        .when()
            .put("/member/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CT028 - Atualizar membro com campos de datatype inválidos deve falhar")
    public void deveFalharAoAtualizarMembroComCamposDatatypeInvalidos() {
        given()
            .contentType(ContentType.JSON)
            .body(membroComDatatypesInvalidos)
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CT029 - Atualizar membro com datas de nascimento ou registro no futuro deve falhar")
    public void deveFalharAoAtualizarMembroComDatasNoFuturo() {
        Member membroInvalido = criarMembroValido();
        String dataFutura = LocalDate.now().plusDays(1).toString();
        membroInvalido.setBirthDate(dataFutura);
        membroInvalido.setRegistrationDate(dataFutura);

        given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CT030 - Atualizar membro com postal code, cidade, país, telefone, NIF ou email inválido deve falhar")
    public void deveFalharAoAtualizarMembroComCamposFormatoInvalido() {
        given()
            .contentType(ContentType.JSON)
            .body(criarMembroComCamposFormatoInvalido())
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("CT031 - Apagar membro associado a uma reserva sem usar forceRemove deve falhar")
    public void deveFalharAoApagarMembroAssociadoSemForceRemove() {
        Response response = given()
        .when()
            .delete("/member/{id}", membroParaTesteId);

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroParaTesteId = null;
        }

        assertEquals(409, response.statusCode());
    }

    @Test
    @DisplayName("CT032 - Apagar membro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoApagarMembroComIdInexistenteOuInvalido() {
        given()
        .when()
            .delete("/member/{id}", "idInvalido")
        .then()
            .statusCode(400);

        given()
        .when()
            .delete("/member/{id}", 999999)
        .then()
            .statusCode(404);
    }
}
