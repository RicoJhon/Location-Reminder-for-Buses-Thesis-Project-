package com.richez.ladylyn.buscommuterapp.Model;

/**
 * Created by Ricojhon on 26/09/2018.
 */

public class User {
    private String email,password,name,phone,id,profileUrl;
    public User(){

    }

    public User(String email,String password,String name, String phone,String id){
        this.email=email;
        this.password=password;
        this.name=name;
        this.phone=phone;
        this.id=id;

    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getuserID() {
        return id;
    }

    public void setuserID(String id) {
        this.id = id;
    }
}


