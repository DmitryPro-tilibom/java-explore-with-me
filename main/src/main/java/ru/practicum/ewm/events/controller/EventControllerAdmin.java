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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.requests.UpdateEventAdminRequest;
import ru.practicum.ewm.events.service.EventService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventControllerAdmin {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Admin updating event ID: {} with data: {}", eventId, updateEventAdminRequest);
        EventFullDto result = eventService.updateEventByAdmin(eventId, updateEventAdminRequest);
        log.info("Admin successfully updated event ID: {}", eventId);
        return result;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDtoWithViews> getEventsByAdminParams(@RequestParam(required = false) List<Long> users,
                                                              @RequestParam(required = false) List<String> states,
                                                              @RequestParam(required = false) List<Long> categories,
                                                              @RequestParam(required = false) @DateTimeFormat(pattern =
                                                                      EndpointHitDto.DATE_TIME_PATTERN) LocalDateTime rangeStart,
                                                              @RequestParam(required = false) @DateTimeFormat(pattern =
                                                                      EndpointHitDto.DATE_TIME_PATTERN) LocalDateTime rangeEnd,
                                                              @RequestParam(value = "from", defaultValue = "0")
                                                              @PositiveOrZero Integer from,
                                                              @RequestParam(value = "size", defaultValue = "10")
                                                              @Positive Integer size) {
        log.info("Admin searching events with params: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        List<EventFullDtoWithViews> result = eventService.getEventsByAdminParams(
                users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("Admin found {} events matching search criteria", result.size());
        return result;
    }
}