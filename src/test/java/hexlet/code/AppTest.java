package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private final Path fixturesDir = Paths.get("src", "test", "resources", "fixtures");

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        Url existingUrl = new Url("http://www.example.com");
        existingUrl.save();
        new UrlCheck(existingUrl, 600, "testTitle", "testH1", "testDesc").save();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockUrl = mockWebServer.url("/").toString();

        boolean mockUrlExists = new QUrl()
                .name.equalTo(mockUrl)
                .exists();

        if (!mockUrlExists) {
            new Url(mockUrl).save();
        }
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() throws IOException {
        mockWebServer.shutdown();
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
    void testCreateCheck() throws IOException {

        String responseBody = Files.readString(fixturesDir.resolve("page1.html"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody));

        Url url = new QUrl()
                .name.equalTo(mockUrl)
                .findOne();

        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls/" + url.getId() + "/checks")
                .asString();

        assertEquals(302, response.getStatus());

        UrlCheck urlCheck = new QUrlCheck()
                .url.equalTo(url)
                .statusCode.equalTo(200)
                .title.equalTo("Test title")
                .h1.equalTo("Test H1")
                .description.equalTo("Test description")
                .findOne();

        assertNotNull(urlCheck);
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

}
