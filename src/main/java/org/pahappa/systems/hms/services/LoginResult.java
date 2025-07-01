package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.models.UserAccount;

public class LoginResult {
    private final UserAccount userAccount;
    private final Object userDetails; // Staff or Patient

    public LoginResult(UserAccount userAccount, Object userDetails) {
        this.userAccount = userAccount;
        this.userDetails = userDetails;
    }

    public UserAccount getUserAccount() { return userAccount; }
    public Object getUserDetails() { return userDetails; }
}