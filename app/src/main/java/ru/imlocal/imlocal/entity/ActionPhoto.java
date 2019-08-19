package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ActionPhoto implements Serializable {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("eventId")
    @Expose
    private int eventId;

    @SerializedName("eventPhoto")
    @Expose
    private String actionPhoto;

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

    public String getActionPhoto() {
        return actionPhoto;
    }

    public void setActionPhoto(String actionPhoto) {
        this.actionPhoto = actionPhoto;
    }
}
