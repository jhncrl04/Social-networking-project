package com.example.fakebook;

import java.util.Date;

public class Post {
    private String authorName;
    private String postId;
    private String content;
    private String imageBytes;
    private String userID;
    private String firstName;
    private Date dateCreated;
    private Long likesCount;
    private Long commentsCount;
    private String posterProfile;

    public Post() {
    }  // Needed for Firestore

    public Post(String firstName, String authorName, String postId, String content, String imageBytes, String userID, Date dateCreated,
                Long likesCount,
                Long commentsCount, String posterProfile) {
        this.firstName = firstName;
        this.authorName = authorName;
        this.postId = postId;
        this.content = content;
        this.imageBytes = imageBytes;
        this.userID = userID;
        this.dateCreated = dateCreated;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.posterProfile = posterProfile;
    }

    // Getters
    public String getFirstName(){
        return firstName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getPostId() {
        return postId;
    }

    public String getContent() {
        return content;
    }

    public String getImageBytes() {
        return imageBytes;
    }

    public String getUserID() {
        return userID;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Long getLikesCount() {
        return likesCount;
    }

    public Long getCommentsCount() {
        return commentsCount;
    }

    public String getPosterProfile() {
        return posterProfile;
    }

    public void setFirstName(String firstName) { this.firstName = firstName; };

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setPosterProfile(String posterProfile) {
        this.posterProfile = posterProfile;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }
}
