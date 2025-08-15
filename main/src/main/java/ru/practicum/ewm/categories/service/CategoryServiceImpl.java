package ru.practicum.ewm.categories.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.Category;
import ru.practicum.ewm.categories.CategoryMapper;
import ru.practicum.ewm.categories.CategoryRepository;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.exceptions.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding new category: {}", newCategoryDto);
        CategoryDto savedCategory = CategoryMapper.toCategoryDto(
                categoryRepository.save(CategoryMapper.toCategory(newCategoryDto))
        );
        log.info("Category added: {}", savedCategory);
        return savedCategory;
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("Updating category ID={}, data: {}", categoryId, categoryDto);
        Category category = getCategory(categoryId);
        category.setName(categoryDto.getName());
        CategoryDto updatedCategory = CategoryMapper.toCategoryDto(category);
        log.info("Category updated: {}", updatedCategory);
        return updatedCategory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Getting categories (from={}, size={})", from, size);
        List<CategoryDto> categories = categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
        log.info("Found {} categories", categories.size());
        return categories;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long categoryId) {
        log.info("Getting category with ID={}", categoryId);
        CategoryDto category = CategoryMapper.toCategoryDto(getCategory(categoryId));
        log.info("Category with ID={} successfully found", category);
        return category;
    }

    @Override
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with ID={}", categoryId);
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Category with id=" + categoryId + " was not found");
        }
        categoryRepository.deleteById(categoryId);
        log.info("Category with ID={} successfully deleted", categoryId);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Category with id=" + categoryId + " was not found"));
    }
}