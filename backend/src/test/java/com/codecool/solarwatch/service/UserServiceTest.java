package com.codecool.solarwatch.service;

import com.codecool.solarwatch.model.entity.Member;
import com.codecool.solarwatch.model.entity.Role;
import com.codecool.solarwatch.model.entity.RoleType;
import com.codecool.solarwatch.repository.MemberRepository;
import com.codecool.solarwatch.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void createUserWhenValidInputItCreatesUserWithUserRole() {
        Role mockUserRole = new Role();
        mockUserRole.setRoleType(RoleType.ROLE_USER);
        when(roleRepository.findByRoleType(RoleType.ROLE_USER))
                .thenReturn(Optional.of(mockUserRole));
        userService.createUser("testUser", "password123");
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertEquals("testUser", savedMember.getName());
        assertEquals("password123", savedMember.getPassword());
        assertTrue(savedMember.getRoles().contains(mockUserRole));
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
        Member updatedUser = userService.addAdminToUser("existingUser");
        assertTrue(updatedUser.getRoles().contains(userRole));
        assertTrue(updatedUser.getRoles().contains(adminRole));
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
}