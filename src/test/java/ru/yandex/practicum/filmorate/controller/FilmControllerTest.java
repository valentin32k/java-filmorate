package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmControllerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    ObjectMapper mapper = new ObjectMapper();

    Film validFilm = new Film(0, "Название фильма", "Описание фильма", "1999-01-01", 1500, new HashSet<>());

    @Test
    void addValidFilm() throws JsonProcessingException {
        sendRequest(validFilm, "POST");
        HttpResponse<String> response = sendRequest(null, "GET");
        List<Film> filmList = mapper.readValue(response.body(), new TypeReference<>() {
        });
        Film returnedFilm = filmList.get(0);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(validFilm.getName(), returnedFilm.getName());
        Assertions.assertEquals(validFilm.getDescription(), returnedFilm.getDescription());
        Assertions.assertEquals(validFilm.getReleaseDate(), returnedFilm.getReleaseDate());
        Assertions.assertEquals(validFilm.getDuration(), returnedFilm.getDuration());
    }

    @Test
    void addVoidBodyFilm() {
        Assertions.assertEquals(500, sendRequest(null, "POST").statusCode());
    }

    @Test
    void addFilmWithVoidName() {
        Film sendFilm = new Film(0, "", validFilm.getDescription(), validFilm.getReleaseDate(), validFilm.getDuration(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        System.out.println(response.statusCode());
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addFilmWithBigDescription() {
        Film sendFilm = new Film(0, validFilm.getName(), BIG_FILM_DESCRIPTION, validFilm.getReleaseDate(), validFilm.getDuration(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addFilmWithWrongReleaseDate() {
        Film sendFilm = new Film(0, validFilm.getName(), validFilm.getDescription(), "1812-09-07", validFilm.getDuration(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addFilmWithWrongDuration() {
        Film sendFilm = new Film(0, validFilm.getName(), validFilm.getDescription(), validFilm.getReleaseDate(), -1, new HashSet<>());
        HttpResponse<String> response = sendRequest(sendFilm, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void validFilmUpdate() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = mapper.readValue(response.body(), Film.class);
        Film updatedFilm = new Film(0, "newName", "newDescription", "2020-01-02", 111, new HashSet<>());
        updatedFilm = updatedFilm.withId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(updatedFilm, mapper.readValue(response.body(), Film.class));
    }

    @Test
    void updateWithVoidName() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = mapper.readValue(response.body(), Film.class);
        Film updatedFilm = new Film(0, "", "newDescription", "2020-01-02", 111, new HashSet<>());
        updatedFilm = updatedFilm.withId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithBigDescription() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = mapper.readValue(response.body(), Film.class);
        Film updatedFilm = new Film(0, "newName", BIG_FILM_DESCRIPTION, "2020-01-02", 111, new HashSet<>());
        updatedFilm = updatedFilm.withId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongReleaseDate() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = mapper.readValue(response.body(), Film.class);
        Film updatedFilm = new Film(0, "newName", "newDescription", "1812-09-07", 111, new HashSet<>());
        updatedFilm = updatedFilm.withId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongDuration() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validFilm, "POST");
        Film tmpFilm = mapper.readValue(response.body(), Film.class);
        Film updatedFilm = new Film(0, "newName", "newDescription", "2020-01-02", -1, new HashSet<>());
        updatedFilm = updatedFilm.withId(tmpFilm.getId());
        response = sendRequest(updatedFilm, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongId() {
        Film wrongIdFilm = validFilm.withId(10);
        Assertions.assertEquals(404, sendRequest(wrongIdFilm, "PUT").statusCode());
    }

    private HttpResponse<String> sendRequest(Film film, String method) {
        HttpResponse<String> response;
        try {
            String serverAdress = "http://localhost:8080/films";
            String body;
            HttpRequest request;
            if ("POST".equals(method)) {
                body = mapper.writeValueAsString(film);
                request = HttpRequest
                        .newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .uri(URI.create(serverAdress))
                        .build();
            } else if ("PUT".equals(method)) {
                body = mapper.writeValueAsString(film);
                request = HttpRequest
                        .newBuilder()
                        .PUT(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .uri(URI.create(serverAdress))
                        .build();
            } else if ("GET".equals(method)) {
                request = HttpRequest
                        .newBuilder()
                        .GET()
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

    public static final String BIG_FILM_DESCRIPTION = StringUtils.repeat("1", 201);
}