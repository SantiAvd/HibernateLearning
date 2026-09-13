package org.example.service;

import org.example.Repository.CategoryRepository;
import org.example.entity.Category;
import org.example.exceptions.CategoryNotFoundException;
import org.example.model.CategoryCollors;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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

    @Mock
    SessionFactory sessionFactory;

    @Mock
    Transaction transaction;

    @Mock
    Session session;

    @InjectMocks
    CategoryService categoryService;

    Category category;

    @BeforeEach
    void beforeEach() {
        category = new Category("Дом", CategoryCollors.BLUE);

        when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);
    }

    @Test
    void getByIdTest() {
        when(categoryRepository.findById(1L, session)).thenReturn(Optional.of(category));

        Category result = categoryService.getById(1L);

        Assertions.assertEquals("Дом", result.getName());
    }

    @Test
    void getByIdNotFoundTest() {
        Long id = 1L;
        when(categoryRepository.findById(id, session)).thenReturn(Optional.empty());

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

        verify(categoryRepository, times(1)).save(any(Category.class), eq(session));
        verify(transaction, times(1)).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void deleteCategoryTest() {

        when(categoryRepository.findById(1L,session)).thenReturn(Optional.of(category));
        categoryService.delete(1L);

        verify(categoryRepository,times(1)).delete(category, session);
        verify(transaction, times(1)).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void deleteCategoryWhenIsPresentFalseTest() {
        when(categoryRepository.findById(1L, session)).thenReturn(Optional.empty());

        CategoryNotFoundException exception = Assertions.assertThrowsExactly(CategoryNotFoundException.class,
                () -> categoryService.delete(1L));

        Assertions.assertEquals("Категория " + 1L + " не найдена", exception.getMessage());
        verify(categoryRepository, never()).delete(any(Category.class), eq(session));
        verify(transaction, times(1)).rollback();
        verify(transaction, never()).commit();
    }
}
