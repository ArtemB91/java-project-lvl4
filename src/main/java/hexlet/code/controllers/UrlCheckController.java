package hexlet.code.controllers;


import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class UrlCheckController {
    public static Handler addCheck = ctx -> {
        Long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(urlId)
                .findOne();
        if (url == null) {
            ctx.status(422);
            return;
        }

        HttpResponse<String> response = Unirest
                .head(url.getName())
                .asString();

        UrlCheck urlCheck = new UrlCheck(url, response.getStatus(), "", "", "");
        urlCheck.save();

        ctx.redirect("/urls/" + urlId);
    };

}
