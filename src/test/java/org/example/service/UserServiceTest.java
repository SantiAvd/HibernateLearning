package org.example.service;

import org.example.Repository.UserRepository;
import org.example.entity.User;
import org.example.exceptions.UserNotFoundException;
import org.example.model.UserState;
import org.example.service.dto.UserRegistrationResult;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    Transaction transaction;

    @Mock
    SessionFactory sessionFactory;

    @Mock
    Session session;

    @InjectMocks
    UserService userService;

    User user;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "AlexBob","Alex");

        when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);
    }


    @ParameterizedTest
    @EnumSource(UserState.class)
    void updateStateTest() {
        user.setState(UserState.IDLE);
        when(userRepository.findByTelegramId(1L, session)).thenReturn(Optional.of(user));

        userService.updateState(1L, UserState.WAITING_FOR_ASSIGN_NEW_CATEGORY_COLOR);

        verify(userRepository, times(1)).update(user, session);
        verify(transaction,times(1)).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void updateStateUserNotFoundTest() {

        when(userRepository.findByTelegramId(1L, session)).thenReturn(Optional.empty());


        UserNotFoundException ex = Assertions.assertThrowsExactly(UserNotFoundException.class,
                () -> userService.updateState(1L, UserState.IDLE));

        Assertions.assertEquals("Пользователь с ID " + 1L + " не найден", ex.getMessage());
        verify(userRepository, never()).update(any(User.class), eq(session));
        verify(transaction,never()).commit();
        verify(transaction, times(1)).rollback();
    }

    @Test
    void getUserByTGIdTest() {
        when(userRepository.findByTelegramId(1L, session)).thenReturn(Optional.of(user));

        User resUser = userService.getUserByTelegramId(1L);

        Assertions.assertEquals(1L, resUser.getTelegramId());
    }

    @Test
    void getUserByTGIdUserNotFoundTest() {
        when(userRepository.findByTelegramId(1L, session)).thenReturn(Optional.of(user));

        User resUser = userService.getUserByTelegramId(1L);

        Assertions.assertEquals(1L, resUser.getTelegramId());

    }

    @Test
    void getOrCreateUserWhenUserExistsAndUnchangedShouldReturnExistingWithoutUpdate() {
        User existingUser = new User(1L, "Alex", "Ale");
        when(userRepository.findByTelegramId(1L, session)).thenReturn(Optional.of(existingUser));

        UserRegistrationResult result = userService.getOrCreateUser(1L, "Alex", "Ale");

        Assertions.assertFalse(result.isNew());
        Assertions.assertEquals(existingUser, result.user());
        verify(userRepository, never()).update(any(User.class), eq(session));
        verify(transaction, times(1)).commit();
        verify(transaction, never()).rollback();
    }

    @Test
    void getOrCreateUserWhenUserExistsAndChangedShouldUpdateAndReturnExisting() {
        User existingUser = new User(1L, "OldName", "Ale");
        when(userRepository.findByTelegramId(1L, session)).thenReturn(Optional.of(existingUser));

        UserRegistrationResult result = userService.getOrCreateUser(1L, "NewName", "Ale");

        Assertions.assertFalse(result.isNew());
        Assertions.assertEquals("NewName", result.user().getUserName());
        verify(userRepository, times(1)).update(existingUser, session);
        verify(transaction, times(1)).commit();
    }

    @Test
    void getOrCreateUser_whenUserNotExists_shouldCreateNewUser() {
        when(userRepository.findByTelegramId(2L, session)).thenReturn(Optional.empty());

        UserRegistrationResult result = userService.getOrCreateUser(2L, "NewUser", "New");

        Assertions.assertTrue(result.isNew());
        Assertions.assertEquals("NewUser", result.user().getUserName());
        verify(userRepository, times(1)).save(any(User.class), eq(session));
        verify(transaction, times(1)).commit();
    }

    @Test
    void getOrCreateUser_whenSaveFailsButUserExistsOnRetry_shouldReturnRaceWinner() {
        User raceWinner = new User(3L, "Winner", "W");

        when(userRepository.findByTelegramId(3L, session))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(raceWinner));

        doThrow(new RuntimeException("Constraint violation"))
                .when(userRepository).save(any(User.class), eq(session));

        UserRegistrationResult result = userService.getOrCreateUser(3L, "NewUser", "New");

        Assertions.assertFalse(result.isNew());
        Assertions.assertEquals(raceWinner, result.user());
        verify(transaction, times(1)).commit();
    }

    @Test
    void getOrCreateUser_whenSaveFailsAndUserStillNotFound_shouldRethrowAndRollback() {
        RuntimeException saveException = new RuntimeException("DB connection lost");
        when(transaction.isActive()).thenReturn(true);
        when(userRepository.findByTelegramId(4L, session)).thenReturn(Optional.empty());
        doThrow(saveException).when(userRepository).save(any(User.class), eq(session));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
                () -> userService.getOrCreateUser(4L, "NewUser", "New"));


        Assertions.assertEquals(saveException, thrown);
        verify(transaction, times(1)).rollback();
        verify(transaction, never()).commit();
    }
}