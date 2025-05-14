package com.example.fakebook;

public class Chathead {
    private String firstName;
    private String lastName;
    private String fullName;
    private String profilePic;
    private String userId;

    public Chathead(){}

    public Chathead(String userId, String firstName, String lastName, String fullName, String profilePic) {
        this.userId = userId;
        this.profilePic = profilePic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getFirstName() { return firstName; }
    public String getProfilePic() { return profilePic; }

    public void setUserId(String userId){
        this.userId = userId;
    }
    public void setFullName(String fullName){
        this.fullName = fullName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
