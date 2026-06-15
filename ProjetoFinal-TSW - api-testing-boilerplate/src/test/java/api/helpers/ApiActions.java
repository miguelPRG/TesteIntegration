package api.helpers;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import api.classes.Book;
import api.classes.Member;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


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

    public static Integer criarReserva(Integer membroId, Integer livroId) {
        
        // Como não conseguimos apagar reservas, vamos garatinr que não criamos mais nenhuma caso exista pelo menos uma
        
        Response response = given()
        .when()
            .get("/reservation")
        .then()
            .statusCode(200)
            .extract()
            .response();        
        
        return given()
        .when()
            .post("/reservation/member/{memberId}/book{bookId}", membroId, livroId)
        .then()
            .statusCode(201)
            .extract()
            .as(Integer.class);
    }

    public static void apagarLivro(Integer livroId, boolean forceRemove) {
        given()
            .queryParam("forceRemove", forceRemove)
        .when()
            .delete("/book/{id}", livroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
    }

    public static void apagarLivro(Integer livroId) {
        apagarLivro(livroId, true);
    }

    public static void apagarMembro(Integer membroId, boolean forceRemove) {
        given()
            .queryParam("forceRemove", forceRemove)
        .when()
            .delete("/member/{id}", membroId)
        .then()
            .statusCode(anyOf(equalTo(204), equalTo(404)));
    }

    public static void apagarMembro(Integer membroId) {
        apagarMembro(membroId, true);
    }

}
