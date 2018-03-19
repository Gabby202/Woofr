package com.example.gabby.dogapp;

/**
 * Created by Gabby on 11/26/2017.
 */

public class UserInformation {
    //set variables you want to store for each user
    public String name;
    public String username;
    public String phone;
    public boolean isWalker;

    public UserInformation() {

    }

    public UserInformation(String username) {
        this.username = username;

    }

    public UserInformation(String name, String phone, boolean isWalker) {

        this.name = name;
        this.phone = phone;
        this.isWalker = isWalker;
    }
}
