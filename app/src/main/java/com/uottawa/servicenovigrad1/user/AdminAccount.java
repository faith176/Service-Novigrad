package com.uottawa.servicenovigrad1.user;

public class AdminAccount extends UserAccount {


    public AdminAccount(String uid) {
        super("admin", "admin", "admin", uid);
    }

    @Override
    public String toString() {
        return null;
    }
}
