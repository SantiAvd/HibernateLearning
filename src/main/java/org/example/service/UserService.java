package org.example.service;

import org.example.Repository.UserRepository;
import org.example.entity.User;
import org.example.exceptions.UserNotFoundException;
import org.example.model.UserState;
import org.example.service.dto.UserRegistrationResult;

import java.util.Objects;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRegistrationResult getOrCreateUser(
            Long telegramId,
            String username,
            String firstName
    ) {

        Optional<User> optionalUser = userRepository.findByTelegramId(telegramId);
         if (optionalUser.isPresent()) {
             User existingUser = optionalUser.get();

             boolean changed = false;
             if (!Objects.equals(existingUser.getUserName(), username)) {
                 existingUser.setUserName(username);
                 changed = true;
             }
             if (!Objects.equals(existingUser.getFirstName(), firstName)) {
                 existingUser.setFirstName(firstName);
                 changed = true;
             }

             if (changed) {
                 userRepository.update(existingUser);
             }

             return new UserRegistrationResult(existingUser, false);
         }
        User user = new User(
                telegramId,
                username,
                firstName
        );

        try {
            userRepository.save(user);
        } catch (RuntimeException e) {
            Optional<User> raceWinner = userRepository.findByTelegramId(telegramId);
            if (raceWinner.isPresent()) {
                return new UserRegistrationResult(raceWinner.get(), false);
            }
            throw e;
        }

         return new UserRegistrationResult(user, true);
    }

    public void updateState(Long telegramId, UserState newState) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException(telegramId));

        user.setState(newState);
        userRepository.update(user);
    }

    public void update(User user) {
        userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException(user.getId()));

        userRepository.update(user);
    }

    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() ->
                        new RuntimeException("Пользователь не найден"));
    }
}
