package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.dto.user.AddAdminRequest;
import com.codecool.solarwatch.model.dto.user.JwtResponse;
import com.codecool.solarwatch.model.dto.user.UserRequest;
import com.codecool.solarwatch.model.dto.user.UserUpdateResponse;
import com.codecool.solarwatch.model.entity.Member;
import com.codecool.solarwatch.security.jwt.JwtUtils;
import com.codecool.solarwatch.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public UserController(PasswordEncoder encoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager, UserService userService) {
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> createUser(@RequestBody UserRequest signUpRequest) {
        userService.createUser(signUpRequest.username(), encoder.encode(signUpRequest.password()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody UserRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity
                .ok(new JwtResponse(jwt, userDetails.getUsername(), roles));
    }

    @PutMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserUpdateResponse> addAdmin(@RequestBody AddAdminRequest request) {
        Member updatedUser = userService.addAdminToUser(request.username());
        return ResponseEntity.ok(new UserUpdateResponse(updatedUser.getName(), updatedUser.getRoles()));
    }

}

