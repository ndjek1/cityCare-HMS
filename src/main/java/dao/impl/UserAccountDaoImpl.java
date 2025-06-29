package dao.impl;


import dao.UserAccountDao;

import models.UserAccount;
import org.hibernate.query.Query;
import java.util.Optional;

public class UserAccountDaoImpl extends AbstractDao<UserAccount, Long> implements UserAccountDao {

    public UserAccountDaoImpl() {
        super(UserAccount.class);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        return execute(session -> {
            Query<UserAccount> query = session.createQuery(
                    "FROM UserAccount ua WHERE ua.username = :username", UserAccount.class);
            query.setParameter("username", username);
            return query.uniqueResultOptional();
        });
    }

    @Override
    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }


}