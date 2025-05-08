package com.codecool.solarwatch.model.dto.user;

import java.util.List;

public record JwtResponse(String jwt, String userName, List<String> roles) {
}
