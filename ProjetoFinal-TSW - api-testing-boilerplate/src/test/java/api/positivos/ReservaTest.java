package api.positivos;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import api.BaseTest;
import api.classes.Reserva;
import static api.helpers.ApiActions.apagarLivro;
import static api.helpers.ApiActions.apagarMembro;
import static api.helpers.ApiActions.criarLivro;
import static api.helpers.ApiActions.criarMembro;
import static api.helpers.ApiActions.criarReserva;
import static api.helpers.DadosTesteFactory.criarLivroValido;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static io.restassured.RestAssured.given;
import io.restassured.response.Response;

@DisplayName("Testes da Entidade: Reserva")
public class ReservaTest extends BaseTest {

    private static Integer livroParaTesteId;
    private static Integer membroParaTesteId;
    private static Integer livroComReservaAtivaId;
    private static Integer membroComReservaAtivaId;

    @BeforeAll
    static void criarLivroEMembro() {
        livroParaTesteId = criarLivro(criarLivroValido());
        membroParaTesteId = criarMembro(criarMembroValido());

        livroComReservaAtivaId = criarLivro(criarLivroValido());
        membroComReservaAtivaId = criarMembro(criarMembroValido());
        criarReserva(membroComReservaAtivaId, livroComReservaAtivaId);
    }

    @AfterAll
    static void limparDadosCriados() {
        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId);
        }

        if (livroComReservaAtivaId != null) {
            apagarLivro(livroComReservaAtivaId);
        }

        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId);
        }

        if (membroComReservaAtivaId != null) {
            apagarMembro(membroComReservaAtivaId);
        }
    }

    @Test
    @DisplayName("CT033 - Deve criar uma reserva com sucesso")
    void deveCriarReservaComSucesso() {
        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroParaTesteId, livroParaTesteId)
        .then()
            .statusCode(201)
            .body(notNullValue());
    }

    @Test
    @DisplayName("CT034 - Listar todas as reservas ativas com sucesso")
    void deveListarTodasReservasAtivasComSucesso() {
        Response response = given()
        .when()
            .get("/reservation")
        .then()
            .statusCode(200)
            .extract()
            .response();

        List<Reserva> reservas = response.jsonPath().getList("", Reserva.class);
        List<Map<String, Object>> reservasJson = response.jsonPath().getList("");

        assertFalse(reservas.isEmpty());

        for (int i = 0; i < reservas.size(); i++) {
            Reserva reserva = reservas.get(i);
            Map<String, Object> reservaJson = reservasJson.get(i);

            assertAll(
                () -> assertNotNull(reserva.getId()),
                () -> assertNotNull(reserva.getMemberId()),
                () -> assertNotNull(reserva.getBookId()),
                () -> assertNotNull(reserva.getReservationDate()),
                () -> assertTrue(reservaJson.containsKey("retur
        assertTrue(reservas.stream().anyMatch(reserva ->
            membroComReservaAtivaId.equals(reserva.getMemberId())
                && livroComReservaAtivaId.equals(reserva.getBookId())
        ));
    }
}
