package org.example.Repository;

import org.example.HibernateUtil;
import org.example.entity.Category;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    public Optional<Category> findById(Long id, Session session) {
        Category category = session.find(Category.class, id);
        return Optional.ofNullable(category);
    }

    public void save(Category category, Session session) {
                session.persist(category);
    }

    public List<Category> findAll(Session session) {
        Query<Category> query = session.createQuery(
                "FROM Category",
                Category.class
        );
        return query.getResultList();
    }

    public void delete(Category category, Session session) {
        session.remove(category);
    }
}