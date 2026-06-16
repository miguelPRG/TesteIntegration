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
import api.classes.Member;
import static api.helpers.ApiActions.apagarLivro;
import static api.helpers.ApiActions.apagarMembro;
import static api.helpers.ApiActions.criarLivro;
import static api.helpers.ApiActions.criarMembro;
import static api.helpers.ApiActions.criarReserva;
import static api.helpers.DadosTesteFactory.criarLivroValido;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@DisplayName("Testes da Entidade: Member")
@Order(3)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberTest extends BaseTest {

    private Integer membroParaTesteId;
    private Integer membroComReservaAtivaId;
    private Integer livroComReservaAtivaId;

    private int obterOrdemTeste(TestInfo testInfo) {
        return testInfo.getTestMethod()
            .map(method -> method.getAnnotation(Order.class))
            .map(Order::value)
            .orElse(0);
    }

    private void assertMembroTemCamposObrigatorios(Member membro) {
        assertAll(
            () -> assertNotNull(membro.getId()),
            () -> assertNotNull(membro.getFirstName()),
            () -> assertNotNull(membro.getLastName()),
            () -> assertNotNull(membro.getAddress()),
            () -> assertNotNull(membro.getPostalCode()),
            () -> assertNotNull(membro.getCity()),
            () -> assertNotNull(membro.getCountry()),
            () -> assertNotNull(membro.getPhoneNumber()),
            () -> assertNotNull(membro.getNif()),
            () -> assertNotNull(membro.getEmail()),
            () -> assertNotNull(membro.getBirthDate()),
            () -> assertNotNull(membro.getRegistrationDate())
        );
    }

    private void assertMembroCriadoNoSetup(Member membro) {
        assertAll(
            () -> assertNotNull(membro),
            () -> assertEquals(membroParaTesteId, membro.getId()),
            () -> assertEquals("João", membro.getFirstName()),
            () -> assertEquals("Silva", membro.getLastName()),
            () -> assertEquals("Lisboa", membro.getCity()),
            () -> assertEquals("Portugal", membro.getCountry())
        );
    }

    @BeforeEach
    void prepararDadosParaTestesNaoCreate(TestInfo testInfo) {
        int ordemTeste = obterOrdemTeste(testInfo);

        if (ordemTeste >= 21 && ordemTeste <= 24) {
            membroParaTesteId = criarMembro(criarMembroValido());
        }

        if (ordemTeste == 25) {
            membroComReservaAtivaId = criarMembro(criarMembroValido());
            livroComReservaAtivaId = criarLivro(criarLivroValido());
            criarReserva(membroComReservaAtivaId, livroComReservaAtivaId);
        }
    }

    @AfterEach
    void limparDadosCriados() {
        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId, true);
            membroParaTesteId = null;
        }

        if (membroComReservaAtivaId != null) {
            apagarMembro(membroComReservaAtivaId, true);
            membroComReservaAtivaId = null;
        }

        if (livroComReservaAtivaId != null) {
            apagarLivro(livroComReservaAtivaId, true);
            livroComReservaAtivaId = null;
        }
    }

    @Test
    @Order(20)
    @DisplayName("CT020 - Criar um membro com sucesso")
    public void deveCriarMembroComSucesso() {
        membroParaTesteId = criarMembro(criarMembroValido());

        assertTrue(membroParaTesteId > 0);
    }

    @Test
    @Order(21)
    @DisplayName("CT021 - Listar membros com sucesso")
    public void deveListarMembrosComSucesso() {
        List<Member> membros = given()
        .when()
            .get("/member")
        .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("", Member.class);

        assertFalse(membros.isEmpty());

        for (Member membro : membros) {
            assertMembroTemCamposObrigatorios(membro);
        }

        assertTrue(membros.stream().anyMatch(membro -> membroParaTesteId.equals(membro.getId())));
    }

    @Test
    @Order(22)
    @DisplayName("CT022 - Obter um membro existente por id")
    public void deveObterMembroPorIdComSucesso() {
        Member membroObtido = given()
        .when()
            .get("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Member.class);

        assertMembroCriadoNoSetup(membroObtido);
    }

    @Test
    @Order(23)
    @DisplayName("CT023 - Atualizar um membro com sucesso")
    public void deveAtualizarMembroComSucesso() {
        Member membroAtualizado = given()
        .when()
            .get("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Member.class);

        membroAtualizado.setFirstName("Miguel");
        membroAtualizado.setCity("Porto");
        // Ignorar o ID do membro ao atualizar, pois ele não deve ser alterado.
        membroAtualizado.setId(null);
        // Ignorar a data de nascimento para evitar erros de validação
        membroAtualizado.setBirthDate(null);

        String membroAtualizadoIdResponse = given()
            .contentType(ContentType.JSON)
            .body(membroAtualizado)
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            //Temos um erro do lado do servidor(500) quando birthDate é ignorada. Quando não é, dá erro 400 a dizer que a data de nascimento é inválida.
            .statusCode(200)
            .extract()
            .asString();

        Integer membroAtualizadoId = Integer.valueOf(membroAtualizadoIdResponse.trim());

        Member membroObtido = given()
        .when()
            .get("/member/{id}", membroAtualizadoId)
        .then()
            .statusCode(200)
            .extract()
            .as(Member.class);

        assertAll(
            () -> assertEquals(membroParaTesteId, membroObtido.getId()),
            () -> assertEquals("Miguel", membroObtido.getFirstName()),
            () -> assertEquals("Silva", membroObtido.getLastName()),
            () -> assertEquals("Porto", membroObtido.getCity())
        );
    }

    @Test
    @Order(24)
    @DisplayName("CT024 - Apagar um membro com sucesso")
    public void deveApagarMembroComSucesso() {
        given()
        .when()
            .delete("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(204);

        given()
        .when()
            .get("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(404);

        membroParaTesteId = null;
    }

    @Test
    @Order(25)
    @DisplayName("CT025 - Apagar membro com forceRemove true mesmo que haja uma reserva ativa")
    public void deveApagarMembroMesmoComReservaAtiva() {
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/member/{id}", membroComReservaAtivaId)
        .then()
            .statusCode(204);

        membroComReservaAtivaId = null;
    }
}
