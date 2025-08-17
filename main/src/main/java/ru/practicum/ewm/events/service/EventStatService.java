package ru.practicum.ewm.events.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.model.Event;

import java.util.List;
import java.util.Map;

public interface EventStatService {
    List<EventFullDtoWithViews> addViewsToEvents(List<Event> events, Map<Long, Long> confirmedRequests);

    EventFullDtoWithViews addViewsToEvent(Event event, Long confirmedRequests);

    void saveHit(HttpServletRequest request);
}