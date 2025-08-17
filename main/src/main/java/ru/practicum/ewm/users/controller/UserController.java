package ru.practicum.ewm.users.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.users.NewUserRequest;
import ru.practicum.ewm.users.dto.UserDto;
import ru.practicum.ewm.users.service.UserService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDto addUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("Admin creating new user with email: {}", newUserRequest.getEmail());
        UserDto createdUser = userService.addUser(newUserRequest);
        log.info("Admin successfully created user with ID: {} and email: {}",
                createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) List<Long> ids,
                                  @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                  @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Admin requesting users list with parameters: ids={}, from={}, size={}", ids, from, size);
        List<UserDto> users = userService.getUsers(ids, from, size);
        log.info("Admin retrieved {} users", users.size());
        return users;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Admin deleting user with ID: {}", userId);
        userService.deleteUser(userId);
        log.info("Admin successfully deleted user with ID: {}", userId);
    }
}