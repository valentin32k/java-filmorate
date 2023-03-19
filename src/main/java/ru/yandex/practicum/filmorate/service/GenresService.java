package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenresDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenresService {
    private final GenresDbStorage genresDbStorage;

    public Genre getGenreById(int id) {
        return genresDbStorage.getGenreById(id);
    }

    public List<Genre> getAllGenres() {
        return genresDbStorage.getAllGenres();
    }
}
