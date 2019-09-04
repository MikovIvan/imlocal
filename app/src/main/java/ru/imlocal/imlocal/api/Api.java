package ru.imlocal.imlocal.api;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;

public interface Api {

    @GET("shops")
    Observable<List<Shop>> getAllShops();

    @GET("events")
    Observable<List<Action>> getAllActions();

    @GET("happenings")
    Observable<List<Event>> getAllEvents();

    @GET("shops/{shopId}")
    Observable<Shop> getShop(@Path("shopId") int shopId);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("user/register")
    Call<User> registerUser(@Body User user);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("user/login")
    Call<User> loginUser(@Body User user);

}
