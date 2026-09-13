package org.example.service;

import org.example.Repository.CategoryRepository;
import org.example.Repository.TaskRepository;
import org.example.exceptions.TaskNotFoundException;
import org.example.service.dto.TaskCreateRequest;
import org.example.entity.Category;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.model.TaskStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class TaskService {
    private final TaskRepository taskRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final SessionFactory sessionFactory;

    public TaskService(TaskRepository taskRepository, UserService userService, CategoryService categoryService, SessionFactory sessionFactory) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.sessionFactory = sessionFactory;
    }

    public void create(TaskCreateRequest request) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Category category = null;
                if (request.getCategory() != null) {
                    category = categoryService.getById(request.getCategory());
                }

                User user = userService.getUserByTelegramId(request.getTelegramId());

                Task task = new Task(
                        request.getTitle(),
                        request.getDescription(),
                        request.getDeadline(),
                        request.getPriority(),
                        user,
                        category);

                taskRepository.save(task, session);
                tx.commit();

            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void setCategory(Long taskId, Category category) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = taskRepository.findById(taskId, session)
                        .orElseThrow(() -> new TaskNotFoundException(taskId));

                task.setCategory(category);
                taskRepository.update(task, session);
                tx.commit();

            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public List<Task> showAll(User user) {
        try (Session session = sessionFactory.openSession()) {
            return taskRepository.findByUser(user, session);
        }
    }

    public void changeStatus(Long id, TaskStatus status) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = taskRepository.findById(id, session)
                        .orElseThrow(() -> new TaskNotFoundException(id));

                task.setStatus(status);
                taskRepository.update(task, session);
                tx.commit();

            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void delete(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Task task = taskRepository.findById(id, session)
                        .orElseThrow(() -> new TaskNotFoundException(id));

                taskRepository.delete(task.getId(), session);
                tx.commit();

            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }
}