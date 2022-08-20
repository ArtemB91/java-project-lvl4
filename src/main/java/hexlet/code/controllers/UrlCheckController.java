package hexlet.code.controllers;


import hexlet.code.UrlChecker;
import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

public final class UrlCheckController {

    private UrlCheckController() {
        throw new IllegalStateException("Utility class");
    }

    public static Handler create = ctx -> {
        Long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(urlId)
                .findOne();

        if (url == null) {
            ctx.status(422);
            return;
        }

        UrlCheck urlCheck = UrlChecker.check(url);
        urlCheck.save();

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.redirect("/urls/" + urlId);
    };

}
