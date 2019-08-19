package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EventPhoto implements Serializable {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("happeningId")
    @Expose
    private int eventId;

    @SerializedName("happeningPhoto")
    @Expose
    private String eventPhoto;

    public EventPhoto(int id, int eventId, String eventPhoto) {
        this.id = id;
        this.eventId = eventId;
        this.eventPhoto = eventPhoto;
    }

    public EventPhoto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventPhoto() {
        return eventPhoto;
    }

    public void setEventPhoto(String eventPhoto) {
        this.eventPhoto = eventPhoto;
    }
}
