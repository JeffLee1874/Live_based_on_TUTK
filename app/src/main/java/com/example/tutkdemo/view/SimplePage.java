package com.example.tutkdemo.view;

public class SimplePage {
    private String UID;
    private String userName;
    private String passWord;


    public SimplePage(String UID, String userName, String passWord) {
        this.UID = UID;
        this.userName = userName;
        this.passWord = passWord;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
