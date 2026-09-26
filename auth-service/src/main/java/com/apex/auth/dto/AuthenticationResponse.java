package com.apex.auth.dto;

import com.apex.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private UUID id;
    private String accessToken;
    private String refreshToken;
    private Role role;
    private String email;
}