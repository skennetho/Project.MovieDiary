package com.example.moviediary;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
/*
 * http://www.jsonschema2pojo.org/
 * 를 통해 json에서 모델을 뽑음
 * source type을 JSON, annotation style을 Gson으로 함
 * */


public class Item {
    //MovieDiaryDB(mTitle, mYear , diary, lastDate,mPoster)
    public Item(String title, String year, String diary, String diaryDate, String image) {
        this.title = title;
        this.pubDate = year;
        this.image = image;
        this.diary = diary;
        this.diaryDate = diaryDate;
    }

    public Item() {

    }

    private String diaryDate;

    public void setDiaryDate(String d) {
        this.diaryDate = d;
    }

    public String getDiaryDate() {
        return diaryDate;
    }

    private String diary;

    public void setDiary(String d) {
        this.diary = d;
    }

    public String getDiary() {
        return diary;
    }

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("link")
    @Expose
    private String link;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("subtitle")
    @Expose
    private String subtitle;

    @SerializedName("pubDate")
    @Expose
    private String pubDate;

    @SerializedName("director")
    @Expose
    private String director;

    @SerializedName("actor")
    @Expose
    private String actor;

    @SerializedName("userRating")
    @Expose
    private String userRating;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

}

