package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserControllerTest {
    private final HttpClient client = HttpClient.newHttpClient();
    Gson gson = new Gson();
    User validUser = new User("email@yandex.ru", "Login", "Name", "1999-01-01");

    private HttpResponse<String> sendRequest(User user, String method) {
        HttpResponse<String> response;
        try {
            String serverAdress = "http://localhost:8080/users";
            String body = gson.toJson(user);
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
    void addValidUser() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User returnedUser = gson.fromJson(response.body(), User.class);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(validUser.getEmail(), returnedUser.getEmail());
        Assertions.assertEquals(validUser.getLogin(), returnedUser.getLogin());
        Assertions.assertEquals(validUser.getName(), returnedUser.getName());
        Assertions.assertEquals(validUser.getBirthday(), returnedUser.getBirthday());
    }

    @Test
    void addVoidBodyUser() {
        Assertions.assertEquals(400, sendRequest(null, "POST").statusCode());
    }

    @Test
    void addUserWithVoidEmail() {
        User sendUser = new User("", validUser.getLogin(), validUser.getName(), validUser.getBirthday());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithWrongEmail() {
        User sendUser = new User("email", validUser.getLogin(), validUser.getName(), validUser.getBirthday());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithVoidLogin() {
        User sendUser = new User(validUser.getEmail(), "", validUser.getName(), validUser.getBirthday());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithWrongLogin() {
        User sendUser = new User(validUser.getEmail(), "Three words login", validUser.getName(), validUser.getBirthday());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void addUserWithVoidName() {
        User sendUser = new User(validUser.getEmail(), validUser.getLogin(), "", validUser.getBirthday());
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        User returnedUser = gson.fromJson(response.body(), User.class);
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
        User sendUser = new User(validUser.getEmail(), validUser.getLogin(), validUser.getName(), birthdayString);
        HttpResponse<String> response = sendRequest(sendUser, "POST");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithVoidEmail() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = gson.fromJson(response.body(), User.class);
        User updatedUser = new User("", "newLogin", "newName", "2020-01-02");
        updatedUser.setId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongEmail() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = gson.fromJson(response.body(), User.class);
        User updatedUser = new User("email", "newLogin", "newName", "2020-01-02");
        updatedUser.setId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithVoidLogin() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = gson.fromJson(response.body(), User.class);
        User updatedUser = new User("newEmail", "", "newName", "2020-01-02");
        updatedUser.setId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateWithWrongLogin() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User tmpUser = gson.fromJson(response.body(), User.class);
        User updatedUser = new User("newEmail", "Three words login", "newName", "2020-01-02");
        updatedUser.setId(tmpUser.getId());
        response = sendRequest(updatedUser, "PUT");
        Assertions.assertEquals(400, response.statusCode());
    }

    @Test
    void updateUserWithVoidName() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User returnedUser = gson.fromJson(response.body(), User.class);
        User tmpUser = new User("new@email.ru", "newLogin", "", "1999-01-01");
        tmpUser.setId(returnedUser.getId());
        response = sendRequest(tmpUser, "PUT");
        User updatedUser = gson.fromJson(response.body(), User.class);
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(tmpUser.getEmail(), updatedUser.getEmail());
        Assertions.assertEquals(tmpUser.getLogin(), updatedUser.getLogin());
        Assertions.assertEquals(tmpUser.getLogin(), updatedUser.getName());
        Assertions.assertEquals(tmpUser.getBirthday(), updatedUser.getBirthday());
    }

    @Test
    void updateUserWithWrongBirthday() {
        HttpResponse<String> response = sendRequest(validUser, "POST");
        User returnedUser = gson.fromJson(response.body(), User.class);
        LocalDate birthday = LocalDate.now().plusDays(1);
        String birthdayString = birthday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        User sendUser = new User(returnedUser.getEmail(), returnedUser.getLogin(), returnedUser.getName(), birthdayString);
        response = sendRequest(sendUser, "PUT");
        Assertions.assertEquals(404, response.statusCode());
    }
}