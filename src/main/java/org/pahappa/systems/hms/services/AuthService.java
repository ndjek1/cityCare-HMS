package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.models.UserAccount;
import java.util.Optional;

public interface AuthService {
    Optional<LoginResult> login(String username, String password);
    Object getSpecificEntityDetails(UserAccount userAccount);
    boolean updateLoginCredentialsForUser(Long userAccountId, String newUsername, String newPasswordToHash);
    boolean usernameExists(String username);
}