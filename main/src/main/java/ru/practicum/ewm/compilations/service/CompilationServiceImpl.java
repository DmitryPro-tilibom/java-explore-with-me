package ru.practicum.ewm.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.Compilation;
import ru.practicum.ewm.compilations.CompilationMapper;
import ru.practicum.ewm.compilations.CompilationRepository;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.events.EventMapper;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.requests.RequestRepository;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.ewm.requests.model.RequestStatus.CONFIRMED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Adding new compilation with title: {}", newCompilationDto.getTitle());
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null) {
            log.debug("Setting events for compilation: {}", newCompilationDto.getEvents());
            compilation.setEvents(eventRepository.findAllByIdIn(newCompilationDto.getEvents()));
        }

        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        log.info("Compilation created with ID: {}", compilation.getId());

        if (compilation.getEvents() != null) {
            List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                    .stream()
                    .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));

            compilationDto.setEvents(compilation.getEvents().stream()
                    .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.get(event.getId())))
                    .collect(Collectors.toList()));
        }

        return compilationDto;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation) {
        log.info("Updating compilation with ID: {}", compId);
        Compilation compilation = getCompilation(compId);

        if (updateCompilation.getEvents() != null) {
            log.debug("Updating events for compilation ID {}: {}", compId, updateCompilation.getEvents());
            compilation.setEvents(eventRepository.findAllByIdIn(updateCompilation.getEvents()));
        }

        if (updateCompilation.getPinned() != null) {
            log.debug("Updating pinned status for compilation ID {}: {}", compId, updateCompilation.getPinned());
            compilation.setPinned(updateCompilation.getPinned());
        }

        String title = updateCompilation.getTitle();
        if (title != null && !title.isBlank()) {
            log.debug("Updating title for compilation ID {}: {}", compId, title);
            compilation.setTitle(title);
        }

        log.info("Compilation ID {} successfully updated", compId);
        return enrichWithConfirmedRequests(CompilationMapper.toCompilationDto(compilation), compilation.getEvents());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Getting compilations list with parameters: pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        List<CompilationDto> result = new ArrayList<>();
        for (Compilation compilation : compilations) {
            CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);

            if (compilation.getEvents() != null) {
                List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
                Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                        .stream()
                        .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));

                compilationDto.setEvents(compilation.getEvents().stream()
                        .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.get(event.getId())))
                        .collect(Collectors.toList()));
            }

            result.add(compilationDto);
        }

        log.info("Returning {} compilations", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compilationId) {
        log.info("Getting compilation by ID: {}", compilationId);
        Compilation compilation = getCompilation(compilationId);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);

        if (compilation.getEvents() != null) {
            List<Long> ids = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                    .stream()
                    .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));

            compilationDto.setEvents(compilation.getEvents().stream()
                    .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.get(event.getId())))
                    .collect(Collectors.toList()));
        }

        return compilationDto;
    }

    @Override
    public void deleteCompilation(Long compilationId) {
        log.info("Deleting compilation with ID: {}", compilationId);
        getCompilation(compilationId);
        compilationRepository.deleteById(compilationId);
        log.info("Compilation ID {} successfully deleted", compilationId);
    }

    private Compilation getCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() -> {
            log.error("Compilation not found with ID: {}", compilationId);
            return new NotFoundException("Compilation id=" + compilationId + " not found");
        });
    }

    private CompilationDto enrichWithConfirmedRequests(CompilationDto dto, Set<Event> events) {
        if (events != null && !events.isEmpty()) {
            List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                    .stream()
                    .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));

            dto.setEvents(events.stream()
                    .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.get(event.getId())))
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}