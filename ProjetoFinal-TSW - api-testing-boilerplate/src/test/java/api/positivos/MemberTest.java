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
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MemberTest extends BaseTest {

    private Integer membroParaTesteId;
    private Integer livroParaTesteId;

    @BeforeEach
    void criarMembroQuandoNecessario(TestInfo testInfo) {
        boolean testePrecisaDeMembroExistente = testInfo.getTestMethod()
            .map(method -> method.getName().equals("deveListarMembrosComSucesso")
                || method.getName().equals("deveObterMembroPorIdComSucesso")
                || method.getName().equals("deveAtualizarMembroComSucesso")
                || method.getName().equals("deveApagarMembroComSucesso")
            )
            .orElse(false);

        if (testePrecisaDeMembroExistente) {
            membroParaTesteId = criarMembro(criarMembroValido());
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
    }

    @Test
    @DisplayName("CT016 - Criar um membro com sucesso")
    public void deveCriarMembroComSucesso() {
        membroParaTesteId = criarMembro(criarMembroValido());
    }

    @Test
    @DisplayName("CT017 - Listar membros com sucesso")
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

        assertTrue(membros.stream().anyMatch(membro -> membroParaTesteId.equals(membro.getId())));
    }

    @Test
    @DisplayName("CT018 - Obter um membro existente por id")
    public void deveObterMembroPorIdComSucesso() {
        Member membroObtido = given()
        .when()
            .get("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Member.class);

        assertAll(
            () -> assertNotNull(membroObtido),
            () -> assertEquals(membroParaTesteId, membroObtido.getId()),
            () -> assertEquals("João", membroObtido.getFirstName()),
            () -> assertEquals("Silva", membroObtido.getLastName()),
            () -> assertEquals("Lisboa", membroObtido.getCity()),
            () -> assertEquals("Portugal", membroObtido.getCountry())
        );
    }

    @Test
    @DisplayName("CT019 - Atualizar um membro com sucesso")
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

        Integer membroAtualizadoId = given()
            .contentType(ContentType.JSON)
            .body(membroAtualizado)
        .when()
            .put("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Integer.class);

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
    @DisplayName("CT020 - Apagar um membro com sucesso")
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
    @DisplayName("CT021 - Apagar membro com forceRemove true mesmo que haja uma reserva ativa")
    public void deveApagarMembroMesmoComReservaAtiva() {
        membroParaTesteId = criarMembro(criarMembroValido());
        livroParaTesteId = criarLivro(criarLivroValido());
        criarReserva(membroParaTesteId, livroParaTesteId);

        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(204);

        membroParaTesteId = null;
    }
}
