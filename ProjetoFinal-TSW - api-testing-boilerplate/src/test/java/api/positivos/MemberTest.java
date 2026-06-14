package api.positivos;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
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
import api.classes.Book;
import api.classes.BookStatus;
import api.classes.Member;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@DisplayName("Testes da Entidade: Member")
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MemberTest extends BaseTest {

    private static final AtomicLong MEMBER_SEQUENCE = new AtomicLong(System.currentTimeMillis());

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

        if (!testePrecisaDeMembroExistente) {
            return;
        }

        membroParaTesteId = criarMembro(criarMembroValido());
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
        criarReservaParaTeste();

        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/member/{id}", membroParaTesteId)
        .then()
            .statusCode(204);

        membroParaTesteId = null;
    }
}
