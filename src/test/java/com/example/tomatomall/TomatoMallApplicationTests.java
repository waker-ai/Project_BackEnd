package com.example.tomatomall;

import com.example.tomatomall.po.User;
import com.example.tomatomall.repository.AccountRepository;
import com.example.tomatomall.service.serviceImpl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("marry");
        testUser.setPassword("123456");
        testUser.setName("Marry Jane");
        testUser.setEmail("marry@example.com");
    }

    @Test
    void testCreateUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = accountService.createUser(testUser);

        assertNotNull(createdUser);
        assertEquals("marry", createdUser.getUsername());
        verify(accountRepository, times(1)).save(any(User.class));
    }

    @Test
    void testFindByUsername() {
        when(accountRepository.findByUsername("marry")).thenReturn(testUser);

        User foundUser = accountService.findByUsername("marry");

        assertNotNull(foundUser);
        assertEquals("marry", foundUser.getUsername());
    }

    @Test
    void testUpdateUser() {
        when(accountRepository.findByUsername("marry")).thenReturn(testUser);
        when(accountRepository.save(any(User.class))).thenReturn(testUser);

        testUser.setName("Updated Name");
        User updatedUser = accountService.updateUser(testUser);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        verify(accountRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticate() {
        when(accountRepository.findByUsername("marry")).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        boolean isAuthenticated = accountService.authenticate("marry", "123456");

        assertTrue(isAuthenticated);
    }

    @Test
    void testAuthenticateWithWrongPassword() {
        when(accountRepository.findByUsername("marry")).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        boolean isAuthenticated = accountService.authenticate("marry", "wrongpassword");

        assertFalse(isAuthenticated);
    }
}

