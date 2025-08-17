package ru.practicum.ewm.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventShortDtoWithViews;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventControllerPublic {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDtoWithViews> getEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
                                                  LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
                                                  LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                  @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero
                                                  Integer from,
                                                  @RequestParam(value = "size", defaultValue = "10") @Positive
                                                  Integer size,
                                                  HttpServletRequest request) {
        log.info("Public event search request - text: '{}', categories: {}, paid: {}, " +
                        "rangeStart: {}, rangeEnd: {}, onlyAvailable: {}, sort: {}, from: {}, size: {}, " +
                        "client IP: {}, request URI: {}",
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size,
                request.getRemoteAddr(), request.getRequestURI());

        List<EventShortDtoWithViews> result = eventService.getEvents(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request);

        log.info("Returning {} events for public search", result.size());
        return result;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDtoWithViews getEventById(@PathVariable Long eventId,
                                              HttpServletRequest request) {
        log.info("Public request for event ID: {}, client IP: {}, URI: {}",
                eventId, request.getRemoteAddr(), request.getRequestURI());

        EventFullDtoWithViews result = eventService.getEventById(eventId, request);

        log.info("Returning event ID: {} with title: '{}' and {} views",
                eventId, result.getTitle(), result.getViews());
        return result;
    }
}