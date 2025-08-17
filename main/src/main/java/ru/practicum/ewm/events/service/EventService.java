package ru.practicum.ewm.events.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventShortDtoWithViews;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.requests.UpdateEventAdminRequest;
import ru.practicum.ewm.events.requests.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest updateEvent);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent);

    List<EventShortDto> getEventsByOwnerId(Long userId, Integer from, Integer size);

    EventFullDto getEventByOwnerId(Long userId, Long eventId);

    List<EventFullDtoWithViews> getEventsByAdminParams(List<Long> users, List<String> states, List<Long> categories,
                                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                       Integer from, Integer size);

    List<EventShortDtoWithViews> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                           Integer size, HttpServletRequest request);

    EventFullDtoWithViews getEventById(Long eventId, HttpServletRequest request);

    Event getEventEntityById(Long eventId);

    Event getEventByOwner(Long userId, Long eventId);

}