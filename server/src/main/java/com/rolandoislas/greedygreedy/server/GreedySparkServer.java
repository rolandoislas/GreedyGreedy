package com.rolandoislas.greedygreedy.server;

import spark.HaltException;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GreedySparkServer {
    static String getIndex(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "index.hbs"));
    }

    private static String getPartialWithHtmlLineBreaks(String fileName, boolean replaceOnlyDouble) throws HaltException {
        InputStream fileStream = ClassLoader.getSystemResourceAsStream("templates/partial/" + fileName);
        String fileString = new BufferedReader(new InputStreamReader(fileStream))
                .lines().collect(Collectors.joining("\n"));
        fileString = fileString.replaceAll(replaceOnlyDouble ? "\n\n" : "\n", replaceOnlyDouble ? "<br><br>" : "<br>");
        return fileString;
    }

    static String getInfoIndex(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        model.put("text", getPartialWithHtmlLineBreaks("license.txt", true));
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "info.hbs"));
    }

    static String getInfoPrivacy(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        model.put("text", getPartialWithHtmlLineBreaks("privacy.txt", false));
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "info_privacy.hbs"));
    }

    static String getInfoOss(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        model.put("text", getPartialWithHtmlLineBreaks("third-party.txt", false));
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "info_oss.hbs"));
    }

    static String getDownloadIndex(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "download.hbs"));
    }

    static String getGameIndex(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "game.hbs"));
    }
}
