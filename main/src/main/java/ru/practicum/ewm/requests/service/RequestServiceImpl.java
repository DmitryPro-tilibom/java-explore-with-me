package ru.practicum.ewm.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.service.EventInfoService;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.requests.RequestMapper;
import ru.practicum.ewm.requests.RequestRepository;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.model.ParticipationRequest;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.users.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventInfoService eventInfoService;
    private final UserService userService;

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.info("Adding request for user ID: {} to event ID: {}", userId, eventId);
        Event event = eventInfoService.getEventEntityById(eventId);

        User user = getUser(userId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            log.warn("Duplicate request from user ID: {} to event ID: {}", userId, eventId);
            throw new DataIntegrityViolationException("Request is already exist.");
        }

        if (userId.equals(event.getInitiator().getId())) {
            log.warn("Initiator attempt to request own event. User ID: {}, Event ID: {}", userId, eventId);
            throw new DataIntegrityViolationException("Initiator can't send request to his own event.");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            log.warn("Attempt to request unpublished event ID: {}", eventId);
            throw new DataIntegrityViolationException("Participation is possible only in published event.");
        }

        if (event.getParticipantLimit() != 0 &&
                event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            log.warn("Participant limit reached for event ID: {}", eventId);
            throw new DataIntegrityViolationException("Participant limit has been reached.");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(event.getRequestModeration() && event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED);

        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Request ID: {} created with status: {}", savedRequest.getId(), savedRequest.getStatus());
        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Updating request statuses by user ID: {} for event ID: {}", userId, eventId);

        User initiator = getUser(userId);
        Event event = eventInfoService.getEventByOwner(userId, eventId);

        if (!event.getInitiator().equals(initiator)) {
            log.warn("User ID: {} is not initiator of event ID: {}", userId, eventId);
            throw new ValidationException("User isn't initiator.");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= confirmedRequests) {
            log.warn("Participant limit reached for event ID: {}", eventId);
            throw new DataIntegrityViolationException("The participant limit has been reached.");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndIdInAndStatus(eventId,
                statusUpdateRequest.getRequestIds(), RequestStatus.PENDING);

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            ParticipationRequest request = requests.get(i);
            if (statusUpdateRequest.getStatus() == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            } else if (statusUpdateRequest.getStatus() == RequestStatus.CONFIRMED &&
                    event.getParticipantLimit() > 0 &&
                    (confirmedRequests + i) < event.getParticipantLimit()) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(RequestMapper.toParticipationRequestDto(request));
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            }
        }

        log.info("Updated {} requests: {} confirmed, {} rejected",
                requests.size(), confirmed.size(), rejected.size());
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }


    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Canceling request ID: {} by user ID: {}", requestId, userId);
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);
        log.info("Request ID: {} canceled successfully", requestId);
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId) {
        log.info("Getting requests for event ID: {} by owner ID: {}", eventId, userId);
        checkUser(userId);
        eventInfoService.getEventByOwner(userId, eventId);

        List<ParticipationRequestDto> requests = requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        log.info("Found {} requests for event ID: {}", requests.size(), eventId);
        return requests;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        log.info("Getting all requests for user ID: {}", userId);
        checkUser(userId);
        List<ParticipationRequestDto> requests = requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());

        log.info("Found {} requests for user ID: {}", requests.size(), userId);
        return requests;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getConfirmedRequestsCountForEvent(Long eventId) {
        log.debug("Getting confirmed requests count for event ID: {}", eventId);
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedRequestsCountForEvents(List<Long> eventIds) {
        log.debug("Getting confirmed requests count for events: {}", eventIds);
        return requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        ConfirmedRequests::getEvent,
                        ConfirmedRequests::getCount
                ));
    }

    private User getUser(Long userId) {
        return userService.getUserById(userId);
    }

    private void checkUser(Long userId) {
        if (!userService.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }
}