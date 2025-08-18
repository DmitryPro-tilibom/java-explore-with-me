package ru.practicum.ewm.users.service;

import ru.practicum.ewm.users.NewUserRequest;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> userIds, Integer from, Integer size);

    void deleteUser(Long userId);

    User getUserById(Long userId);
}