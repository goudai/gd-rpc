package io.goudai.network;

import java.io.Serializable;

/**
 * Created by freeman on 2016/1/14.
 */
public class User implements Serializable {
    private String username = " default username";
    private String password = "default password";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
