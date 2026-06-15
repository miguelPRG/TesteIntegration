package api.negativos;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

import api.BaseTest;
import api.classes.Member;
import static api.helpers.ApiActions.apagarLivro;
import static api.helpers.ApiActions.apagarMembro;
import static api.helpers.ApiActions.criarLivro;
import static api.helpers.ApiActions.criarMembro;
import static api.helpers.ApiActions.criarReserva;
import static api.helpers.DadosTesteFactory.criarLivroValido;
import static api.helpers.DadosTesteFactory.criarMembroComCamposFormatoInvalido;
import static api.helpers.DadosTesteFactory.criarMembroComDatatypesInvalidos;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@DisplayName("Testes Negativos da Entidade: Member")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberTest extends BaseTest {

    private static Map<String, Object> membroComDatatypesInvalidos;

    private Integer membroParaTesteId;
    private Integer membroComReservaAtivaId;
    private Integer livroParaTesteId;
    private Integer membroCriadoInesperadamenteId;

    private int obterOrdemTeste(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(method -> method.getAnnotation(Order.class))
            .map(Order::value)
            .orElse(0);
    }

    @BeforeAll
    static void prepararDadosParaTestes() {
        membroComDatatypesInvalidos = criarMembroComDatatypesInvalidos();
    }

    @BeforeEach
    void prepararDadosParaTestesDeAtualizacao(TestInfo testInfo) {
        int ordemTeste = obterOrdemTeste(testInfo);

        if (ordemTeste == 30 || ordemTeste == 31 || ordemTeste == 32) {
            membroParaTesteId = criarMembro(criarMembroValido());
        }
    }

    @AfterEach
    void limparDadosCriados() {
        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
            membroParaTesteId = null;
        }

        if (membroCriadoInesperadamenteId != null) {
            apagarMembro(membroCriadoInesperadamenteId);
            membroCriadoInesperadamenteId = null;
        }

        if (membroComReservaAtivaId != null) {
            apagarMembro(membroComReservaAtivaId, true);
            membroComReservaAtivaId = null;
        }

        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId, true);
            livroParaTesteId = null;
        }
    }

    @Test
    @Order(24)
    @DisplayName("CT024 - Criar membro com campos de datatype inválidos deve falhar")
    public void deveFalharAoCriarMembroComCamposDatatypeInvalidos() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroComDatatypesInvalidos)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroCriadoInesperadamenteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(25)
    @DisplayName("CT025 - Criar membro com datas inválidas deve falhar")
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
            membroCriadoInesperadamenteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(26)
    @DisplayName("CT026 - Criar membro com datas de nascimento ou registro no futuro deve falhar")
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
            membroCriadoInesperadamenteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(27)
    @DisplayName("CT027 - Criar membro com postal code, cidade, país, telefone, NIF ou email inválido deve falhar")
    public void deveFalharAoCriarMembroComCamposFormatoInvalido() {
        Member membroInvalido = criarMembroComCamposFormatoInvalido();

        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroCriadoInesperadamenteId = response.as(Integer.class);
        }

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(28)
    @DisplayName("CT028 - Obter membro com ID inexistente ou inválido deve falhar")
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
    @Order(29)
    @DisplayName("CT029 - Atualizar membro com ID inexistente ou inválido deve falhar")
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
    @Order(30)
    @DisplayName("CT030 - Atualizar membro com campos de datatype inválidos deve falhar")
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
    @Order(31)
    @DisplayName("CT031 - Atualizar membro com datas de nascimento ou registro no futuro deve falhar")
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
    @Order(32)
    @DisplayName("CT032 - Atualizar membro com postal code, cidade, país, telefone, NIF ou email inválido deve falhar")
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
    @Order(33)
    @DisplayName("CT033 - Apagar membro associado a uma reserva sem usar forceRemove deve falhar")
    public void deveFalharAoApagarMembroAssociadoSemForceRemove() {
        membroComReservaAtivaId = criarMembro(criarMembroValido());
        livroParaTesteId = criarLivro(criarLivroValido());
        criarReserva(membroComReservaAtivaId, livroParaTesteId);

        Response response = given()
        .when()
            .delete("/member/{id}", membroComReservaAtivaId);

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            membroComReservaAtivaId = null;
        }

        assertEquals(409, response.statusCode());
    }

    @Test
    @Order(34)
    @DisplayName("CT034 - Apagar membro com ID inexistente ou inválido deve falhar")
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
