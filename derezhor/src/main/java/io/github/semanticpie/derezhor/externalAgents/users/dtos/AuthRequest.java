package io.github.semanticpie.derezhor.externalAgents.users.dtos;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
