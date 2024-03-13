package io.github.semanticpie.derezhor.externalAgents.users.services;

import io.github.semanticpie.derezhor.externalAgents.users.dtos.AuthRequest;
import io.github.semanticpie.derezhor.externalAgents.users.dtos.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    ResponseEntity<?> createNewUser(@RequestBody SignUpRequest signUpRequest);

    ResponseEntity<?> createAuthToken(@RequestBody AuthRequest authRequest);
}
