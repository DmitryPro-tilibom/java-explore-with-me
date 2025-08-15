package ru.practicum.ewm.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.compilations.dto.CompilationDto;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.service.CompilationService;

import jakarta.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationControllerAdmin {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Admin: Adding new events compilation with title: {}", newCompilationDto.getTitle());
        CompilationDto result = compilationService.addCompilation(newCompilationDto);
        log.info("Admin: Successfully added compilation with ID: {}", result.getId());
        return result;
    }

    @PatchMapping("/{compilationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Long compilationId,
                                            @RequestBody @Valid UpdateCompilationRequest updateCompilation) {
        log.info("Admin: Updating events compilation by ID: {}", compilationId);
        CompilationDto result = compilationService.updateCompilation(compilationId, updateCompilation);
        log.info("Admin: Successfully updated compilation with ID: {}", compilationId);
        return result;
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compilationId) {
        log.info("Admin: Deleting events compilation by ID: {}", compilationId);
        compilationService.deleteCompilation(compilationId);
        log.info("Admin: Event compilation with ID: {} was successfully deleted", compilationId);
    }
}