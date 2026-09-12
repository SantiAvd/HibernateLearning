package org.example.service;

import org.example.Repository.CategoryRepository;
import org.example.entity.Category;
import org.example.exceptions.CategoryNotFoundException;
import org.example.model.CategoryCollors;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class CategoryService {

    CategoryRepository categoryRepository;
    SessionFactory sessionFactory;

    public CategoryService(CategoryRepository categoryRepository, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.categoryRepository = categoryRepository;
    }

    public  Category getById(Long id) {
        try(Session session = sessionFactory.openSession()) {
            return categoryRepository.findById(id, session)
                    .orElseThrow(() ->
                            new CategoryNotFoundException(id));
        }
    }

    public List<Category> getAll() {
        try (Session session = sessionFactory.openSession()) {
            return categoryRepository.findAll(session);
        }
    }

    public Category create(String name, CategoryCollors color) {

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Category category = new Category(name, color);
                categoryRepository.save(category, session);
                transaction.commit();
                return  category;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void delete(Long id) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Category category = categoryRepository.findById(id, session).orElseThrow(
                        () -> new CategoryNotFoundException(id)
                );
                categoryRepository.delete(category, session);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }
}
