package api.positivos;

import java.time.LocalDateTime;
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
@Order(5)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservaTest extends BaseTest {

    private static Integer livroParaTesteId;
    private static Integer membroParaTesteId;
    private static Integer reservaParaTesteId;

    private void assertReservaTemCamposObrigatorios(Reserva reserva) {
        assertAll(
            () -> assertNotNull(reserva.getId()),
            () -> assertNotNull(reserva.getMemberId()),
            () -> assertNotNull(reserva.getBookId()),
            () -> assertNotNull(reserva.getReservationDate())
        );
    }

    private void assertReservaCriadaNoSetup(Reserva reserva) {
        assertReservaTemCamposObrigatorios(reserva);
        assertAll(
            () -> assertEquals(reservaParaTesteId, reserva.getId()),
            () -> assertEquals(membroParaTesteId, reserva.getMemberId()),
            () -> assertEquals(livroParaTesteId, reserva.getBookId())
        );
    }

    private void assertListaContemReservaCriadaNoSetup(List<Reserva> reservas) {
        assertTrue(reservas.stream().anyMatch(r ->
            r.getId().equals(reservaParaTesteId)
            && r.getMemberId().equals(membroParaTesteId)
            && r.getBookId().equals(livroParaTesteId)
        ));
    }

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
    @Order(41)
    @DisplayName("CT041 - Deve criar uma reserva com sucesso")
    void deveCriarReservaComSucesso() {
        assertNotNull(reservaParaTesteId);
    }

    @Test
    @Order(42)
    @DisplayName("CT042 - Listar todas as reservas ativas com sucesso")
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
            assertReservaTemCamposObrigatorios(reservas.get(i));
        }
        
        assertListaContemReservaCriadaNoSetup(reservas);
    }

    @Test
    @Order(43)
    @DisplayName("CT043 - Obter uma reserva por ID com sucesso")
    void deveObterReservaPorIdComSucesso() {
        
        Reserva reserva = given()
        .when()
            .get("/reservation/{id}", reservaParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Reserva.class);
        
        assertReservaCriadaNoSetup(reserva);
        assertTrue(reserva.getReturnDate() == null || reserva.getReturnDate().isEmpty());
    }

    @Test
    @Order(44)
    @DisplayName("CT044 - Obter uma reserva por id do membro com sucesso")
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
            assertReservaTemCamposObrigatorios(reservas.get(i));
        }
        
        assertListaContemReservaCriadaNoSetup(reservas);
    }
    
    @Test
    @Order(45)
    @DisplayName("CT045 - Obter uma reserva por id do livro com sucesso")
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
            assertReservaTemCamposObrigatorios(reservas.get(i));
        }

        assertListaContemReservaCriadaNoSetup(reservas);
    }

    @Test
    @Order(46)
    @DisplayName("CT046 - Atualizar uma reserva com sucesso")
    void deveAtualizarReservaComSucesso() {

        // Sacamos a reserva antes de atualizar
        Reserva reservaAntesDaAtualizacao = given()
        .when()
            .get("/reservation/{id}", reservaParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Reserva.class);

        // Verificamos que como foi uma reserva recém criada, a data de retorno ainda não foi preenchida.
        assertTrue(
            reservaAntesDaAtualizacao.getReturnDate() == null || reservaAntesDaAtualizacao.getReturnDate().isEmpty()
        );
        
        // Atualizamos a reserva e gaurdamos a resposta para validar o que foi retornado.
        Response response = given()
        .when()
            .put("/reservation/{id}", reservaParaTesteId)
        .then()
            // é retonado 200 em vez de 204 que é o que está no documentação
            .statusCode(200)
            .extract()
            .response();
        

        // Sacamos a reserva novamente para verificar se a data de retorno foi preenchida.
        Reserva reservaAtualizada = given()
        .when()
            .get("/reservation/{id}", reservaParaTesteId)
        .then()
            .statusCode(200)
            .extract()
            .as(Reserva.class);

        String novaReturnDate = reservaAtualizada.getReturnDate();

        // Se foi atualizado com sucesso, a reserva passa a ter data de retorno.
        assertNotNull(novaReturnDate);
        assertTrue(
            LocalDateTime.parse(novaReturnDate)
                .isAfter(LocalDateTime.parse(reservaAntesDaAtualizacao.getReservationDate()))
        );

        // Verificamos a data de retorno
        String responseBody = response.asString().trim();

        // A documentação sugere que é retornado o id da reserva atualizada.
        assertFalse(responseBody.isEmpty(), "A API não retornou o id da reserva atualizada.");
        assertEquals(reservaAtualizada.getId(), Integer.valueOf(responseBody));
    }
}
