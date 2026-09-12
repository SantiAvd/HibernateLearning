package org.example.Repository;

import org.example.HibernateUtil;
import org.hibernate.Transaction;
import org.example.entity.User;
import org.hibernate.Session;

import org.hibernate.query.Query;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByTelegramId(Long telegramId, Session session){
           Query<User> query = session.createQuery("FROM User WHERE telegramId = :telegramId", User.class);
           query.setParameter("telegramId", telegramId);
           return  query.uniqueResultOptional();
    }

    public void save(User user, Session session) {
        session.persist(user);
    }

    public void update(User user, Session session) {
        session.merge(user);
    }

}
