package org.example.service;


import org.example.Repository.TaskRepository;
import org.example.dto.TaskCreateRequest;
import org.example.entity.Category;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.model.CategoryCollors;
import org.example.model.PriorityValues;
import org.example.model.TaskStatus;
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
    @InjectMocks
    TaskService taskService;
    User user;
    Task task;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "Alex", "Ale");
        task = new Task("Title", "Desc", Instant.now(), PriorityValues.LOW, user, null);
    }

    @Test
    void createTaskTest() {
        Category category = new Category("Home", CategoryCollors.ORANGE);
        TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("title");
        taskCreateRequest.setDescription("description");
        taskCreateRequest.setCategory(category);
        taskCreateRequest.setPriority(PriorityValues.LOW);
        taskCreateRequest.setDeadline(Instant.parse("2026-08-23T10:15:30Z"));

        taskService.create(taskCreateRequest, user);

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void setCategoryIfExistTest() {
        Category category = new Category("Home", CategoryCollors.ORANGE);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.setCategory(1L, category);

        verify(taskRepository, times(1)).update(any(Task.class));
    }

    @Test
    void setCategoryIfNotExistTest() {
        Category category = new Category("Home", CategoryCollors.ORANGE);
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        taskService.setCategory(1L, category);

        verify(taskRepository, never()).update(any(Task.class));
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    void changeStatusTaskIfExistTest(TaskStatus status) {
        task.setStatus(status);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.changeStatus(1L, status);

        Assertions.assertEquals(status, task.getStatus());

        verify(taskRepository,times(1)).update(task);
    }

    @Test
    void changeStatusTaskIfNotExistTest() {
        task.setStatus(TaskStatus.NOT_STARTED);
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        taskService.changeStatus(1L, TaskStatus.NOT_STARTED);

        verify(taskRepository,never()).update(any(Task.class));
    }

    @Test
    void deleteTaskIfExistTest() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L);

        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTaskIfNotExistTest() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        taskService.delete(1L);

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void showAllTastTest() {
        Task task1 = new Task("Title1", "Desc1", Instant.now(), PriorityValues.LOW, user, null);
        Task task2 = new Task("Title2", "Desc2", Instant.now(), PriorityValues.LOW, user, null);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(taskRepository.findByUser(user)).thenReturn(tasks);

        List<Task> result = taskService.showAll(user);

        Assertions.assertEquals(2, result.size());
    }


}
