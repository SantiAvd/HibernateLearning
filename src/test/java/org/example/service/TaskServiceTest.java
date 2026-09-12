package org.example.service;


import org.example.Repository.TaskRepository;
import org.example.exceptions.TaskNotFoundException;
import org.example.service.dto.TaskCreateRequest;
import org.example.entity.Category;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.model.CategoryCollors;
import org.example.model.PriorityValues;
import org.example.model.TaskStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    TaskRepository taskRepository;

    @Mock
    CategoryService categoryService;

    @Mock
    UserService userService;
    @Mock
    Transaction transaction;

    @Mock
    SessionFactory sessionFactory;

    @Mock
    Session session;


    @InjectMocks
    TaskService taskService;
    User user;
    Task task;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Alex", "Ale");
        task = new Task("Title", "Desc", Instant.now(), PriorityValues.LOW, user, null);

        when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);
    }

    @Test
    void createTaskTest() {
        Category category = new Category("Home", CategoryCollors.ORANGE);

        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("title");
        taskCreateRequest.setDescription("description");
        taskCreateRequest.setPriority(PriorityValues.LOW);
        taskCreateRequest.setDeadline(Instant.parse("2026-08-23T10:15:30Z"));
        taskCreateRequest.setTelegramId(1L);
        taskCreateRequest.setCategory(5L);

        when(userService.getUserByTelegramId(1L)).thenReturn(user);
        when(categoryService.getById(5L)).thenReturn(category);

        taskService.create(taskCreateRequest);

        verify(taskRepository, times(1)).save(any(Task.class), eq(session));
    }

    @Test
    void setCategoryIfExistTest() {
        Category category = new Category("Home", CategoryCollors.ORANGE);
        when(taskRepository.findById(1L, session)).thenReturn(Optional.of(task));

        taskService.setCategory(1L, category);

        verify(taskRepository, times(1)).update(any(Task.class), eq(session));
        verify(transaction, times(1)).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void setCategoryIfNotExistTest() {
        Category category = new Category("Home", CategoryCollors.ORANGE);
        when(taskRepository.findById(1L, session)).thenReturn(Optional.empty());


        TaskNotFoundException ex =  Assertions.assertThrowsExactly(TaskNotFoundException.class,
                () -> taskService.setCategory(1L, category));

        Assertions.assertEquals("Задача с ID " + 1L + " не найдена", ex.getMessage());
        verify(taskRepository, never()).update(any(Task.class), eq(session));
        verify(transaction, times(1)).rollback();
        verify(transaction, never()).commit();
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    void changeStatusTaskIfExistTest(TaskStatus status) {
        task.setStatus(status);
        when(taskRepository.findById(1L, session)).thenReturn(Optional.of(task));

        taskService.changeStatus(1L, status);

        Assertions.assertEquals(status, task.getStatus());

        verify(taskRepository,times(1)).update(task, session);
    }

    @Test
    void createTaskTest_withoutCategory() {
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("title");
        taskCreateRequest.setDescription("description");
        taskCreateRequest.setPriority(PriorityValues.LOW);
        taskCreateRequest.setDeadline(Instant.parse("2026-08-23T10:15:30Z"));
        taskCreateRequest.setTelegramId(1L);
        taskCreateRequest.setCategory(null);

        when(userService.getUserByTelegramId(1L)).thenReturn(user);

        taskService.create(taskCreateRequest);

        verify(categoryService, never()).getById(any());
        verify(taskRepository, times(1)).save(any(Task.class), eq(session));
        verify(transaction, times(1)).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void changeStatusTaskIfNotExistTest() {
        task.setStatus(TaskStatus.NOT_STARTED);
        when(taskRepository.findById(1L, session)).thenReturn(Optional.empty());

        TaskNotFoundException exception = Assertions.assertThrowsExactly(TaskNotFoundException.class,
                () -> taskService.changeStatus(1L, TaskStatus.NOT_STARTED));

        Assertions.assertEquals("Задача с ID 1 не найдена", exception.getMessage());
        verify(taskRepository, never()).update(any(Task.class), eq(session));
        verify(transaction, times(1)).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void deleteTaskIfNotExistTest() {
        Long id = 1L;
        when(taskRepository.findById(id, session)).thenReturn(Optional.empty());

        TaskNotFoundException taskNotFoundException = Assertions.assertThrowsExactly(TaskNotFoundException.class,
                () -> taskService.delete(id));

        Assertions.assertEquals("Задача с ID "  + id +  " не найдена", taskNotFoundException.getMessage());
        verify(taskRepository, never()).delete(id, session);

        verify(transaction, times(1)).rollback();
        verify(transaction, never()).commit();
    }

    @Test
    void showAllTastTest() {
        Task task1 = new Task("Title1", "Desc1", Instant.now(), PriorityValues.LOW, user, null);
        Task task2 = new Task("Title2", "Desc2", Instant.now(), PriorityValues.LOW, user, null);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(taskRepository.findByUser(user, session)).thenReturn(tasks);

        List<Task> result = taskService.showAll(user);

        Assertions.assertEquals(2, result.size());
    }

}
