package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Shop implements Serializable {

    @SerializedName("shopId")
    @Expose
    private int shopId;
    @SerializedName("creatorId")
    @Expose
    private String creatorId;
    @SerializedName("shopActive")
    @Expose
    private String shopActive;
    @SerializedName("shopShortName")
    @Expose
    private String shopShortName;
    @SerializedName("shopFullName")
    @Expose
    private String shopFullName;
    @SerializedName("shopTypeId")
    @Expose
    private int shopTypeId;
    @SerializedName("shopPhone")
    @Expose
    private String shopPhone;
    @SerializedName("shopWeb")
    @Expose
    private String shopWeb;
    @SerializedName("shopAddressId")
    @Expose
    private String shopAddressId;
    @SerializedName("shopCostMin")
    @Expose
    private String shopCostMin;
    @SerializedName("shopCostMax")
    @Expose
    private String shopCostMax;
    @SerializedName("shopMiddleCost")
    @Expose
    private String shopMiddleCost;
    @SerializedName("shopWorkTime")
    @Expose
    private String shopWorkTime;
    @SerializedName("shopAgregator")
    @Expose
    private String shopAgregator;
    @SerializedName("shopStatusId")
    @Expose
    private String shopStatusId;
    @SerializedName("shopShortDescription")
    @Expose
    private String shopShortDescription;
    @SerializedName("shopFullDescription")
    @Expose
    private String shopFullDescription;
    @SerializedName("shopRating")
    @Expose
    private int shopRating;
    @SerializedName("shopPhotos")
    @Expose
    private List<ShopPhoto> shopPhotoArray;
    @SerializedName("events")
    @Expose
    private List<Action> shopActionArray;
    @SerializedName("happenings")
    @Expose
    private List<Event> shopEventList;
    @SerializedName("shopAddress")
    @Expose
    private ShopAddress shopAddress;
    @SerializedName("shopAvgRating")
    @Expose
    private double shopAvgRating;
    @SerializedName("shopLinkPdf")
    @Expose
    private String shopLinkPdf;


    public Shop() {
    }

    public Shop(String creatorId, String shopShortName, int shopTypeId, String shopPhone, String shopWeb, String shopCostMin, String shopCostMax, String shopWorkTime, String shopShortDescription, String shopFullDescription, ShopAddress shopAddress) {
        this.creatorId = creatorId;
        this.shopShortName = shopShortName;
        this.shopTypeId = shopTypeId;
        this.shopPhone = shopPhone;
        this.shopWeb = shopWeb;
        this.shopCostMin = shopCostMin;
        this.shopCostMax = shopCostMax;
        this.shopWorkTime = shopWorkTime;
        this.shopShortDescription = shopShortDescription;
        this.shopFullDescription = shopFullDescription;
        this.shopAddress = shopAddress;
    }

    public String getShopLinkPdf() {
        return shopLinkPdf;
    }

    public void setShopLinkPdf(String shopLinkPdf) {
        this.shopLinkPdf = shopLinkPdf;
    }

    public List<Event> getShopEventList() {
        return shopEventList;
    }

    public void setShopEventList(List<Event> shopEventList) {
        this.shopEventList = shopEventList;
    }

    public ShopAddress getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(ShopAddress shopAddress) {
        this.shopAddress = shopAddress;
    }

    public double getShopAvgRating() {
        return shopAvgRating;
    }

    public void setShopAvgRating(double shopAvgRating) {
        this.shopAvgRating = shopAvgRating;
    }

    public List<Action> getShopActionArray() {
        return shopActionArray;
    }

    public void setShopActionArray(List<Action> shopActionArray) {
        this.shopActionArray = shopActionArray;
    }

    public List<ShopPhoto> getShopPhotoArray() {
        return shopPhotoArray;
    }

    public void setShopPhotoArray(List<ShopPhoto> shopPhotoArray) {
        this.shopPhotoArray = shopPhotoArray;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getShopActive() {
        return shopActive;
    }

    public void setShopActive(String shopActive) {
        this.shopActive = shopActive;
    }

    public String getShopShortName() {
        return shopShortName;
    }

    public void setShopShortName(String shopShortName) {
        this.shopShortName = shopShortName;
    }

    public String getShopFullName() {
        return shopFullName;
    }

    public void setShopFullName(String shopFullName) {
        this.shopFullName = shopFullName;
    }

    public int getShopTypeId() {
        return shopTypeId;
    }

    public void setShopTypeId(int shopTypeId) {
        this.shopTypeId = shopTypeId;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    public String getShopWeb() {
        return shopWeb;
    }

    public void setShopWeb(String shopWeb) {
        this.shopWeb = shopWeb;
    }

    public String getShopAddressId() {
        return shopAddressId;
    }

    public void setShopAddressId(String shopAddressId) {
        this.shopAddressId = shopAddressId;
    }

    public String getShopCostMin() {
        return shopCostMin;
    }

    public void setShopCostMin(String shopCostMin) {
        this.shopCostMin = shopCostMin;
    }

    public String getShopCostMax() {
        return shopCostMax;
    }

    public void setShopCostMax(String shopCostMax) {
        this.shopCostMax = shopCostMax;
    }

    public String getShopMiddleCost() {
        return shopMiddleCost;
    }

    public void setShopMiddleCost(String shopMiddleCost) {
        this.shopMiddleCost = shopMiddleCost;
    }

    public String getShopWorkTime() {
        return shopWorkTime;
    }

    public void setShopWorkTime(String shopWorkTime) {
        this.shopWorkTime = shopWorkTime;
    }

    public String getShopAgregator() {
        return shopAgregator;
    }

    public void setShopAgregator(String shopAgregator) {
        this.shopAgregator = shopAgregator;
    }

    public String getShopStatusId() {
        return shopStatusId;
    }

    public void setShopStatusId(String shopStatusId) {
        this.shopStatusId = shopStatusId;
    }

    public String getShopShortDescription() {
        return shopShortDescription;
    }

    public void setShopShortDescription(String shopShortDescription) {
        this.shopShortDescription = shopShortDescription;
    }

    public String getShopFullDescription() {
        return shopFullDescription;
    }

    public void setShopFullDescription(String shopFullDescription) {
        this.shopFullDescription = shopFullDescription;
    }

    public int getShopRating() {
        return shopRating;
    }

    public void setShopRating(int shopRating) {
        this.shopRating = shopRating;
    }

}
