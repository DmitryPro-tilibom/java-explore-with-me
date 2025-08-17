package ru.practicum.ewm.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.service.RequestService;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("User ID: {} creating new participation request for event ID: {}", userId, eventId);
        ParticipationRequestDto result = requestService.addRequest(userId, eventId);
        log.info("Created participation request ID: {} for user ID: {} to event ID: {}",
                result.getId(), userId, eventId);
        return result;
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("User ID: {} canceling request ID: {}", userId, requestId);
        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);
        log.info("Request ID: {} canceled by user ID: {}, new status: {}",
                requestId, userId, result.getStatus());
        return result;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByUser(@PathVariable Long userId) {
        log.info("Getting all participation requests for user ID: {}", userId);
        List<ParticipationRequestDto> result = requestService.getRequestsByUser(userId);
        log.info("Found {} participation requests for user ID: {}", result.size(), userId);
        return result;
    }
}