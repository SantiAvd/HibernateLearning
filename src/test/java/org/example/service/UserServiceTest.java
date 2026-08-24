package org.example.service;

import org.example.Repository.UserRepository;
import org.example.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    UserService userService;

    User user;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "AlexBob","Alex");
    }

    @Test
    void getOrCreateUserIfNotExistTest() {
       String userName = "AlexBob";
       String firstName = "Alex";
       Long telegramId = 1L;

       when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
       UserRegistrationResult result = userService.getOrCreateUser(telegramId,userName,firstName);

       Assertions.assertFalse(result.isNew());
       Assertions.assertEquals("AlexBob", result.user().getUserName());

       verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getOrCreateUserIfExistTest() {
        when(userRepository.findByTelegramId(2L)).thenReturn(Optional.empty());
        UserRegistrationResult result = userService.getOrCreateUser(2L,"NewUser","User");

        Assertions.assertTrue(result.isNew());
        Assertions.assertEquals("NewUser", result.user().getUserName());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateTest() {
        userService.update(user);

        verify(userRepository,times(1)).update(user);
    }

    @Test
    void getUserBytelegramIdIfExistTest() {
        when(userRepository.findByTelegramId(1L)).thenReturn(Optional.of(user));

        User resultUser = userService.getUserByTelegramId(1L);
        System.out.println(resultUser);
        Assertions.assertEquals(1L, resultUser.getTelegramId());
    }

    @Test
    void getUserBytelegramIdIfNotExistTest() {
        when(userRepository.findByTelegramId(1L)).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrowsExactly(RuntimeException.class,
                () -> userService.getUserByTelegramId(1L));

        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }
}