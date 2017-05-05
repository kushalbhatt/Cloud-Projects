package com.example.KUSHAL.myapplication.backend;

/**
 * Created by KUSHAL on 17-Apr-17.
 */

public class MessageData {
    private String message;
    private String latitude;
    private String longitude;
    private String hotness;
    private String stuff;

    public String getHotness() {
        return hotness;
    }

    public void setHotness(String hotness) {
        this.hotness = hotness;
    }

    public String getStuff() {
        return stuff;
    }

    public void setStuff(String stuff) {
        this.stuff = stuff;
    }

    public String getMessage() {
        return message;
    }

    public void setPayload(String payload) {
        this.message = payload;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
