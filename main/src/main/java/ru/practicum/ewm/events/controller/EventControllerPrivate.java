package ru.practicum.ewm.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.NewEventDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.events.requests.UpdateEventUserRequest;
import ru.practicum.ewm.events.service.EventService;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventControllerPrivate {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("User ID: {} creating new event with data: {}", userId, newEventDto);
        EventFullDto result = eventService.addEvent(userId, newEventDto);
        log.info("User ID: {} successfully created event ID: {}", userId, result.getId());
        return result;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByOwner(@PathVariable Long userId,
                                           @PathVariable Long eventId,
                                           @RequestBody @Valid UpdateEventUserRequest updateEvent) {
        log.info("User ID: {} updating event ID: {} with data: {}", userId, eventId, updateEvent);
        EventFullDto result = eventService.updateEventByOwner(userId, eventId, updateEvent);
        log.info("User ID: {} successfully updated event ID: {}", userId, eventId);
        return result;
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("User ID: {} updating requests status for event ID: {} with data: {}",
                userId, eventId, request);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(userId, eventId, request);
        log.info("User ID: {} processed {} requests for event ID: {}",
                userId, result.getConfirmedRequests().size() + result.getRejectedRequests().size(), eventId);
        return result;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByOwner(@PathVariable Long userId,
                                                @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Getting events for owner ID: {}, from: {}, size: {}", userId, from, size);
        List<EventShortDto> result = eventService.getEventsByOwnerId(userId, from, size);
        log.info("Found {} events for owner ID: {}", result.size(), userId);
        return result;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("User ID: {} requesting event ID: {}", userId, eventId);
        EventFullDto result = eventService.getEventByOwnerId(userId, eventId);
        log.info("Returning event ID: {} for user ID: {}", eventId, userId);
        return result;
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByEventOwner(@PathVariable Long userId,
                                                                 @PathVariable Long eventId) {
        log.info("User ID: {} requesting participation requests for event ID: {}", userId, eventId);
        List<ParticipationRequestDto> result = requestService.getRequestsByEventOwner(userId, eventId);
        log.info("Found {} participation requests for event ID: {}", result.size(), eventId);
        return result;
    }
}