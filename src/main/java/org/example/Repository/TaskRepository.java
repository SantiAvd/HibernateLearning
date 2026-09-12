package org.example.Repository;

import org.example.HibernateUtil;
import org.example.entity.Category;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.exceptions.TaskNotFoundException;
import org.example.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class TaskRepository {

    public void save(Task task, Session session) {
            session.persist(task);
    }

    public Optional<Task> findById(Long id, Session session) {

        Task task = session.find(Task.class, id);
        return Optional.ofNullable(task);
    }

    public List<Task> findByUser(User user, Session session) {
        Query<Task> query = session.createQuery("FROM Task WHERE user = :user", Task.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public void update(Task task, Session session) {
        session.merge(task);
    }

    public void delete(Long id, Session session) {
        Query<Task> query = session.createQuery("from Task where id = :id", Task.class);
        query.setParameter("id", id);
        Task task = query.uniqueResult();
        task = session.merge(task);
        session.remove(task);
    }
}
