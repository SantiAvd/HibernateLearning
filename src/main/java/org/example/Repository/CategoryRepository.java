package org.example.Repository;

import org.example.HibernateUtil;
import org.example.model.Category;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class CategoryRepository {

    public Optional<Category> findById(Long id) {
        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Category category = session.find(Category.class, id);

            return Optional.ofNullable(category);

        } catch (Exception e) {
            throw new RuntimeException( "Ошибка при поиске категории id=" + id, e);
        }
    }

    public void save(Category category) {
        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.persist(category);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении категории", e);
        }
    }

    public List<Category> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery(
                    "FROM Category",
                    Category.class
            );
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении списка категорий",e);
        }
    }

    public void delete(Category category) {

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.remove(category);
                tx.commit();
            }catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении", e);
        }
    }
}