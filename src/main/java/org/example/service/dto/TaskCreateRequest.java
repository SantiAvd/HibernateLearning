package org.example.service.dto;

import org.example.entity.Category;
import org.example.model.PriorityValues;

import java.time.Instant;

public class TaskCreateRequest {
    private String title;
    private String description;
    private Instant deadline;
    private PriorityValues priority;
    private Long categoryId;
    private Long telegramId;

    public TaskCreateRequest() {}

    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PriorityValues getPriority() {
        return priority;
    }

    public void setPriority(PriorityValues priority) {
        this.priority = priority;
    }

    public Long getCategory() {
        return categoryId;
    }

    public void setCategory(Long category) {
        this.categoryId = category;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }
}
