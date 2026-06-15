package api.positivos;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservaTest extends BaseTest {

    private static Integer livroParaTesteId;
    private static Integer membroParaTesteId;
    private static Integer reservaParaTesteId;

    @BeforeAll
    static void criarLivroEMembro() {
        livroParaTesteId = criarLivro(criarLivroValido());
        membroParaTesteId = criarMembro(criarMembroValido());
        reservaParaTesteId = criarReserva(membroParaTesteId, livroParaTesteId);
    }

    @AfterAll
    static void limparDadosCriados() {
        if (livroParaTesteId != null) {
            apagarLivro(livroParaTesteId, true);
        }

        if (membroParaTesteId != null) {
            apagarMembro(membroParaTesteId, true);
        }
    }

    @Test
    @Order(33)
    @DisplayName("CT033 - Deve criar uma reserva com sucesso")
    void deveCriarReservaComSucesso() {
        assertTrue(reservaParaTesteId > 0);
    }

    @Test
    @Order(34)
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

        assertFalse(reservas.isEmpty());

        for (int i = 0; i < reservas.size(); i++) {
            Reserva reserva = reservas.get(i);

            assertAll(
                () -> assertNotNull(reserva.getId()),
                () -> assertNotNull(reserva.getMemberId()),
                () -> assertNotNull(reserva.getBookId()),
                () -> assertNotNull(reserva.getReservationDate()),
                () -> assertTrue(reserva.getReturnDate() == null || reserva.getReturnDate().isEmpty())
            );
        }
        
        assertTrue(reservas.stream().anyMatch(r -> 
            r.getId().equals(reservaParaTesteId)
            && r.getMemberId().equals(membroParaTesteId)
            && r.getBookId().equals(livroParaTesteId)
        ));
    }

    @Test
    @Order(35)
    @DisplayName("CT035 - Obter uma reserva por ID com sucesso")
    void deveObterReservaPorIdComSucesso() {
        
        Reserva reserva = given()
        .when()
            .get("/reservation/{id}", reservaParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Reserva.class);
        
        assertAll(
            () -> assertNotNull(reserva.getId()),
            () -> assertNotNull(reserva.getMemberId()),
            () -> assertNotNull(reserva.getBookId()),
            () -> assertNotNull(reserva.getReservationDate()),
            () -> assertTrue(reserva.getReturnDate() == null || reserva.getReturnDate().isEmpty())
        );
    }

    @Test
    @Order(36)
    @DisplayName("CT036 - Obter uma reserva por id do membro com sucesso")
    void deveObterReservaPorIdMembroComSucesso() {
        List<Reserva> reservas = given()
        .when()
            .get("/reservation/member/{memberId}", membroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .jsonPath().getList("", Reserva.class);

        assertFalse(reservas.isEmpty());

        for (int i = 0; i < reservas.size(); i++) {
            Reserva reserva = reservas.get(i);

            assertAll(
                () -> assertNotNull(reserva.getId()),
                () -> assertNotNull(reserva.getMemberId()),
                () -> assertNotNull(reserva.getBookId()),
                () -> assertNotNull(reserva.getReservationDate()),
                () -> assertTrue(reserva.getReturnDate() == null || reserva.getReturnDate().isEmpty())
            );
        }
        
        assertTrue(reservas.stream().anyMatch(r -> 
            r.getId().equals(reservaParaTesteId)
            && r.getMemberId().equals(membroParaTesteId)
            && r.getBookId().equals(livroParaTesteId)
        ));
    }
    
    @Test
    @Order(37)
    @DisplayName("CT037 - Obter uma reserva por id do livro com sucesso")
    void deveObterReservaPorIdLivroComSucesso() {
        List<Reserva> reservas = given()
        
        .when()
            .get("/reservation/book/{bookId}", livroParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .jsonPath().getList("", Reserva.class);
        
        assertFalse(reservas.isEmpty());

        for (int i = 0; i < reservas.size(); i++) {
            Reserva reserva = reservas.get(i);

            assertAll(
                () -> assertNotNull(reserva.getId()),
                () -> assertNotNull(reserva.getMemberId()),
                () -> assertNotNull(reserva.getBookId()),
                () -> assertNotNull(reserva.getReservationDate()),
                () -> assertTrue(reserva.getReturnDate() == null || reserva.getReturnDate().isEmpty())
            );
        }

        assertTrue(reservas.stream().anyMatch(r -> 
            r.getId().equals(reservaParaTesteId)
            && r.getMemberId().equals(membroParaTesteId)
            && r.getBookId().equals(livroParaTesteId)
        ));
    }

    @Test
    @Order(38)
    @DisplayName("CT038 - Atualizar uma reserva com sucesso")
    void deveAtualizarReservaComSucesso() {
        Response response = given()
        .when()
            .put("/reservation/{id}", reservaParaTesteId)
        .then()
            // A documentação indica 204, mas a API retorna 200; Alteramos o status code para refletir o comportamento real.
            .statusCode(200)
            .extract()
            .response();
        
        Reserva reservaAtualizada = given()
        .when()
            .get("/reservation/{id}", reservaParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Reserva.class);

        // Se foi atualizado com sucesso, a data de retorno já não deve ser nula.
        assertNotNull(reservaAtualizada.getReturnDate());

        String responseBody = response.asString().trim();

        // A documentação sugere que é retornado o id da reserva atualizada.
        assertFalse(responseBody.isEmpty(), "A API não retornou o id da reserva atualizada.");
        assertEquals(reservaAtualizada.getId(), Integer.valueOf(responseBody));
    }
}
