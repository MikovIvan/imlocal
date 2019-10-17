package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("shopId")
    @Expose
    private int shopId;
    @SerializedName("creatorId")
    @Expose
    private int creatorId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("price")
    @Expose
    private int price;
    @SerializedName("begin")
    @Expose
    private String begin;
    @SerializedName("end")
    @Expose
    private String end;
    @SerializedName("createdOn")
    @Expose
    private String createdOn;
    @SerializedName("updatedOn")
    @Expose
    private String updatedOn;
    @SerializedName("happeningTypeId")
    @Expose
    private int eventTypeId;
    @SerializedName("happeningPhotos")
    @Expose
    private List<EventPhoto> eventPhotoList;

    public Event(int shopId, int creatorId, String title, String description, String address, int price, String begin, int eventTypeId) {
        this.shopId = shopId;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.address = address;
        this.price = price;
        this.begin = begin;
        this.eventTypeId = eventTypeId;
    }

    public Event(int id, int shopId, int creatorId, String title, String description, String address, int price, String begin, String createdOn, String updatedOn, int eventTypeId, List<EventPhoto> eventPhotoList) {
        this.id = id;
        this.shopId = shopId;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.address = address;
        this.price = price;
        this.begin = begin;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.eventTypeId = eventTypeId;
        this.eventPhotoList = eventPhotoList;
    }

    public Event() {
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public int getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(int eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public List<EventPhoto> getEventPhotoList() {
        return eventPhotoList;
    }

    public void setEventPhotoList(List<EventPhoto> eventPhotoList) {
        this.eventPhotoList = eventPhotoList;
    }
}
