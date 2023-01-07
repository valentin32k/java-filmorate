package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserControllerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    ObjectMapper mapper = new ObjectMapper();
    User validUser = new User(0, "email@yandex.ru", "Login", "Name", "1999-01-01", new HashSet<>());


    @Test
    void addValidUser() throws JsonProcessingException {
        sendRequest(validUser, "POST");
        HttpResponse<String> response = sendRequest(null, "GET");
        List<User> usersList = mapper.readValue(response.body(), new TypeReference<>() {
        });
        User returnedUser = usersList.get(0);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(validUser.getEmail(), returnedUser.getEmail());
        Assertions.assertEquals(validUser.getLogin(), returnedUser.getLogin());
        Assertions.assertEquals(validUser.getName(), returnedUser.getName());
        Assertions.assertEquals(validUser.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void addVoidBodyUser() {
        Assertions.assertEquals(500, sendRequest(null, "POST").statusCode());
    }

    @Test
    void addUserWithVoidEmail() {
        User sendUser = new User(0, "", validUser.getLogin(), validUser.getName(), validUser.getBirthday(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithWrongEmail() {
        User sendUser = new User(0, "email", validUser.getLogin(), validUser.getName(), validUser.getBirthday(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithVoidLogin() {
        User sendUser = new User(0, validUser.getEmail(), "", validUser.getName(), validUser.getBirthday(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithWrongLogin() {
        User sendUser = new User(0, validUser.getEmail(), "Three words login", validUser.getName(), validUser.getBirthday(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithVoidName() throws JsonProcessingException {
        User sendUser = new User(0, validUser.getEmail(), validUser.getLogin(), "", validUser.getBirthday(), new HashSet<>());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        User returnedUser = mapper.readValue(response.body(), User.class);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(validUser.getEmail(), returnedUser.getEmail());
        Assertions.assertEquals(validUser.getLogin(), returnedUser.getLogin());
        Assertions.assertEquals(validUser.getLogin(), returnedUser.getName());
        Assertions.assertEquals(validUser.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void addUserWithWrongBirthday() {
        LocalDate birthday = LocalDate.now().plusDays(1);
        String birthdayString = birthday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        User sendUser = new User(0, validUser.getEmail(), validUser.getLogin(), validUser.getName(), birthdayString, new HashSet<>());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongId() {
        User wrongIdUser = validUser.withId(100);
        Assertions.assertEquals(404, sendRequest(wrongIdUser, "PUT").statusCode());
    }

    @Test
    void updateWithVoidEmail() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = mapper.readValue(response.body(), User.class);
        User updatedUser = new User(0, "", "newLogin", "newName", "2020-01-02", new HashSet<>());
        updatedUser = updatedUser.withId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongEmail() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = mapper.readValue(response.body(), User.class);
        User updatedUser = new User(0, "email", "newLogin", "newName", "2020-01-02", new HashSet<>());
        updatedUser = updatedUser.withId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithVoidLogin() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = mapper.readValue(response.body(), User.class);
        User updatedUser = new User(0, "newEmail", "", "newName", "2020-01-02", new HashSet<>());
        updatedUser = updatedUser.withId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongLogin() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = mapper.readValue(response.body(), User.class);
        User updatedUser = new User(0, "newEmail", "Three words login", "newName", "2020-01-02", new HashSet<>());
        updatedUser = updatedUser.withId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateUserWithVoidName() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User returnedUser = mapper.readValue(response.body(), User.class);
        User tmpUser = new User(0, "new@email.ru", "newLogin", "", "1999-01-01", new HashSet<>());
        tmpUser = tmpUser.withId(returnedUser.getId());
        response = sendRequest(tmpUser, "PUT");
        User updatedUser = mapper.readValue(response.body(), User.class);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(tmpUser.getEmail(), updatedUser.getEmail());
        Assertions.assertEquals(tmpUser.getLogin(), updatedUser.getLogin());
        Assertions.assertEquals(tmpUser.getLogin(), updatedUser.getName());
        Assertions.assertEquals(tmpUser.getBirthday(), updatedUser.getBirthday());
    }

    @Test
    void updateUserWithWrongBirthday() throws JsonProcessingException {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User returnedUser = mapper.readValue(response.body(), User.class);
        LocalDate birthday = LocalDate.now().plusDays(1);
        String birthdayString = birthday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        User sendUser = new User(returnedUser.getId(), returnedUser.getEmail(), returnedUser.getLogin(), returnedUser.getName(), birthdayString, new HashSet<>());
        response = sendRequest(sendUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    private HttpResponse<String> sendRequest(User user, String method) {
        HttpResponse<String> response;
        try {
            String serverAdress = "http://localhost:8080/users";
            String body;
            HttpRequest request;
            if ("POST".equals(method)) {
                body = mapper.writeValueAsString(user);
                request = HttpRequest
                        .newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .uri(URI.create(serverAdress))
                        .build();
            } else if ("PUT".equals(method)) {
                body = mapper.writeValueAsString(user);
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
}