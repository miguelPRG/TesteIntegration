package api.negativos;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
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
import static api.helpers.DadosTesteFactory.criarMembroComDatatypesInvalidos;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@DisplayName("Testes Negativos da Entidade: Member")
@Order(4)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberTest extends BaseTest {

    private Integer membroParaTesteId;
    private Integer livroParaTesteId;
    private Member membroParaAtualizacaoInvalida;

    private int obterOrdemTeste(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(method -> method.getAnnotation(Order.class))
            .map(Order::value)
            .orElse(0);
    }

    private void assertFalhaAoCriarMembroInvalido(Object membroInvalido) {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .post("/member");

        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            apagarMembro(response.as(Integer.class));
        }

        assertEquals(400, response.statusCode());
    }

    private void assertFalhaAoAtualizarMembroInvalido(Object membroInvalido) {
        given()
            .contentType(ContentType.JSON)
            .body(membroInvalido)
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(400);
    }


    @BeforeEach
    void prepararMembroExistenteParaTestesDeAtualizacao(TestInfo testInfo) {
        int ordemTeste = obterOrdemTeste(testInfo);

        if (ordemTeste >= 33 && ordemTeste <= 39) {
            // Esta variavel será utilizada nos testes de atualização. Aqui criamos um registo na base de dados para garantir que o ID existe, já que os testes de atualização exigem um ID válido.
            membroParaTesteId = criarMembro(criarMembroValido());
        }

        if (ordemTeste >= 34 && ordemTeste <= 38) {
            // Esta variavel será utilizada como body válido nos testes de atualização; cada teste altera apenas o campo que pretende validar como inválido.
            membroParaAtualizacaoInvalida = criarMembroValido();
        }
    }

    @AfterEach
    void limparDadosCriados() {
        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
            membroParaTesteId = null;
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
        assertFalhaAoCriarMembroInvalido(criarMembroComDatatypesInvalidos());
    }

    @Test
    @Order(25)
    @DisplayName("CT025 - Criar membro com datas inválidas deve falhar")
    public void deveFalharAoCriarMembroComDatasInvalidas() {
        Member membroComBirthDateInvalida = criarMembroValido();
        membroComBirthDateInvalida.setBirthDate("data inválida");

        assertFalhaAoCriarMembroInvalido(membroComBirthDateInvalida);

        Member membroComRegistrationDateInvalida = criarMembroValido();
        membroComRegistrationDateInvalida.setRegistrationDate("data inválida");

        assertFalhaAoCriarMembroInvalido(membroComRegistrationDateInvalida);
    }

    @Test
    @Order(26)
    @DisplayName("CT026 - Criar membro com datas de nascimento ou registro no futuro deve falhar")
    public void deveFalharAoCriarMembroComDatasNoFuturo() {
        Member membroComBirthDateNoFuturo = criarMembroValido();
        membroComBirthDateNoFuturo.setBirthDate(LocalDate.now().plusDays(1).toString());

        assertFalhaAoCriarMembroInvalido(membroComBirthDateNoFuturo);

        Member membroComRegistrationDateNoFuturo = criarMembroValido();
        membroComRegistrationDateNoFuturo.setRegistrationDate(LocalDate.now().plusDays(1).toString());

        assertFalhaAoCriarMembroInvalido(membroComRegistrationDateNoFuturo);
    }

    @Test
    @Order(27)
    @DisplayName("CT027 - Criar membro com postal code inválido deve falhar")
    public void deveFalharAoCriarMembroComPostalCodeInvalido() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setPostalCode("12345671324");

        assertFalhaAoCriarMembroInvalido(membroInvalido);
    }


    @Test
    @Order(28)
    @DisplayName("CT028 - Criar membro com telefone inválido deve falhar")
    public void deveFalharAoCriarMembroComTelefoneInvalido() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setPhoneNumber(12345);

        assertFalhaAoCriarMembroInvalido(membroInvalido);
    }

    @Test
    @Order(29)
    @DisplayName("CT029 - Criar membro com NIF inválido deve falhar")
    public void deveFalharAoCriarMembroComNifInvalido() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setNif(123456780);

        assertFalhaAoCriarMembroInvalido(membroInvalido);
    }

    @Test
    @Order(30)
    @DisplayName("CT030 - Criar membro com email inválido deve falhar")
    public void deveFalharAoCriarMembroComEmailInvalido() {
        Member membroInvalido = criarMembroValido();
        membroInvalido.setEmail("email-invalido");

        assertFalhaAoCriarMembroInvalido(membroInvalido);
    }

    @Test
    @Order(31)
    @DisplayName("CT031 - Obter membro com ID inexistente ou inválido deve falhar")
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
    @Order(32)
    @DisplayName("CT032 - Atualizar membro com ID inexistente ou inválido deve falhar")
    public void deveFalharAoAtualizarMembroComIdInexistenteOuInvalido() {

        given()
            .contentType(ContentType.JSON)
            .body(criarMembroValido())
        .when()
            .put("/member/{id}", 999999)
        .then()
            .statusCode(404);

        given()
            .contentType(ContentType.JSON)
            .body(criarMembroValido())
        .when()
            .put("/member/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(33)
    @DisplayName("CT033 - Atualizar membro com campos de datatype inválidos deve falhar")
    public void deveFalharAoAtualizarMembroComCamposDatatypeInvalidos() {
        given()
            .contentType(ContentType.JSON)
            .body(criarMembroComDatatypesInvalidos())
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(400);
    }

    @Test
    @Order(34)
    @DisplayName("CT034 - Atualizar membro com datas de nascimento ou registro no futuro deve falhar")
    public void deveFalharAoAtualizarMembroComDatasNoFuturo() {
        String dataFutura = LocalDate.now().plusDays(1).toString();

        membroParaAtualizacaoInvalida.setRegistrationDate(dataFutura);
        assertFalhaAoAtualizarMembroInvalido(membroParaAtualizacaoInvalida);

        membroParaAtualizacaoInvalida.setBirthDate(dataFutura);
        assertFalhaAoAtualizarMembroInvalido(membroParaAtualizacaoInvalida);

    }

    @Test
    @Order(35)
    @DisplayName("CT035 - Atualizar membro com postal code inválido deve falhar")
    public void deveFalharAoAtualizarMembroComPostalCodeInvalido() {
        membroParaAtualizacaoInvalida.setPostalCode("1234567");

        assertFalhaAoAtualizarMembroInvalido(membroParaAtualizacaoInvalida);
    }

    @Test
    @Order(36)
    @DisplayName("CT036 - Atualizar membro com telefone inválido deve falhar")
    public void deveFalharAoAtualizarMembroComTelefoneInvalido() {
        membroParaAtualizacaoInvalida.setPhoneNumber(12345);

        assertFalhaAoAtualizarMembroInvalido(membroParaAtualizacaoInvalida);
    }

    @Test
    @Order(37)
    @DisplayName("CT037 - Atualizar membro com NIF inválido deve falhar")
    public void deveFalharAoAtualizarMembroComNifInvalido() {
        membroParaAtualizacaoInvalida.setNif(123456780);

        assertFalhaAoAtualizarMembroInvalido(membroParaAtualizacaoInvalida);
    }

    @Test
    @Order(38)
    @DisplayName("CT038 - Atualizar membro com email inválido deve falhar")
    public void deveFalharAoAtualizarMembroComEmailInvalido() {
        membroParaAtualizacaoInvalida.setEmail("email-invalido");

        assertFalhaAoAtualizarMembroInvalido(membroParaAtualizacaoInvalida);
    }

    @Test
    @Order(39)
    @DisplayName("CT039 - Apagar membro associado a uma reserva sem usar forceRemove deve falhar")
    public void deveFalharAoApagarMembroAssociadoSemForceRemove() {
        livroParaTesteId = criarLivro(criarLivroValido());
        criarReserva(membroParaTesteId, livroParaTesteId);

        Response response = given()
        .when()
            .delete("/member/{id}", membroParaTesteId);

        assertEquals(409, response.statusCode());
    }

    @Test
    @Order(40)
    @DisplayName("CT040 - Apagar membro com ID inexistente ou inválido deve falhar")
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
