package org.example.entity;
import org.example.model.CategoryCollors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;



public class CategoryTest {
    @Test
    void newCategoryShouldHaveName() {
        Category category = new Category("Работа", CategoryCollors.RED);
        assertEquals("Работа", category.getName());
        assertEquals(CategoryCollors.RED, category.getColor());

    }
}
