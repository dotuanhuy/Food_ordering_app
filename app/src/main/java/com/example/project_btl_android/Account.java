package com.example.project_btl_android;

import java.io.Serializable;

public class Account implements Serializable {
    private String id;
    private String UserName;
    private String Password;
    private String Email;
    private String PhoneNumber;
    private int RoleId;
    private boolean Active;

    public Account() {
    }

    public Account(String id, String userName) {
        this.id = id;
        UserName = userName;
    }

    public Account(String id, String userName, String password, String email, String phoneNumber, int roleId, boolean active) {
        this.id = id;
        UserName = userName;
        Password = password;
        Email = email;
        PhoneNumber = phoneNumber;
        RoleId = roleId;
        this.Active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public int getRoleId() {
        return RoleId;
    }

    public void setRoleId(int roleId) {
        RoleId = roleId;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }
}
