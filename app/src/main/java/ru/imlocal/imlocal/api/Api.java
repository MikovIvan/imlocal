package ru.imlocal.imlocal.api;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.entity.User;

public interface Api {

    @GET("shops")
    Call<List<Shop>> getShops(@Query("page") int page);

    @GET("shop")
    Call<List<Shop>> getAllShops(@Query("userPoint") String point, @Query("range") int range);

    @GET("events")
    Observable<List<Action>> getAllActions();

    @GET("happenings")
    Observable<List<Event>> getAllEvents();

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("user/register")
    Call<User> registerUser(@Body User user);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("user/login")
    Call<User> loginUser(@Body User user);

    @FormUrlEncoded
    @POST("user/favorite")
    Call<User> addFavorites(
            @Field("kind") String kind,
            @Field("source_id") String sourceId,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("user/favorite")
    Call<RequestBody> removeFavorites(
            @Field("kind") String kind,
            @Field("source_id") String sourceId,
            @Field("user_id") String userId,
            @Field("delete") String delete
    );

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("users/{id}")
    Call<User> updateUser(@Header("Authorization") String credentials, @Path("id") String id, @Body User user);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("events")
    Call<Action> createAction(@Header("Authorization") String credentials, @Body Action action);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("shopaddresses")
    Call<ShopAddress> createShopAddress(@Header("Authorization") String credentials, @Body ShopAddress shopAddress);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("shopaddresses/{id}")
    Call<ShopAddress> updateShopAddress(@Header("Authorization") String credentials, @Body ShopAddress shopAddress, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("shops")
    Call<Shop> createShop(@Header("Authorization") String credentials, @Body Shop shop);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("happenings")
    Call<Event> createEvent(@Header("Authorization") String credentials, @Body Event event);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("happenings/{id}")
    Call<Event> updateEvent(@Header("Authorization") String credentials, @Body Event event, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("events/{id}")
    Call<Action> updateAction(@Header("Authorization") String credentials, @Body Action action, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("events/{id}")
    Call<Shop> updateShop(@Header("Authorization") String credentials, @Body Shop shop, @Path("id") int id);
}
