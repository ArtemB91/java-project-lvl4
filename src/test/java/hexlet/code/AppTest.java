package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        Url existingUrl = new Url("http://www.example.com");
        existingUrl.save();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testUrls() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        String content = response.getBody();

        assertEquals(200, response.getStatus());
        assertTrue(content.contains("http://www.example.com"));
    }

    @Test
    void testCreateUrl() {
        Url url = new Url("https://ya.ru");
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", url.getName())
                .asString();

        assertEquals(302, response.getStatus());

        int urlsInDB = new QUrl()
                .name.equalTo(url.getName())
                .findCount();

        assertEquals(1, urlsInDB);
    }

    @Test
    void testCreateExistingUrl() {
        Url url = new Url("http://www.example.com");
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", url.getName())
                .asString();

        assertEquals(302, response.getStatus());

        int urlsInDB = new QUrl()
                .name.equalTo(url.getName())
                .findCount();

        assertEquals(1, urlsInDB);
    }

    @Test
    void testCreateInvalidUrl() {
        Url url = new Url("hhhh://www.example.com");
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", url.getName())
                .asString();
        assertEquals(302, response.getStatus());

        boolean urlExists = new QUrl()
                .name.equalTo(url.getName())
                .exists();
        assertFalse(urlExists);
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

}
