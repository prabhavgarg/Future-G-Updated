package com.gargprabhav.future_g;

import java.util.ArrayList;

public class GetSetListView{
    public String title, date, description, status, id;
    public int image;

    public GetSetListView(){
    }

    public GetSetListView(String title, String date, String description, String status, String id, int image) {
        this.image = image;
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getImage() {
        return image;
    }

    public String getDescription() { return description;}

    public String getStatus() {return status;}
}