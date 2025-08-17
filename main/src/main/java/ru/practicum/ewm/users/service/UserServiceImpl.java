package ru.practicum.ewm.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.users.NewUserRequest;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.UserMapper;
import ru.practicum.ewm.users.UserRepository;
import ru.practicum.ewm.users.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(NewUserRequest newUserRequest) {
        log.info("Creating new user with email: {}", newUserRequest.getEmail());
        User user = UserMapper.toUser(newUserRequest);
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> userIds, Integer from, Integer size) {
        log.info("Fetching users with IDs: {}, from: {}, size: {}", userIds, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<UserDto> result;
        if (userIds == null) {
            result = userRepository.findAll(pageable)
                    .map(UserMapper::toUserDto)
                    .getContent();
        } else {
            result = userRepository.findAllByIdIn(userIds, pageable)
                    .stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }

        log.info("Found {} users matching criteria", result.size());
        return result;
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
        log.info("Successfully deleted user with ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    return new NotFoundException("User with id=" + userId + " was not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long userId) {
        log.debug("Checking existence of user with ID: {}", userId);
        boolean exists = userRepository.existsById(userId);
        if (exists) {
            log.debug("User with ID: {} exists", userId);
        } else {
            log.debug("User with ID: {} does not exist", userId);
        }
        return exists;
    }
}