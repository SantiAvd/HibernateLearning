package org.example.service;

import org.example.Repository.UserRepository;
import org.example.model.User;

import java.util.Optional;
import java.util.SortedMap;

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
             return new UserRegistrationResult(optionalUser.get(), false);
         }
        User user = new User(
                telegramId,
                username,
                firstName
        );

         userRepository.save(user);
         return new UserRegistrationResult(user, true);
    }

    public void update(User user) {
        userRepository.update(user);
    }

    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() ->
                        new RuntimeException("Пользователь не найден"));
    }
}
