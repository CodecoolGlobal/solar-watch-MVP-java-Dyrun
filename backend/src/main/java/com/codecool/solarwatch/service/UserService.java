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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(MemberRepository memberRepository, RoleRepository roleRepository, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public void createUser(String name, String password) {
        Member user = new Member();
        Role roleUser = roleRepository.findByRoleType(RoleType.ROLE_USER).orElseThrow();
        user.setName(name);
        user.setPassword(password);
        user.setRoles(Set.of(roleUser));
        memberRepository.save(user);
    }

    public UserUpdateResponse addAdminToUser(String username) {
        logger.info("Adding admin to user: {}", username);
        Member user = memberRepository.findByName(username).orElseThrow();
        Role roleAdmin = roleRepository.findByRoleType(RoleType.ROLE_ADMIN).orElseThrow();
        Set<Role> updatedRoles = new HashSet<>(user.getRoles());
        updatedRoles.add(roleAdmin);
        user.setRoles(updatedRoles);
        memberRepository.save(user);
        return new UserUpdateResponse(user.getName(), user.getRoles());
    }

    public JwtResponse loginUser(UserRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(jwt, userDetails.getUsername(), roles);
    }
}
