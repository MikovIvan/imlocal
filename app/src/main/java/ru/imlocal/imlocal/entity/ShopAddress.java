package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ShopAddress implements Serializable {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("street")
    @Expose
    private String street;
    @SerializedName("houseNumber")
    @Expose
    private String houseNumber;
    @SerializedName("housing")
    @Expose
    private int housing;
    @SerializedName("building")
    @Expose
    private int building;
    @SerializedName("latitude")
    @Expose
    private float latitude;
    @SerializedName("longitude")
    @Expose
    private float longitude;

    public ShopAddress() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public int getHousing() {
        return housing;
    }

    public void setHousing(int housing) {
        this.housing = housing;
    }

    public int getBuilding() {
        return building;
    }

    public void setBuilding(int building) {
        this.building = building;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "г. " + city + ", " + street + ", " + houseNumber;
    }
}
