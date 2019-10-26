package ru.imlocal.imlocal.api;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.EventPhoto;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.entity.User;

public interface Api {

    @GET("shops")
    Call<List<Shop>> getShops(@Query("page") int page);

    @GET("shop")
    Call<List<Shop>> getAllShops(@Query("userPoint") String point, @Query("range") int range, @Query("page") int page, @Query("per-page") int perPage);

    @GET("events")
    Call<List<Action>> getAllActions(@Query("page") int page, @Query("per-page") int perPage);

    @GET("events")
    Observable<List<Action>> getAllActions();

    @GET("happenings")
    Observable<List<Event>> getAllEvents();

    @GET("happenings")
    Call<List<Event>> getAllEvents(@Query("page") int page, @Query("per-page") int perPage);

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

    @Multipart
    @POST("events")
    Call<Action> createAction(@Header("Authorization") String credentials,
                              @Part("eventOwnerId") RequestBody actionOwnerId,
                              @Part("eventTypeId") RequestBody actionTypeId,
                              @Part("title") RequestBody title,
                              @Part("shortDesc") RequestBody shortDesc,
                              @Part("fullDesc") RequestBody fullDesc,
                              @Part("begin") RequestBody begin,
                              @Part("end") RequestBody end,
                              @Part("creatorId") RequestBody creatorId,
                              @Part MultipartBody.Part[] file
    );

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("shopaddresses")
    Call<ShopAddress> createShopAddress(@Header("Authorization") String credentials, @Body ShopAddress shopAddress);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("shopaddresses/{id}")
    Call<ShopAddress> updateShopAddress(@Header("Authorization") String credentials, @Body ShopAddress shopAddress, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("shopaddresses/{id}")
    Call<ShopAddress> deleteShopAddress(@Header("Authorization") String credentials, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("shops")
    Call<Shop> createShop(@Header("Authorization") String credentials, @Body Shop shop);

    @Multipart
    @POST("happenings")
    Call<Event> createEvent(@Header("Authorization") String credentials,
                            @Part("creatorId") RequestBody creatorId,
                            @Part("title") RequestBody title,
                            @Part("description") RequestBody description,
                            @Part("address") RequestBody address,
                            @Part("price") RequestBody price,
                            @Part("begin") RequestBody begin,
                            @Part("end") RequestBody end,
                            @Part("happeningTypeId") RequestBody happeningTypeId,
                            @Part MultipartBody.Part file
    );

    @Multipart
    @POST("happenings/{id}")
    Call<Event> updateEvent(@Header("Authorization") String credentials,
                            @Part("creatorId") RequestBody creatorId,
                            @Part("title") RequestBody title,
                            @Part("description") RequestBody description,
                            @Part("address") RequestBody address,
                            @Part("price") RequestBody price,
                            @Part("begin") RequestBody begin,
                            @Part("end") RequestBody end,
                            @Part("happeningTypeId") RequestBody happeningTypeId,
                            @Part MultipartBody.Part file,
                            @Path("id") String id
    );
    
    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("happenings/{id}")
    Call<Event> updateEvent(@Header("Authorization") String credentials, @Body Event event, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("events/{id}")
    Call<Action> updateAction(@Header("Authorization") String credentials, @Body Action action, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("shops/{id}")
    Call<Shop> updateShop(@Header("Authorization") String credentials, @Body Shop shop, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("shops/{id}")
    Call<Shop> deleteShop(@Header("Authorization") String credentials, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("happenings/{id}")
    Call<Event> deleteEvent(@Header("Authorization") String credentials, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("events/{id}")
    Call<Action> deleteAction(@Header("Authorization") String credentials, @Path("id") String id);

    @FormUrlEncoded
    @POST("shop-rating/create")
    Call<RequestBody> addRating(@Header("Authorization") String credentials,
                                @Field("userId") int userId,
                                @Field("shopId") int shopId,
                                @Field("rating") int rating
    );

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("happeningphotos/{id}")
    Call<EventPhoto> deleteEventPhoto(@Header("Authorization") String credentials, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("eventphotos/{id}")
    Call<ActionPhoto> deleteActionPhoto(@Header("Authorization") String credentials, @Path("id") String id);
}
