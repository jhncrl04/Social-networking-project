package com.example.fakebook;

import java.util.Date;

public class Account {
    private String firstName;
    private String lastName;
    private String fullname;
    private String userID;
    private String profilePic;
    private Date followDate;
    private int followerCount;

    public Account(){}

    public Account(String firstName, String lastName, String fullname, String userID, String profilePic, Date followDate, int followerCount){
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullname = fullname;
        this.userID = userID;
        this.profilePic = profilePic;
        this.followDate = followDate;
        this.followerCount = followerCount;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullname() {
        return fullname;
    }

    public String getUserID() {
        return userID;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public Date getFollowDate() {
        return followDate;
    }

    public int getFollowerCount(){
        return followerCount;
    }

    // setter
    public void setFullname(String fullname){
        this.fullname = fullname;
    }

    public void setUserID(String userID){
        this.userID = userID;
    }
}
