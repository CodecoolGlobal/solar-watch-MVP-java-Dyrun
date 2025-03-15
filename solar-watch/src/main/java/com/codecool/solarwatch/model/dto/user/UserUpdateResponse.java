package com.codecool.solarwatch.model.dto.user;

import com.codecool.solarwatch.model.entity.Role;

import java.util.Set;

public record UserUpdateResponse(String username, Set<Role> roles) {
}
