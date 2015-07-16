package org.jhu.gis.usertool.model;

public class LoginCredential {

    private String userName;
    private String password;

    public LoginCredential(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
