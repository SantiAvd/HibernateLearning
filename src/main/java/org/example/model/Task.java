package org.example.model;

import  jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status = TaskStatus.NOT_STARTED;

    @Column(name = "deadline")
    private Instant deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private PriorityValues priority;
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne
    @JoinColumn(
            name = "category_id"
    )
    private Category category;

    public Task(){}

    public Task(String title, String description, Instant deadline, PriorityValues priority, User user, Category category) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.user = user;
        this.category = category;
    }

    public long getId() { return id;}
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public String getDescription() { return description; }
    public Instant getDeadline() { return deadline; }
    public TaskStatus getStatus() { return status; }
    public PriorityValues getPriority() { return priority; }
    public User getUser() { return user; }
    public Category getCategory() { return category; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setPriority(PriorityValues priority) { this.priority = priority; }
    public void setUser(User user) { this.user = user; }
    public void setCategory(Category category) { this.category = category; }

    @Override
    public String toString() {
        return id + "\n" +
                "📌 " + title + "\n" +
                "📝 " + (description != null && !description.isBlank() ? description : "без описания") + "\n" +
                "📅 Срок: " + formatDeadline() + "\n" +
                "🚦 Приоритет: " + formatPriority() + "\n" +
                "📂 Категория: " + (category != null ? category.getName() : "без категории") + "\n" +
                "📊 Статус: " + status;
    }

    private String formatDeadline() {
        if (deadline == null) {
            return "не указан";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return LocalDateTime.ofInstant(deadline, ZoneId.systemDefault()).format(formatter);
    }

    private String formatPriority() {
        if (priority == null) {
            return "не указан";
        }
        return switch (priority) {
            case LOW -> "🟢 LOW";
            case MEDIUM -> "🟡 MEDIUM";
            case HIGHT -> "🔴 HIGH";
            case URGENT -> "URGENT";
        };
    }
}
