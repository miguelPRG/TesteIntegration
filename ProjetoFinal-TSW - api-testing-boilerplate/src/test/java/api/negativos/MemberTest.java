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
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MemberTest extends BaseTest {

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
            criarReserva(membroParaTesteId, livroParaTesteId);
        }
    }

    @AfterEach
    void limparDadosCriados() {
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
