
package io.github.semanticpie.derezhor.externalAgents.users.services;

import io.github.semanticpie.derezhor.externalAgents.users.models.ScUser;
import io.github.semanticpie.derezhor.externalAgents.users.models.enums.UserRole;
import org.ostis.scmemory.model.element.link.ScLinkString;

import java.util.Optional;

public interface UserService {


/**
     * Create formalized user in memory. Users must have unique usernames. This method also store
     * user's hashed password with salt.
     *
     * @param username is user's nickname in memory
     * @param password is user's unencrypted password
     * @param userRole is user's privilege
     * @return ScUser which contains unique strUUID in memory, username, password and user's privilege
     */
    Optional<ScUser> createUser(String username, String password, UserRole userRole);

    Optional<String> getUserUUID(String username);

    Optional<ScLinkString> getUsernameScLink(String username);

    Optional<ScUser> findByUsername(String username);

}

