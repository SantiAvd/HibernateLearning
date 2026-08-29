package org.example.service;

import org.example.Repository.CategoryRepository;
import org.example.entity.Category;
import org.example.exceptions.CategoryNotFoundException;
import org.example.model.CategoryCollors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    Category category;

    @BeforeEach
    void beforeEach() {
        category = new Category("Дом", CategoryCollors.BLUE);
    }

    @Test
    void getByIdTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getById(1L);

        Assertions.assertEquals("Дом", result.getName());
    }

    @Test
    void getByIdNotFoundTest() {
        Long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = Assertions.assertThrowsExactly(CategoryNotFoundException.class,
                () -> categoryService.getById(id));

        Assertions.assertEquals("Категория " + id + " не найдена", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(CategoryCollors.class)
    void createCategoryTest(CategoryCollors collor) {
        Category result = categoryService.create("Спорт", collor);

        Assertions.assertEquals("Спорт", result.getName());
        Assertions.assertEquals(collor, result.getColor());


        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void deleteCategoryTest() {

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        categoryService.delete(1L);

        verify(categoryRepository,times(1)).delete(category);
    }

    @Test
    void deleteCategoryWhenIsPresentFalseTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        categoryService.delete(1L);
        verify(categoryRepository,never()).delete(any(Category.class));
    }
}
