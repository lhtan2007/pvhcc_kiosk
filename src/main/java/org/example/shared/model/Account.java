package org.example.shared.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Account")
public class Account {
    @Id
    private String userName;
    private String hashedPwd;
    private List<UUID> departments;
    private int role;
    private boolean isLoggedIn;

    public Account(String userName, String hashedPwd, int role) {
        this.userName = userName;
        this.hashedPwd = hashedPwd;
        this.role = role;
        this.departments = new ArrayList<>();
        this.isLoggedIn = false;
    }

    public Account() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHashedPwd() {
        return hashedPwd;
    }

    public void setHashedPwd(String hashedPwd) {
        this.hashedPwd = hashedPwd;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public List<UUID> getDepartments() {
        return departments;
    }

    public boolean getLoginStatus() {
        return isLoggedIn;
    }

    public void setLoginStatus(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }
}
