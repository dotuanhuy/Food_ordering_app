package com.example.project_btl_android;

public class Review {
    private String id;
    private String userId;
    private String productId;
    private String content;
    private String image;
    private float rating;

    public Review() {
    }

    public Review(String id, String userId, String productId, String content, String image, float rating) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.content = content;
        this.image = image;
        this.rating = rating;
    }

    public Review(String userId, String productId, String content, String image, float rating) {
        this.userId = userId;
        this.productId = productId;
        this.content = content;
        this.image = image;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
