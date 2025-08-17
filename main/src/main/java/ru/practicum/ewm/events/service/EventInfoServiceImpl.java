package ru.practicum.ewm.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.exceptions.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventInfoServiceImpl implements EventInfoService {
    private final EventRepository eventRepository;

    @Override
    public Event getEventEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    @Override
    public Event getEventByOwner(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }
}