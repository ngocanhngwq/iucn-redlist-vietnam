package com.technifysoft.bookapp.models;

public class ModelPdf {
    //variables
    String uid, id, title, description, categoryId, url;
    //Thêm trường image, nội dung
    String image, content;
    long timestamp, viewsCount, downloadsCount;
    boolean favorite;

    //empty constructor, required for firebase
    public ModelPdf() {
    }

    //constructor with all params
    public ModelPdf(String uid, String id, String title, String description, String categoryId,
                    String url, String content, String image, long timestamp,
                    long viewsCount, long downloadsCount, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.viewsCount = viewsCount;
        this.downloadsCount = downloadsCount;
        this.favorite = favorite;
        this.content = content;
        this.image = image;
    }


    /*----Getter/Setters----*/

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ModelPdf{" +
                "uid='" + uid + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", url='" + url + '\'' +
                ", image='" + image + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", viewsCount=" + viewsCount +
                ", downloadsCount=" + downloadsCount +
                ", favorite=" + favorite +
                '}';
    }
}
