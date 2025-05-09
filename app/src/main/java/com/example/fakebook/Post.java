package com.example.fakebook;

import java.util.Date;

public class Post {
    private String authorName;
    private String postId;
    private String content;
    private String posterId;
    private Date dateCreated;
    private Long likesCount;
    private Long commentsCount;

    public Post() {}  // Needed for Firestore

    public Post(String authorName, String postId, String content, String posterId, Date dateCreated, Long likesCount, Long commentsCount) {
        this.authorName = authorName;
        this.postId = postId;
        this.content = content;
        this.posterId = posterId;
        this.dateCreated = dateCreated;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    // Getters
    public String getAuthorName() { return authorName; }
    public String getPostId() { return postId; }
    public String getContent() { return content; }
    public String getPosterID() { return posterId; }
    public Date getDateCreated() { return  dateCreated; }
    public Long getLikesCount() { return likesCount; }
    public Long getCommentsCount() { return commentsCount; }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
