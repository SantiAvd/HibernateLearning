package org.example.service;

import org.example.Repository.CategoryRepository;
import org.example.entity.Category;
import org.example.exceptions.CategoryNotFoundException;
import org.example.model.CategoryCollors;

import java.util.List;
import java.util.Optional;

public class CategoryService {

    CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public  Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id));
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(String name, CategoryCollors color) {
        Category category = new Category(name, color);
        categoryRepository.save(category);
        return  category;
    }

    public void delete(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            categoryRepository.delete(category.get());
        }
    }
}
