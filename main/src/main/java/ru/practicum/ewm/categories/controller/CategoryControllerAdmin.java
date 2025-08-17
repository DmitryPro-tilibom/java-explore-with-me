package ru.practicum.ewm.categories.controller;

import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.service.CategoryService;
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
import ru.practicum.ewm.categories.dto.NewCategoryDto;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryControllerAdmin {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("Adding new category: {}", newCategoryDto);
        CategoryDto createdCategory = categoryService.addCategory(newCategoryDto);
        log.info("Category successfully added: {}", createdCategory);
        return createdCategory;
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto categoryDto) {
        log.info("Updating category with ID={}, new data: {}", catId, categoryDto);
        CategoryDto updatedCategory = categoryService.updateCategory(catId, categoryDto);
        log.info("Category successfully updated: {}", updatedCategory);
        return updatedCategory;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Deleting category with ID={}", catId);
        categoryService.deleteCategory(catId);
        log.info("Category with ID={} successfully deleted", catId);
    }
}