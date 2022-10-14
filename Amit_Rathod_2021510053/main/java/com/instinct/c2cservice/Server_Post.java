package com.instinct.c2cservice;

public class Server_Post {
    private String ProfileImage;
    private String usertype;
    private String Service;
    private String ShopAddress;
    private String ShopName;
    private String fullname;
   private String ShopCity;
    public Server_Post() {

    }

    public String getShopCity() {
        return ShopCity;
    }

    public void setShopCity(String shopCity) {
        ShopCity = shopCity;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String ProfileImage) {
        this.ProfileImage = ProfileImage;
    }



    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
    public String getService() {
        return Service;
    }

    public void setService(String Service) {
        this.Service = Service;
    }

    public String getShopAddress() {
        return ShopAddress;
    }

    public void setShopAddress(String ShopAddress) {
        this.ShopAddress = ShopAddress;
    }

    public String getShopName() {
        return ShopName;
    }

    public void setShopName(String ShopName) {
        this.ShopName = ShopName;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Server_Post(String ProfileImage,String usertype, String Service, String ShopAddress, String ShopName, String fullname,String shopCity) {
        this.ProfileImage = ProfileImage;
        this.Service = Service;
        this.ShopAddress = ShopAddress;
        this.ShopName = ShopName;
        this.usertype = usertype;
        this.fullname = fullname;
        this.ShopCity = shopCity;
    }
}
