package com.example.gabby.dogapp;

/**
 * Created by Gabby on 11/26/2017.
 */

public class UserInformation {
    //set variables you want to store for each user
    public String name;
    public String username;
    public String address;
    public String phone;
    public String bio;
    public boolean isWalker;

    public UserInformation() {

    }

    public UserInformation(String username, String address) {
        this.username = username;
        this.address = address;
    }

    public UserInformation(String name, String phone, String address, String bio, boolean isWalker) {

        this.name = name;
        this.phone = phone;
        this.address = address;
        this.bio = bio;
        this.isWalker = isWalker;
    }
}
