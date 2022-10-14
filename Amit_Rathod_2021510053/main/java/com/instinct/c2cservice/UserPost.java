package com.instinct.c2cservice;

public class UserPost {

    private String FullName;
    private String Phone;
    private String PostDesc;

    public UserPost(String fullName, String phone, String postDesc) {
        FullName = fullName;
        Phone = phone;
        PostDesc = postDesc;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPostDesc() {
        return PostDesc;
    }

    public void setPostDesc(String postDesc) {
        PostDesc = postDesc;
    }

    public UserPost() {

    }

}
