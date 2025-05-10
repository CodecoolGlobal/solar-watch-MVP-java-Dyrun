package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.dto.user.JwtResponse;
import com.codecool.solarwatch.model.dto.user.UserRequest;
import com.codecool.solarwatch.model.dto.user.UserUpdateResponse;
import com.codecool.solarwatch.model.entity.Member;
import com.codecool.solarwatch.model.entity.Role;
import com.codecool.solarwatch.model.entity.RoleType;
import com.codecool.solarwatch.repository.MemberRepository;
import com.codecool.solarwatch.repository.RoleRepository;
import com.codecool.solarwatch.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserWhenValidInputItCreatesUserWithUserRole() {
        Role mockUserRole = new Role();
        mockUserRole.setRoleType(RoleType.ROLE_USER);

        when(roleRepository.findByRoleType(RoleType.ROLE_USER))
                .thenReturn(Optional.of(mockUserRole));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            assertEquals("testUser", savedMember.getName());
            assertEquals("password123", savedMember.getPassword());
            assertTrue(savedMember.getRoles().contains(mockUserRole));
            return savedMember;
        });

        userService.createUser("testUser", "password123");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void addAdminToUserWhenValidUserItAddsAdminRole() {
        Member existingUser = new Member();
        existingUser.setName("existingUser");
        Role userRole = new Role();
        userRole.setRoleType(RoleType.ROLE_USER);
        existingUser.setRoles(Set.of(userRole));
        Role adminRole = new Role();
        adminRole.setRoleType(RoleType.ROLE_ADMIN);

        when(memberRepository.findByName("existingUser"))
                .thenReturn(Optional.of(existingUser));
        when(roleRepository.findByRoleType(RoleType.ROLE_ADMIN))
                .thenReturn(Optional.of(adminRole));
        when(memberRepository.save(any(Member.class))).thenReturn(existingUser);
        UserUpdateResponse updatedUser = userService.addAdminToUser("existingUser");

        assertTrue(updatedUser.roles().contains(userRole));
        assertTrue(updatedUser.roles().contains(adminRole));
        verify(memberRepository).save(existingUser);
    }

    @Test
    void addAdminToUserWhenUserNotFoundItThrowsException() {
        when(memberRepository.findByName("nonExistingUser"))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> {
            userService.addAdminToUser("nonExistingUser");
        });
    }

    @Test
    void createUserWhenMissingUserRoleItThrowsException() {
        when(roleRepository.findByRoleType(RoleType.ROLE_USER))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> {
            userService.createUser("testUser", "password123");
        });
    }

    @Test
    void loginUserWhenValidCredentialsItReturnsJwtResponseAndSetsSecurityContext() {
        String username = "testUser";
        String password = "password123";
        String mockJwt = "mock.jwt.token";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        User userDetails = new User(username, password, authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication))
                .thenReturn(mockJwt);
        JwtResponse response = userService.loginUser(new UserRequest(username, password));

        assertAll(
                () -> assertEquals(mockJwt, response.jwt()),
                () -> assertEquals(username, response.userName()),
                () -> assertTrue(response.roles().contains("ROLE_USER")),
                () -> assertNotNull(SecurityContextHolder.getContext().getAuthentication()),
                () -> assertEquals(
                        authentication,
                        SecurityContextHolder.getContext().getAuthentication()
                )
        );
    }

    @Test
    void loginUserWhenInvalidCredentialsItThrowsException() {
        String invalidUsername = "wrongUser";
        String invalidPassword = "wrongPass";

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> userService.loginUser(new UserRequest(invalidUsername, invalidPassword)),
                "Expected login to throw for invalid credentials"
        );
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}