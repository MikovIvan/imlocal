package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ShopPhoto implements Serializable {

    @SerializedName("shopId")
    @Expose
    private int shopId;

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("shopPhoto")
    @Expose
    private String shopPhoto;

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShopPhoto() {
        return shopPhoto;
    }

    public void setShopPhoto(String shopPhoto) {
        this.shopPhoto = shopPhoto;
    }
}
