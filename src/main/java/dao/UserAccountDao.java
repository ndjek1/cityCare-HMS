package dao;

import models.UserAccount;

import java.util.Optional;

public interface UserAccountDao extends GenericDao<UserAccount, Long> {
    public Optional<UserAccount> findByUsername(String username);
    public boolean usernameExists(String username);
}
