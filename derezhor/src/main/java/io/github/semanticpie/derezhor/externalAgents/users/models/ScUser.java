package io.github.semanticpie.derezhor.externalAgents.users.models;

import io.github.semanticpie.derezhor.externalAgents.users.models.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScUser {
    private String uuid;
    private String username;
    private String password;
    private UserRole userRole;
}
