package io.github.semanticpie.derezhor.externalAgents.users.dtos;

import io.github.semanticpie.derezhor.externalAgents.users.models.enums.UserRole;
import lombok.Data;

import java.util.List;

@Data
public class SignUpRequest {
    private String username;
    private String password;
    private UserRole userRole;
    private List<GenreDTO> favoriteGenres;
}