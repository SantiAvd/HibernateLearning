package org.example.service;

import org.example.Repository.CategoryRepository;
import org.example.Repository.TaskRepository;
import org.example.exceptions.CategoryNotFoundException;
import org.example.exceptions.TaskNotFoundException;
import org.example.service.dto.TaskCreateRequest;
import org.example.entity.Category;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.model.*;

import java.util.List;
import java.util.Optional;

public class TaskService {
    private TaskRepository taskRepository;
    private CategoryService categoryService;
    private UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService, CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public void create(TaskCreateRequest request) {

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

        taskRepository.save(task);
    }

    public void setCategory(Long taskId, Category category) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setCategory(category);
            taskRepository.update(task);
        }
    }

    public List<Task> showAll(User user) {
        return taskRepository.findByUser(user);
    }

    public void changeStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        task.setStatus(status);
        taskRepository.update(task);
    }

    public void delete(Long id) {
        Task optionalTask = taskRepository.findById(id).orElseThrow(() ->
                new TaskNotFoundException(id));
        taskRepository.delete(optionalTask.getId());
    }
}
