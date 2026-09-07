package org.example.Repository;

import org.example.HibernateUtil;
import org.hibernate.Transaction;
import org.example.entity.User;
import org.hibernate.Session;

import org.hibernate.query.Query;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByTelegramId(Long telegramId){
        try (
                Session session = HibernateUtil.getSessionFactory().openSession();
        ) {
           Query<User> query = session.createQuery("FROM User WHERE telegramId = :telegramId", User.class);
           query.setParameter("telegramId", telegramId);
            return  query.uniqueResultOptional();
        } catch (Exception e){
            throw new RuntimeException("Ошибка при поиске студента по telegramId=" + telegramId, e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        }
    }
    public void save(User user) {
        try(
                Session session = HibernateUtil.getSessionFactory().openSession();
        ) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();;
                session.persist(user);
                tx.commit();
            } catch(Exception e)  {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении", e);
        }
    }

    public void update(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.merge(user);
                tx.commit();
            } catch(Exception e)  {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении изменений пользователя", e);
        }
    }

}
