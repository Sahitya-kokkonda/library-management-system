package com.ibizabroker.lms.controller;

import com.ibizabroker.lms.dao.UsersRepository;
import com.ibizabroker.lms.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserController controller;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserController();
        ReflectionTestUtils.setField(controller, "usersRepository", usersRepository);
        ReflectionTestUtils.setField(controller, "passwordEncoder", passwordEncoder);
    }

    @Test
    void registerNewUserEncryptsPasswordBeforeSaving() {
        Users user = new Users();
        user.setUsername("reader1");
        user.setName("Reader One");
        user.setPassword("plain-password");
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        Users registered = controller.registerNewUser(user);

        assertEquals("encoded-password", registered.getPassword());
        verify(usersRepository).save(user);
    }
}
