package api.negativos;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import api.BaseTest;
import static api.helpers.ApiActions.apagarLivro;
import static api.helpers.ApiActions.apagarMembro;
import static api.helpers.ApiActions.criarLivro;
import static api.helpers.ApiActions.criarMembro;
import static api.helpers.ApiActions.criarReserva;
import static api.helpers.DadosTesteFactory.criarLivroValido;
import static api.helpers.DadosTesteFactory.criarMembroValido;
import static io.restassured.RestAssured.given;

@DisplayName("Testes Negativos da Entidade: Reserva")
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
    @Order(41)
    @DisplayName("CT041 - Não deve criar uma reserva com id de membro e de livro inexistentes ou inválidos")
    void naoDeveCriarReservaComIdMembroEIdLivroInexistentesOuInvalidos() {
        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", 999999, livroParaTesteId)
        .then()
            .statusCode(404);

        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroParaTesteId, 999999)
        .then()
            .statusCode(404);

        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", "idInvalido", livroParaTesteId)
        .then()
            .statusCode(400);

        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroParaTesteId, "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(42)
    @DisplayName("CT042 - Não deve criar uma reserva para um livro que já está reservado")
    void naoDeveCriarReservaParaLivroJaReservado() {
        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroParaTesteId, livroParaTesteId)
        .then()
            // A documentação não menciona 409 para este cenário; espera-se 400 conforme contrato documentado.
            .statusCode(400);
    }

    //GET

    @Test
    @Order(43)
    @DisplayName("CT043 - Não deve sacar uma reserva com id inexistente ou inválido")
    void naoDeveSacarReservaComIdInexistenteOuInvalido() {
        given()
        .when()
            .get("/reservation/{id}", 999999)
        .then()
            .statusCode(404);

        given()
        .when()
            .get("/reservation/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(44)
    @DisplayName("CT044 - Não deve obter reservas por id de membro inexistente ou inválido")
    void naoDeveObterReservasPorIdMembroInexistenteOuInvalido() {
        given()
        .when()
            .get("/reservation/member/{memberId}", 999999)
        .then()
            .statusCode(404);
        
        given()
        .when()
            .get("/reservation/member/{memberId}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(45)
    @DisplayName("CT045 - Não deve obter reservas por id de livro inexistente ou inválido")
    void naoDeveObterReservasPorIdLivroInexistenteOuInvalido() {
        given()
        .when()
            .get("/reservation/book/{bookId}", 999999)
        .then()
            .statusCode(404);   
        
        given()
        .when()
            .get("/reservation/book/{bookId}", "idInvalido")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(46)
    @DisplayName("CT046 - Não deve atualizar uma reserva com id inexistente ou inválido")
    void naoDeveAtualizarReservaComIdInexistenteOuInvalido() {
        given()
        .when()
            .put("/reservation/{id}", 999999)
        .then()
            .statusCode(404);

        given()
        .when()
            .put("/reservation/{id}", "idInvalido")
        .then()
            .statusCode(400);
    }

}
