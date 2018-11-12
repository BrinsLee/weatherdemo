package com.brins.weatherdemo.MyAdapter;

import android.graphics.Shader;

public class DataBean {


    private int imageId;
    private String title;
    private String data;

    public DataBean(int imageId, String title, String data) {
        this.imageId = imageId;
        this.title = title;
        this.data = data;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
