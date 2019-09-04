package ru.imlocal.imlocal.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("source_id")
    @Expose
    private String source_id;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("accessToken")
    @Expose
    private String accessToken;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("eventsFavorites")
    @Expose
    private List<Action> actionsFavoritesList;
    @SerializedName("shopsFavorites")
    @Expose
    private List<Shop> shopsFavoritesList;
    @SerializedName("happeningsFavorites")
    @Expose
    private List<Event> eventsFavoritesList;

    private boolean isLogin;

    public User() {
    }

    public User(String source_id, String email, String firstName, String lastName) {
        this.source_id = source_id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String source_id, String email, String username, String accessToken, String source) {
        this.source_id = source_id;
        this.source = source;
        this.email = email;
        this.accessToken = accessToken;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Action> getActionsFavoritesList() {
        return actionsFavoritesList;
    }

    public void setActionsFavoritesList(List<Action> actionsFavoritesList) {
        this.actionsFavoritesList = actionsFavoritesList;
    }

    public List<Shop> getShopsFavoritesList() {
        return shopsFavoritesList;
    }

    public void setShopsFavoritesList(List<Shop> shopsFavoritesList) {
        this.shopsFavoritesList = shopsFavoritesList;
    }

    public List<Event> getEventsFavoritesList() {
        return eventsFavoritesList;
    }

    public void setEventsFavoritesList(List<Event> eventsFavoritesList) {
        this.eventsFavoritesList = eventsFavoritesList;
    }

    public String getSource_id() {
        return source_id;
    }

    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", source_id='" + source_id + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", source='" + source + '\'' +
                ", username='" + username + '\'' +
                ", actionsFavoritesList=" + actionsFavoritesList +
                ", shopsFavoritesList=" + shopsFavoritesList +
                ", eventsFavoritesList=" + eventsFavoritesList +
                ", isLogin=" + isLogin +
                '}';
    }
}
