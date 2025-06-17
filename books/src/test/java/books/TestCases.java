package books;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.hamcrest.Matchers.equalTo;

public class TestCases {
    BaseTests baseTests = new BaseTests();

    private static RequestSpecification requestSpec;


    @BeforeClass
    public static void createRequestSpecification() {

        requestSpec = new RequestSpecBuilder().
                setBaseUri("https://simple-books-api.glitch.me").
                setContentType(ContentType.JSON).
                build();

    }

    @Before
    public void serUp() {
        String token = createNewToken();
        baseTests.setToken("Bearer " + token);
    }

    public String createNewToken() {
        String randomString = UUID.randomUUID().toString();

        String clientName = "client_" + randomString.substring(0, 8);
        String clientEmail = "email_" + randomString.substring(9, 13) + "@example.com";

        String authenticationBody = String.format("""
                {
                  "clientName": "%s",
                  "clientEmail": "%s"
                }
                """, clientName, clientEmail);


        baseTests.setBody(authenticationBody);

        String token =
                given().log().all().
                        spec(requestSpec).
                        body(baseTests.getBody()).
                        when().
                        post("/api-clients/").
                        then().
                        assertThat().
                        statusCode(201).
                        and().
                        extract().
                        path("accessToken");

        return token;
    }

    @Test
    public void validateAllBooksJsonScheme() {

        given().log().all().
                spec(requestSpec).
                when().
                get("/books").
                then().
                assertThat().
                body(matchesJsonSchemaInClasspath("allBooksScheme.json"));

    }

    @Test
    public void validateSpecificBookJsonScheme() {

        baseTests.setBookId(1);
        given().log().all().
                spec(requestSpec).
                when().
                get("/books/" + (baseTests.getBookId())).
                then().
                body(matchesJsonSchemaInClasspath("specificBookScheme.json"));
    }

    @Test
    public void validatePlacingNewOrder() {

        int bookId = given().spec(requestSpec)
                .when().get("/books")
                .then().statusCode(200)
                .extract().path("[0].id");

        String customerName = "cust" + UUID.randomUUID().toString().substring(0, 6);

        String newBookBody = String.format("""
                {
                  "bookId": %d,
                  "customerName": "%s"
                }
                """, bookId, customerName);

        baseTests.setBody(newBookBody);
        String orderId =
                given().log().all().
                        spec(requestSpec).
                        header("Authorization", baseTests.getToken()).
                        body(baseTests.getBody()).
                        when().
                        post("/orders").
                        then().
                        assertThat().
                        statusCode(201).
                        and().
                        extract().
                        path("orderId");
        baseTests.setOrderId(orderId);

        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).

                when().
                get("/orders/" + baseTests.getOrderId()).
                then().
                assertThat().
                statusCode(200);

    }

    @Test
    public void validateDeletingAnOrder() {

        int bookId = given().spec(requestSpec)
                .when().get("/books")
                .then().statusCode(200)
                .extract().path("[0].id");

        String customerName = "cust" + UUID.randomUUID().toString().substring(0, 6);

        String newBookBody = String.format("""
                {
                  "bookId": %d,
                  "customerName": "%s"
                }
                """, bookId, customerName);

        baseTests.setBody(newBookBody);
        String orderId =
                given().log().all().
                        spec(requestSpec).
                        header("Authorization", baseTests.getToken()).
                        body(baseTests.getBody()).
                        when().
                        post("/orders").
                        then().
                        assertThat().
                        statusCode(201).
                        and().
                        extract().
                        path("orderId");
        baseTests.setOrderId(orderId);

        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).

                when().
                get("/orders/" + baseTests.getOrderId()).
                then().
                assertThat().
                statusCode(200);


        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).
                when().
                delete("/orders/" + baseTests.getOrderId()).
                then().
                assertThat().
                statusCode(204);


        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).

                when().
                get("/orders/" + baseTests.getOrderId()).
                then().
                assertThat().
                statusCode(404);

    }

    @Test
    public void validatePostingNewOrderWithoutBody() {

        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).
                when().
                post("/orders").
                then().
                assertThat().
                statusCode(400);

    }


    @Test
    public void validatePostingNewBookWithoutToken() {

        int bookId = given().spec(requestSpec)
                .when().get("/books")
                .then().statusCode(200)
                .extract().path("[0].id");

        String customerName = "cust" + UUID.randomUUID().toString().substring(0, 6);

        String newBookBody = String.format("""
                {
                  "bookId": %d,
                  "customerName": "%s"
                }
                """, bookId, customerName);

        baseTests.setBody(newBookBody);
        given().log().all().
                spec(requestSpec).
                body(baseTests.getBody()).
                when().
                post("/orders").
                then().
                assertThat().
                statusCode(401);


    }


    @Test
    public void validateUpdatingPostedBook() {

        int bookId = given().spec(requestSpec)
                .when().get("/books")
                .then().statusCode(200)
                .extract().path("[0].id");

        String customerName = "cust" + UUID.randomUUID().toString().substring(0, 6);

        String newBookBody = String.format("""
                {
                  "bookId": %d,
                  "customerName": "%s"
                }
                """, bookId, customerName);

        baseTests.setBody(newBookBody);
        String orderId =
                given().log().all().
                        spec(requestSpec).
                        header("Authorization", baseTests.getToken()).
                        body(baseTests.getBody()).
                        when().
                        post("/orders").
                        then().
                        assertThat().
                        statusCode(201).
                        and().
                        extract().
                        path("orderId");
        baseTests.setOrderId(orderId);


        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).
                body("""
                          {
                          "customerName": "Sanaa"
                        }
                        """).
                when().
                patch("/orders/" + baseTests.getOrderId()).
                then().
                assertThat().
                statusCode(204);


        given().log().all().
                spec(requestSpec).
                header("Authorization", baseTests.getToken()).

                when().
                get("/orders/" + baseTests.getOrderId()).
                then().
                assertThat().
                statusCode(200).
                body("customerName", equalTo("Sanaa"));

    }


}
