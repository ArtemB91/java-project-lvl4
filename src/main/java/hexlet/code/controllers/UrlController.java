package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {

    private UrlController() {
        throw new IllegalStateException("Utility class");
    }

    public static Handler create = ctx -> {
        String urlParam = ctx.formParam("url");

        URL url;
        try {
            url = new URL(urlParam);
        } catch (MalformedURLException e) {
            ctx.res.setStatus(422);
            ctx.sessionAttribute("error", "Некорректный URL");
            ctx.redirect("/");
            return;
        }

        String normalizedUrl = url.getProtocol()
                + "://" + url.getHost()
                + (url.getPort() == -1 ? "" : ":" + url.getPort());

        boolean urlExists = new QUrl()
                .name.equalTo(normalizedUrl)
                .exists();
        if (urlExists) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/urls");
            return;
        }

        Url newUrl = new Url(normalizedUrl);
        newUrl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/urls");

    };

    public static Handler getAll = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> urlPagedList = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = urlPagedList.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    public static Handler getOne = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        ctx.attribute("url", url);
        ctx.render("urls/show.html");

    };
}
