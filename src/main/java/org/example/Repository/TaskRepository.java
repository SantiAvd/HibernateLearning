package org.example.Repository;

import org.example.HibernateUtil;
import org.example.model.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {
    public void save(Task task) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.persist(task);
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

    public Optional<Task> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Task task = session.find(Task.class, id);

            return Optional.ofNullable(task);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске задачи по id = " + id, e);
        }
    }

    public List<Task> findByUser(User user) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery("FROM Task WHERE user = :user", Task.class);
            query.setParameter("user", user);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске задач", e);
        }
    }

    public void update(Task task) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();;
                session.merge(task);
                tx.commit();
            } catch(Exception e)  {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при изменении", e);
        }
    }

    public void delete(Task task) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                task = session.merge(task);
                session.remove(task);
                tx.commit();
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении", e);
        }
    }


    public List<Task> findByUserAndStatus(User user, TaskStatus status) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery("FROM Task t where t.status =:status and t.user =:user", Task.class);
            query.setParameter("status", status);
            query.setParameter("user", user);
            return  query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошика при получении задачь с статусом", e);
        }
    }

    public List<Task> findByUserAndPriority(User user,PriorityValues priorityValue) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery("FROM Task t where t.priority =:priority and t.user =:user", Task.class);
            query.setParameter("priority", priorityValue);
            query.setParameter("user", user);
            return  query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошика при получении задачь с статусом", e);
        }
    }

    public List<Task> findByUserAndCategory(User user,Category category) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Task> query = session.createQuery("FROM Task t where t.category =:category and t.user =:user", Task.class);
            query.setParameter("category", category);
            query.setParameter("user", user);
            return  query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Ошика при получении задачь с статусом", e);
        }
    }


}
