package com.uottawa.servicenovigrad1.user;

public class EmployeeAccount extends UserAccount {

    String branch;

    public EmployeeAccount(String name, String email, String uid) {
        super(name, email, "employee", uid);
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String b) {
        this.branch = b;
    }

    @Override
    public String toString() {
        return "UID: " + getUID()
            + "\nName: " + getName()
            + "\nEmail: " + getEmail()
            + "\nRole: " + getRole()
            + "\nBranch: " + (getBranch() != null ? getBranch() : "Not assigned yet");
    }
}
