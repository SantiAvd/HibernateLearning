package org.example.entity;

import jakarta.persistence.*;
import org.example.model.CategoryCollors;
import org.example.mapper.CollorMapper;

import java.util.List;

@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private CategoryCollors color;

    @OneToMany(mappedBy = "category")
    private List<Task> tasks;

    public Category(){}

    public Category(String name, CategoryCollors color) {
        this.name = name;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryCollors getColor() {
        return color;
    }

    public void setColor(CategoryCollors color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return id + "| Название  - " + name  + "| Цвет - " + CollorMapper.collorMapper(color);
    }
}
