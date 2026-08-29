package org.example.exceptions;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("Задача с ID " + id + " не найдена");
    }
}
