package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.model.Event;

public interface EventInfoService {
    Event getEventEntityById(Long eventId);

    Event getEventByOwner(Long userId, Long eventId);
}