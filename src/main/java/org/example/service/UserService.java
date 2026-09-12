package org.example.service;

import org.example.Repository.UserRepository;
import org.example.entity.User;
import org.example.exceptions.UserNotFoundException;
import org.example.model.UserState;
import org.example.service.dto.UserRegistrationResult;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Objects;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;
    private final SessionFactory sessionFactory;

    public UserService(UserRepository userRepository, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.userRepository = userRepository;
    }

    public UserRegistrationResult getOrCreateUser(
            Long telegramId,
            String username,
            String firstName
    ) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                Optional<User> optionalUser = userRepository.findByTelegramId(telegramId, session);

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
                        userRepository.update(
                                existingUser,
                                session
                        );
                    }

                    tx.commit();

                    return new UserRegistrationResult(
                            existingUser,
                            false
                    );
                }

                User user = new User(
                        telegramId,
                        username,
                        firstName
                );

                try {
                    userRepository.save(user, session);
                    tx.commit();
                    return new UserRegistrationResult(user, true);

                } catch (RuntimeException e) {

                    Optional<User> raceWinner = userRepository.findByTelegramId(telegramId, session);
                    if (raceWinner.isPresent()) {
                        tx.commit();

                        return new UserRegistrationResult(raceWinner.get(), false);
                    }

                    throw e;
                }

            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }

                throw e;
            }
        }
    }

    public void updateState(Long telegramId, UserState newState) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                User user = userRepository.findByTelegramId(telegramId, session)
                        .orElseThrow(() -> new UserNotFoundException(telegramId));

                user.setState(newState);
                userRepository.update(user, session);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public User getUserByTelegramId(Long telegramId) {
        try (Session session = sessionFactory.openSession()) {
            return userRepository.
                    findByTelegramId(telegramId, session).orElseThrow(() ->
                        new UserNotFoundException(telegramId));
        }
    }
}
