package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Action implements Serializable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("active")
    @Expose
    private String active;
    @SerializedName("isEventTop")
    @Expose
    private String isActionTop;
    @SerializedName("eventOwnerId")
    @Expose
    private int actionOwnerId;
    @SerializedName("eventTypeId")
    @Expose
    private int actionTypeId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("shortDesc")
    @Expose
    private String shortDesc;
    @SerializedName("fullDesc")
    @Expose
    private String fullDesc;
    @SerializedName("begin")
    @Expose
    private String begin;
    @SerializedName("end")
    @Expose
    private String end;
    @SerializedName("creatorId")
    @Expose
    private int creatorId;

    @SerializedName("eventPhotos")
    @Expose
    private List<ActionPhoto> actionPhotos;

    public Action(String id, String active, String isActionTop, int actionOwnerId, int actionTypeId, String title, String shortDesc, String fullDesc, String begin, String end, int creatorId, List<ActionPhoto> actionPhotos) {
        this.id = id;
        this.active = active;
        this.isActionTop = isActionTop;
        this.actionOwnerId = actionOwnerId;
        this.actionTypeId = actionTypeId;
        this.title = title;
        this.shortDesc = shortDesc;
        this.fullDesc = fullDesc;
        this.begin = begin;
        this.end = end;
        this.creatorId = creatorId;
        this.actionPhotos = actionPhotos;
    }

    public Action() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getIsActionTop() {
        return isActionTop;
    }

    public void setIsActionTop(String isActionTop) {
        this.isActionTop = isActionTop;
    }

    public int getActionOwnerId() {
        return actionOwnerId;
    }

    public void setActionOwnerId(int actionOwnerId) {
        this.actionOwnerId = actionOwnerId;
    }

    public int getActionTypeId() {
        return actionTypeId;
    }

    public void setActionTypeId(int actionTypeId) {
        this.actionTypeId = actionTypeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getFullDesc() {
        return fullDesc;
    }

    public void setFullDesc(String fullDesc) {
        this.fullDesc = fullDesc;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public List<ActionPhoto> getActionPhotos() {
        return actionPhotos;
    }

    public void setActionPhotos(List<ActionPhoto> actionPhotos) {
        this.actionPhotos = actionPhotos;
    }
}
