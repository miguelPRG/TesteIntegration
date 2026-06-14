package api.helpers;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import api.classes.Book;
import api.classes.Member;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public final class ApiActions {

    private ApiActions() {
    }

    public static Integer criarLivro(Book livro) {
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

    public static Integer criarMembro(Member membro) {
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

    public static void criarReserva(Integer membroId, Integer livroId) {
        given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroId, livroId)
        .then()
            .statusCode(201);
    }

    public static void apagarLivro(Integer livroId) {
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/book/{id}", livroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
    }

    public static void apagarMembro(Integer membroId) {
        given()
            .queryParam("forceRemove", true)
        .when()
            .delete("/member/{id}", membroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
    }
}
