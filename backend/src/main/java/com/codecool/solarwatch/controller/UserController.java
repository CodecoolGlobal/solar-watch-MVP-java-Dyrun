package com.codecool.solarwatch.controller;

import com.codecool.solarwatch.model.dto.user.AddAdminRequest;
import com.codecool.solarwatch.model.dto.user.JwtResponse;
import com.codecool.solarwatch.model.dto.user.UserRequest;
import com.codecool.solarwatch.model.dto.user.UserUpdateResponse;
import com.codecool.solarwatch.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final PasswordEncoder encoder;
    private final UserService userService;

    public UserController(PasswordEncoder encoder, UserService userService) {
        this.encoder = encoder;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody UserRequest signUpRequest) {
        userService.createUser(signUpRequest.username(), encoder.encode(signUpRequest.password()));
    }

    @PostMapping("/login")
    public JwtResponse authenticateUser(@RequestBody UserRequest loginRequest) {
        return userService.loginUser(loginRequest);
    }

    @PutMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public UserUpdateResponse addAdmin(@RequestBody AddAdminRequest request) {
        return userService.addAdminToUser(request.username());
    }

}

