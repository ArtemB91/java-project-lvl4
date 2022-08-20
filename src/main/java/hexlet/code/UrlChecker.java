package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class UrlChecker {

    public static UrlCheck check(Url url) {

        HttpResponse<String> response = Unirest
                .get(url.getName())
                .asString();
        int statusCode = response.getStatus();

        String body = response.getBody();

        Document document = Jsoup.parse(body);
        String title = document.title();


        Element h1Element = document.selectFirst("h1");
        String h1 = (h1Element != null) ? h1Element.text() : "";

        Element metaElement = document.selectFirst("meta[name=description]");
        String desc = (metaElement != null) ? metaElement.attr("content") : "";

        return new UrlCheck(url, statusCode, title, h1, desc);
    }
}
