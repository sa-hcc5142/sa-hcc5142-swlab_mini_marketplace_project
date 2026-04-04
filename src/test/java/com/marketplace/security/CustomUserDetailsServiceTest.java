package com.marketplace.security;

import com.marketplace.entity.Role;
import com.marketplace.entity.User;
import com.marketplace.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User activeBuyer;

    @BeforeEach
    void setUp() {
        Role buyerRole = new Role();
        buyerRole.setName("BUYER");

        activeBuyer = new User();
        activeBuyer.setId(1L);
        activeBuyer.setUsername("buyer1");
        activeBuyer.setEmail("buyer1@marketplace.local");
        activeBuyer.setPassword("encoded-password");
        activeBuyer.setActive(true);
        activeBuyer.setRoles(Set.of(buyerRole));
    }

    @Test
    void loadUserByUsername_WithEmail_Success() {
        when(userRepository.findByEmail("buyer1@marketplace.local")).thenReturn(Optional.of(activeBuyer));

        UserDetails details = customUserDetailsService.loadUserByUsername("buyer1@marketplace.local");

        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUYER")));
    }

    @Test
    void loadUserByUsername_WithUsernameFallback_Success() {
        when(userRepository.findByEmail("buyer1")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("buyer1")).thenReturn(Optional.of(activeBuyer));

        UserDetails details = customUserDetailsService.loadUserByUsername("buyer1");

        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUYER")));
    }

    @Test
    void loadUserByUsername_InactiveUser_Disabled() {
        activeBuyer.setActive(false);
        when(userRepository.findByEmail("buyer1@marketplace.local")).thenReturn(Optional.of(activeBuyer));

        UserDetails details = customUserDetailsService.loadUserByUsername("buyer1@marketplace.local");

        assertFalse(details.isEnabled());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail("missing@marketplace.local")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("missing@marketplace.local")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@marketplace.local"));
    }
}
