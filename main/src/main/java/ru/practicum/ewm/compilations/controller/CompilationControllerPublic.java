package ru.practicum.ewm.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.service.CompilationService;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationControllerPublic {
    private final CompilationService compilationService;

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Public: Getting events compilations with parameters: pinned={}, from={}, size={}",
                pinned, from, size);
        List<CompilationDto> result = compilationService.getCompilations(pinned, from, size);
        log.info("Public: Returned {} events compilations", result.size());
        return result;
    }

    @GetMapping("/{compilationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CompilationDto getCompilationById(@PathVariable Long compilationId) {
        log.info("Public: Getting event compilation by ID: {}", compilationId);
        CompilationDto result = compilationService.getCompilationById(compilationId);
        log.info("Public: Event compilation with ID {} returned", compilationId);
        return result;
    }
}