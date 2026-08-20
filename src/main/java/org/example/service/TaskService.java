package org.example.service;

import org.example.Repository.TaskRepository;
import org.example.dto.TaskCreateRequest;
import org.example.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class TaskService {
    private TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    public void create(TaskCreateRequest request, User user) {
        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getDeadline(),
                request.getPriority(),
                user,
                request.getCategory());

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
       Optional<Task> optionalTask = taskRepository.findById(id);
       if (optionalTask.isPresent()) {
           Task task = optionalTask.get();
           task.setStatus(status);
           taskRepository.update(task);
       }
    }

    public void delete(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            taskRepository.delete(task.get());
        }
    }
}
