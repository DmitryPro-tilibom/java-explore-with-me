package ru.practicum.ewm.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.Category;
import ru.practicum.ewm.categories.CategoryMapper;
import ru.practicum.ewm.categories.service.CategoryService;
import ru.practicum.ewm.events.EventMapper;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.dto.EventFullDto;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventShortDtoWithViews;
import ru.practicum.ewm.events.dto.NewEventDto;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.events.model.StateActionAdmin;
import ru.practicum.ewm.events.model.StateActionPrivate;
import ru.practicum.ewm.events.requests.UpdateEventAdminRequest;
import ru.practicum.ewm.events.requests.UpdateEventUserRequest;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.locations.Location;
import ru.practicum.ewm.locations.LocationDto;
import ru.practicum.ewm.locations.LocationMapper;
import ru.practicum.ewm.requests.service.RequestService;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.model.State.PENDING;
import static ru.practicum.ewm.events.model.State.PUBLISHED;

import static ru.practicum.ewm.events.model.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.ewm.events.model.StateActionAdmin.REJECT_EVENT;
import static ru.practicum.ewm.events.model.StateActionPrivate.CANCEL_REVIEW;
import static ru.practicum.ewm.events.model.StateActionPrivate.SEND_TO_REVIEW;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {
    private final  EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;
    private final EventStatService eventStatService;
    private final EventValidationService validationService;

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Adding new event by user ID: {}", userId);
        validationService.validateEventTime(newEventDto.getEventDate());

        User user = userService.getUserById(userId);

        Category category = CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory()));

        Location location = validationService.validateAndGetLocation(
                LocationMapper.toLocation(newEventDto.getLocation())
        );

        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(PENDING);

        Event savedEvent = eventRepository.save(event);
        log.info("Event created with ID: {}", savedEvent.getId());
        return EventMapper.toEventFullDto(savedEvent, 0L);
    }

    @Override
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        log.info("Updating event ID: {} by user ID: {}", eventId, userId);
        Event event = validationService.validateAndGetEvent(eventId, userId);

        if (event.getState() == PUBLISHED) {
            log.error("Attempt to update published event ID: {}", eventId);
            throw new DataIntegrityViolationException("Published events can't be updated");
        }

        updateEventFields(event, updateEvent);

        if (updateEvent.getStateAction() != null) {
            StateActionPrivate stateAction = StateActionPrivate.valueOf(updateEvent.getStateAction());
            if (stateAction.equals(SEND_TO_REVIEW)) {
                event.setState(PENDING);
                log.debug("Event ID: {} sent to review", eventId);
            } else if (stateAction.equals(CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
                log.debug("Event ID: {} canceled by owner", eventId);
            }
        }

        Long confirmedRequests = requestService.getConfirmedRequestsCountForEvent(eventId);
        log.info("Event ID: {} successfully updated by owner", eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        log.info("Admin updating event ID: {}", eventId);
        Event event = validationService.validateAndGetEvent(eventId);

        if (updateEvent.getStateAction() != null) {
            StateActionAdmin stateAction = StateActionAdmin.valueOf(updateEvent.getStateAction());
            handleAdminStateAction(event, stateAction);
        }

        updateEventAdminFields(event, updateEvent);

        Long confirmedRequests = requestService.getConfirmedRequestsCountForEvent(eventId);
        log.info("Event ID: {} successfully updated by admin", eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByOwnerId(Long userId, Integer from, Integer size) {
        log.info("Getting events for owner ID: {}, from: {}, size: {}", userId, from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequestsCountForEvents(eventIds);

        log.debug("Found {} events for owner ID: {}", events.size(), userId);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventEntityById(Long eventId) {
        log.info("Getting event entity by ID: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new NotFoundException("Event with id=" + eventId + " was not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventByOwner(Long userId, Long eventId) {
        log.info("Getting event ID: {} for owner ID: {}", eventId, userId);
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {} for owner ID: {}", eventId, userId);
                    return new NotFoundException("Event with id=" + eventId + " was not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByOwnerId(Long userId, Long eventId) {
        log.info("Getting event ID: {} for owner ID: {}", eventId, userId);
        Event event = validationService.validateAndGetEvent(eventId, userId);
        Long confirmedRequests = requestService.getConfirmedRequestsCountForEvent(eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests);
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        return requestService.getConfirmedRequestsCountForEvents(eventIds);
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventFullDtoWithViews> getEventsByAdminParams(List<Long> users, List<String> states,
                                                              List<Long> categories, LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Admin events search with params: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                users, states, categories, rangeStart, rangeEnd);

        validateDateRange(rangeStart, rangeEnd);

        Specification<Event> specification = buildAdminSpecification(users, states, categories, rangeStart, rangeEnd);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(specification, pageRequest).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        List<EventFullDtoWithViews> result = eventStatService.addViewsToEvents(events, confirmedRequests);

        log.debug("Admin search returned {} events", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDtoWithViews> getEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, String sort, Integer from,
                                                  Integer size, HttpServletRequest request) {
        log.info("Public events search with params: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                "onlyAvailable={}, sort={}", text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);

        validateDateRange(rangeStart, rangeEnd);

        Specification<Event> specification = buildPublicSpecification(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable
        );

        PageRequest pageRequest = buildPageRequest(from, size, sort);
        List<Event> events = eventRepository.findAll(specification, pageRequest).getContent();

        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        List<EventShortDtoWithViews> result = events.stream()
                .map(event -> {
                    Long views = eventStatService.addViewsToEvent(event, confirmedRequests.getOrDefault(event.getId(), 0L))
                            .getViews();
                    return EventMapper.toEventShortDtoWithViews(
                            event,
                            views,
                            confirmedRequests.getOrDefault(event.getId(), 0L)
                    );
                })
                .collect(Collectors.toList());

        eventStatService.saveHit(request);
        log.debug("Public search returned {} events", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDtoWithViews getEventById(Long eventId, HttpServletRequest request) {
        log.info("Getting published event ID: {}", eventId);
        Event event = validationService.validateAndGetEvent(eventId);

        if (event.getState() != PUBLISHED) {
            log.error("Attempt to access unpublished event ID: {}", eventId);
            throw new NotFoundException("Event must be published");
        }

        Long confirmedRequests = requestService.getConfirmedRequestsCountForEvent(eventId);
        EventFullDtoWithViews result = eventStatService.addViewsToEvent(event, confirmedRequests);
        eventStatService.saveHit(request);

        log.debug("Returning event ID: {} with {} views and {} confirmed requests",
                eventId, result.getViews(), confirmedRequests);
        return result;
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateEvent) {
        updateAnnotation(event, updateEvent.getAnnotation());
        updateCategory(event, updateEvent.getCategory());
        updateDescription(event, updateEvent.getDescription());
        updateEventDate(event, updateEvent.getEventDate());
        updateLocation(event, updateEvent.getLocation());
        updateBooleanFields(event,
                updateEvent.getPaid(),
                updateEvent.getParticipantLimit(),
                updateEvent.getRequestModeration());
        updateTitle(event, updateEvent.getTitle());
    }

    private void updateEventAdminFields(Event event, UpdateEventAdminRequest updateEvent) {
        updateAnnotation(event, updateEvent.getAnnotation());
        updateCategory(event, updateEvent.getCategory());
        updateDescription(event, updateEvent.getDescription());
        updateEventDate(event, updateEvent.getEventDate());
        updateLocation(event, updateEvent.getLocation());
        updateBooleanFields(event,
                updateEvent.getPaid(),
                updateEvent.getParticipantLimit(),
                updateEvent.getRequestModeration());
        updateTitle(event, updateEvent.getTitle());
    }

    private void updateAnnotation(Event event, String annotation) {
        Optional.ofNullable(annotation)
                .filter(a -> !a.isBlank())
                .ifPresent(event::setAnnotation);
    }

    private void updateCategory(Event event, Long categoryId) {
        Optional.ofNullable(categoryId)
                .ifPresent(id -> event.setCategory(
                        CategoryMapper.toCategory(categoryService.getCategoryById(id))
                ));
    }

    private void updateDescription(Event event, String description) {
        Optional.ofNullable(description)
                .filter(d -> !d.isBlank())
                .ifPresent(event::setDescription);
    }

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        Optional.ofNullable(eventDate)
                .ifPresent(date -> {
                    validationService.validateEventTime(date);
                    event.setEventDate(date);
                });
    }

    private void updateLocation(Event event, LocationDto locationDto) {
        Optional.ofNullable(locationDto)
                .ifPresent(dto -> event.setLocation(
                        validationService.validateAndGetLocation(LocationMapper.toLocation(dto))
                ));
    }

    private void updateBooleanFields(Event event, Boolean paid, Integer participantLimit, Boolean requestModeration) {
        Optional.ofNullable(paid).ifPresent(event::setPaid);
        Optional.ofNullable(participantLimit).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(requestModeration).ifPresent(event::setRequestModeration);
    }

    private void updateTitle(Event event, String title) {
        Optional.ofNullable(title)
                .filter(t -> !t.isBlank())
                .ifPresent(event::setTitle);
    }

    private void handleAdminStateAction(Event event, StateActionAdmin stateAction) {

        if (PUBLISH_EVENT.equals(stateAction) && !PENDING.equals(event.getState())) {
            log.error("Attempt to publish non-pending event ID: {}", event.getId());
            throw new DataIntegrityViolationException("Event can't be published because it's not pending");
        }

        if (REJECT_EVENT.equals(stateAction) && PUBLISHED.equals(event.getState())) {
            log.error("Attempt to reject published event ID: {}", event.getId());
            throw new DataIntegrityViolationException("Event can't be rejected because it's already published");
        }

        if (PUBLISH_EVENT.equals(stateAction)) {
            event.setState(PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
            log.debug("Event ID: {} published by admin", event.getId());
        } else if (REJECT_EVENT.equals(stateAction)) {
            event.setState(State.CANCELED);
            log.debug("Event ID: {} rejected by admin", event.getId());
        }
    }

    private Specification<Event> buildAdminSpecification(List<Long> users, List<String> states,
                                                         List<Long> categories, LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd) {
        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("state").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(categories));
        }
        if (rangeStart != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        return spec;
    }

    private Specification<Event> buildPublicSpecification(String text, List<Long> categories, Boolean paid,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                          Boolean onlyAvailable) {
        Specification<Event> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("state"), PUBLISHED));

        if (text != null && !text.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%")
            ));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paid"), paid));
        }
        if (onlyAvailable != null && onlyAvailable) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElseGet(rangeStart, LocalDateTime::now);
        spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("eventDate"), startDateTime));

        if (rangeEnd != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("eventDate"), rangeEnd));
        }

        return spec;
    }

    private PageRequest buildPageRequest(Integer from, Integer size, String sort) {
        if (sort == null) {
            return PageRequest.of(from / size, size);
        }

        switch (sort) {
            case "EVENT_DATE":
                return PageRequest.of(from / size, size, Sort.by("eventDate"));
            case "VIEWS":
                return PageRequest.of(from / size, size, Sort.by("views").descending());
            default:
                log.warn("Unknown sort parameter: {}", sort);
                throw new ValidationException("Unknown sort: " + sort);
        }
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.error("Invalid date range: start {} is after end {}", rangeStart, rangeEnd);
            throw new ValidationException("Start date must be before end date");
        }
    }
}