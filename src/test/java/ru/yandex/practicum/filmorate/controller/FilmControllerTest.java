package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmControllerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    Gson gson = new Gson();
    Film validFilm = new Film("Название фильма", "Описание фильма", "1999-01-01", 1500);

    private HttpResponse<String> sendRequest(Film film, String method) {
        HttpResponse<String> response;
        try {
            String serverAdress = "http://localhost:8081/films";
            String body = gson.toJson(film);
            HttpRequest request;
            if (method.equals("POST")) {
                request = HttpRequest
                        .newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .uri(URI.create(serverAdress))
                        .build();
            } else if (method.equals("PUT")) {
                request = HttpRequest
                        .newBuilder()
                        .PUT(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .uri(URI.create(serverAdress))
                        .build();
            } else {
                throw new RuntimeException("Не правильно указан Http-метод");
            }
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Test
    void addValidFilm() {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film returnedFilm = gson.fromJson(response.body(), Film.class);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(validFilm.getName(), returnedFilm.getName());
        Assertions.assertEquals(validFilm.getDescription(), returnedFilm.getDescription());
        Assertions.assertEquals(validFilm.getReleaseDate(), returnedFilm.getReleaseDate());
        Assertions.assertEquals(validFilm.getDuration(), returnedFilm.getDuration());
    }

    @Test
    void addVoidBodyFilm() {
        Assertions.assertEquals(400, sendRequest(null, "POST").statusCode());
    }

    @Test
    void addFilmWithVoidName() {
        Film sendFilm = new Film("", validFilm.getDescription(), validFilm.getReleaseDate(), validFilm.getDuration());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addFilmWithBigDescription() {
        Film sendFilm = new Film(validFilm.getName(), BIG_FILM_DESCRIPTION, validFilm.getReleaseDate(), validFilm.getDuration());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addFilmWithWrongReleaseDate() {
        Film sendFilm = new Film(validFilm.getName(), validFilm.getDescription(), "1812-09-07", validFilm.getDuration());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addFilmWithWrongDuration() {
        Film sendFilm = new Film(validFilm.getName(), validFilm.getDescription(), validFilm.getReleaseDate(), -1);
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void validFilmUpdate() {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = gson.fromJson(response.body(), Film.class);
        Film updatedFilm = new Film("newName", "newDescription", "2020-01-02", 111);
        updatedFilm.setId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(updatedFilm, gson.fromJson(response.body(), Film.class));
    }

    @Test
    void updateWithVoidName() {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = gson.fromJson(response.body(), Film.class);
        Film updatedFilm = new Film("", "newDescription", "2020-01-02", 111);
        updatedFilm.setId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithBigDescription() {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = gson.fromJson(response.body(), Film.class);
        Film updatedFilm = new Film("newName", BIG_FILM_DESCRIPTION, "2020-01-02", 111);
        updatedFilm.setId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongReleaseDate() {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = gson.fromJson(response.body(), Film.class);
        Film updatedFilm = new Film("newName", "newDescription", "1812-09-07", 111);
        updatedFilm.setId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongDuration() {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = gson.fromJson(response.body(), Film.class);
        Film updatedFilm = new Film("newName", "newDescription", "2020-01-02", -1);
        updatedFilm.setId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    public static final String BIG_FILM_DESCRIPTION =
            "— Скажи-ка, дядя, ведь недаром\n" +
                    "Москва, спаленная пожаром,\n" +
                    "Французу отдана?\n" +
                    "Ведь были ж схватки боевые,\n" +
                    "Да, говорят, еще какие!\n" +
                    "Недаром помнит вся Россия\n" +
                    "Про день Бородина!\n" +
                    "\n" +
                    "— Да, были люди в наше время,\n" +
                    "Не то, что нынешнее племя:\n" +
                    "Богатыри — не вы!\n" +
                    "Плохая им досталась доля:\n" +
                    "Немногие вернулись с поля…\n" +
                    "Не будь на то господня воля,\n" +
                    "Не отдали б Москвы!\n" +
                    "\n" +
                    "Мы долго молча отступали,\n" +
                    "Досадно было, боя ждали,\n" +
                    "Ворчали старики:\n" +
                    "«Что ж мы? на зимние квартиры?\n" +
                    "Не смеют, что ли, командиры\n" +
                    "Чужие изорвать мундиры\n" +
                    "О русские штыки?»\n" +
                    "\n" +
                    "И вот нашли большое поле:\n" +
                    "Есть разгуляться где на воле!\n" +
                    "Построили редут.\n" +
                    "У наших ушки на макушке!\n" +
                    "Чуть утро осветило пушки\n" +
                    "И леса синие верхушки —\n" +
                    "Французы тут как тут.\n" +
                    "\n" +
                    "Забил заряд я в пушку туго\n" +
                    "И думал: угощу я друга!\n" +
                    "Постой-ка, брат мусью!\n" +
                    "Что тут хитрить, пожалуй к бою;\n" +
                    "Уж мы пойдем ломить стеною,\n" +
                    "Уж постоим мы головою\n" +
                    "За родину свою!\n" +
                    "\n" +
                    "Два дня мы были в перестрелке.\n" +
                    "Что толку в этакой безделке?\n" +
                    "Мы ждали третий день.\n" +
                    "Повсюду стали слышны речи:\n" +
                    "«Пора добраться до картечи!»\n" +
                    "И вот на поле грозной сечи\n" +
                    "Ночная пала тень.";
}