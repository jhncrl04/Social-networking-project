package com.example.fakebook;

import java.util.Date;

public class NotifItem {

    private String firstName;
    private String lastName;
    private String profilePic;
    private Date notifDate;
    private String notifType;

    public NotifItem() {
    }

    public NotifItem(String firstName, String lastName, String profilePic, Date notifDate, String notifType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.notifDate = notifDate;
        this.profilePic = profilePic;
        this.notifType = notifType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public Date getNotifDate() {
        return notifDate;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProfilePic(String profilePic){
        this.profilePic = profilePic;
    }
}
