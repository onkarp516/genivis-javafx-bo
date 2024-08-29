package com.opethic.genivis.models.api;

import java.io.Serializable;

public class LoginApiRequest implements Serializable {
    private String usercode;
    private String password;

    public String getUsercode() {
        return usercode;
    }

    public void setUsercode(String usercode) {
        this.usercode = usercode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
