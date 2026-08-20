package org.example.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "telegram_id", unique = true, nullable = false)
   private Long telegramId;

   @Column(name = "user_name")
   private String userName;

   @Column(name = "first_name")
   private String firstName;

   @Column(name = "created_at", nullable = false)
   private Instant createdAt = Instant.now();

   @Enumerated(EnumType.STRING)
   @Column(name = "state")
   private UserState state = UserState.IDLE;

   @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
   List<Task> tasks;

   public User() {}

   public User(Long telegramId, String userName, String firstName) {
      this.telegramId = telegramId;
       this.userName = userName;
      this.firstName = firstName;
   }

   public long getId() {
      return id;
   }

   public long getTelegramId() {
      return telegramId;
   }

   public String getUserName() {
      return userName;
   }

   public String getFirstName() {
      return firstName;
   }

   public Instant getCreatedAt() {
      return createdAt;
   }

   public void setId(long id) {
      this.id = id;
   }

   public void setTelegramId(long telegramId) {
      this.telegramId = telegramId;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public void setCreatedAt(Instant createdAt) {
      this.createdAt = createdAt;
   }

   public UserState getState() {
      return state;
   }

   public void setState(UserState state) {
      this.state = state;
   }

   @Override
   public String toString() {
      return "User{" +
              "id=" + id +
              ", telegram_id=" + telegramId +
              ", userName='" + userName + '\'' +
              ", firstName='" + firstName + '\'' +
              ", createdAt=" + createdAt +
              '}';
   }
}
