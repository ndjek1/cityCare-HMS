package org.pahappa.systems.hms.services.impl;


import org.pahappa.systems.hms.dao.PatientDao;
import org.pahappa.systems.hms.dao.StaffDao;
import org.pahappa.systems.hms.dao.UserAccountDao;
import org.pahappa.systems.hms.dao.impl.PatientDaoImpl;
import org.pahappa.systems.hms.dao.impl.StaffDaoImpl;
import org.pahappa.systems.hms.dao.impl.UserAccountDaoImpl;
import org.pahappa.systems.hms.models.UserAccount;
import org.pahappa.systems.hms.services.AuthService;
import org.pahappa.systems.hms.services.LoginResult;
import utils.PasswordUtil;


import java.util.Optional;

public class AuthServiceImpl implements AuthService {

  private final UserAccountDao userAccountDao;
  private final PatientDao patientDao;
  private final StaffDao staffDao;

  public  AuthServiceImpl() {
      this.userAccountDao = new UserAccountDaoImpl();
      this.patientDao = new PatientDaoImpl();
      this.staffDao = new StaffDaoImpl();
  }
    @Override
    public Optional<LoginResult> login(String username, String password) {
        Optional<UserAccount> accountOpt = userAccountDao.findByUsername(username);
        if (accountOpt.isPresent() && PasswordUtil.checkPassword(password, accountOpt.get().getPassword())) {
            Object details = getSpecificEntityDetails(accountOpt.get());
            LoginResult result = new LoginResult(accountOpt.get(),details);
            return Optional.of(result);
        }
        return Optional.empty();
    }

    @Override
    public Object getSpecificEntityDetails(UserAccount userAccount) {
        if (userAccount == null || userAccount.getEntityId() == null) return null;
        return switch (userAccount.getRole()) {
            case PATIENT -> patientDao.findById(userAccount.getEntityId()).orElse(null);
            case DOCTOR, RECEPTIONIST, ADMINISTRATOR -> staffDao.findById(userAccount.getEntityId()).orElse(null);
            default -> null;
        };
    }

    @Override
    public boolean usernameExists(String username) {
        return userAccountDao.usernameExists(username);
    }

    @Override
    public boolean updateLoginCredentialsForUser(Long userAccountId, String newUsername, String newPasswordToHash) {
        Optional<UserAccount> accountOpt = userAccountDao.findById(userAccountId);
        if (accountOpt.isEmpty()) return false;

        UserAccount account = accountOpt.get();
        boolean needsUpdate = false;

        if (newUsername != null && !newUsername.trim().isEmpty() && !account.getUsername().equals(newUsername.trim())) {
            if (userAccountDao.usernameExists(newUsername.trim())) {
                System.err.println("Username '" + newUsername.trim() + "' is already taken.");
                return false;
            }
            account.setUsername(newUsername.trim());
            needsUpdate = true;
        }

        if (newPasswordToHash != null && !newPasswordToHash.isEmpty()) {
            account.setPassword(PasswordUtil.hashPassword(newPasswordToHash));
            needsUpdate = true;
        }

        if (needsUpdate) {
            userAccountDao.update(account);
        }
        return true;
    }
    public Optional<UserAccount> findUserAccountById(Long accountId) {
       return userAccountDao.findById(accountId);
    }
}