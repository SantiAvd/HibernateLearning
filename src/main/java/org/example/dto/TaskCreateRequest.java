package org.example.dto;

import org.example.model.Category;
import org.example.model.PriorityValues;

import java.time.Instant;

public class TaskCreateRequest {
    private String title;
    private String description;
    private Instant deadline;
    private PriorityValues priority;
    private Category category;

    public TaskCreateRequest() {}

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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public void setDeadline(Instant deadline) {
        this.deadline = deadline;
    }
}
