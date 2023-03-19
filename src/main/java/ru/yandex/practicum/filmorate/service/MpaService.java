package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaDbStorage mpaDbStorage;

    public Mpa getMpaById(int id) {
        return mpaDbStorage.getMpaById(id);
    }

    public List<Mpa> getAllMpa() {
        return mpaDbStorage.getAllMpa();
    }
}
