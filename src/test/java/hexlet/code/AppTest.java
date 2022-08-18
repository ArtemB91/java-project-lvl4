package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.Transaction;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AppTest {

    private static MockWebServer mockWebServer;
    private static String mockUrl;
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;


        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setResponseCode(410));
        mockWebServer.start();
        mockUrl = mockWebServer.url("/").toString();

        Url existingUrl = new Url("http://www.example.com");
        existingUrl.save();
        new UrlCheck(existingUrl, 600, "testTitle", "testH1", "testDesc").save();
        new Url(mockUrl).save();
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



    @Test
    void testChecks() {
        Long urlId = new QUrl()
                .name.equalTo("http://www.example.com")
                .findOne()
                .getId();

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/" + urlId)
                .asString();
        String content = response.getBody();

        assertEquals(200, response.getStatus());

        assertTrue(content.contains("600"));
        assertTrue(content.contains("testTitle"));
        assertTrue(content.contains("testH1"));
        assertTrue(content.contains("testDesc"));

    }
    @Test
    void testAddCheck() {

        Url url = new QUrl()
                .name.equalTo(mockUrl)
                .findOne();

        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls/" + url.getId() + "/checks")
                .asString();

        assertEquals(302, response.getStatus());

        UrlCheck urlCheck = new QUrlCheck()
                .url.equalTo(url)
                .findOne();

        assertNotNull(urlCheck);
        assertEquals(410, urlCheck.getStatusCode());
        assertNotNull(urlCheck.getCreatedAt());
        assertEquals(url.getId(), urlCheck.getUrl().getId());
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.close();
    }

}
