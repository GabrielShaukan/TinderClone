package com.shaukan.gabriel.tinderclone.Cards;

//Creating Card class
public class Cards {
    //Card Object instance variables
    private String userId;
    private String name;
    private String profileImageUrl;
    private String age;

    //Card Object Constructor
    public Cards (String userId, String name, String profileImageUrl, String age) {
        this.userId = userId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.age = age;
    }

    //Card Object Methods
    public String getUserId() {
        return userId;
    }
    public  void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

}
