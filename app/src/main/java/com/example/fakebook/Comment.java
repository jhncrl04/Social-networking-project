package com.example.fakebook;

import java.util.Date;

public class Comment {
    private String commentId;
    private String commenterId;
    private Date dateCommented;
    private String postId;
    private String commentContent;
    private String firstName;
    private String lastName;
    private String profilePic;

    public Comment() {
    }

    public Comment(String commentId, String commenterId, Date dateCommented, String postId, String commentContent, String firstName,
                   String lastName, String profilePic) {
        this.commenterId = commenterId;
        this.commentId = commentId;
        this.dateCommented = dateCommented;
        this.postId = postId;
        this.commentContent = commentContent;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePic = profilePic;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public Date getDateCommented() {
        return dateCommented;
    }

    public String getPostId() {
        return postId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getProfilePic(){
        return profilePic;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public void setProfilePic(String profilePic){
        this.profilePic = profilePic;
    }
}


