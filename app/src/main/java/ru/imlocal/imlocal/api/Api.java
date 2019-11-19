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
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.EventPhoto;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.entity.ShopPhoto;
import ru.imlocal.imlocal.entity.ShopRating;
import ru.imlocal.imlocal.entity.User;

public interface Api {

    @GET("shop")
    Call<List<Shop>> getAllShops(@Query("userPoint") String point, @Query("range") int range, @Query("page") int page, @Query("per-page") int perPage);

    @GET("shops/{id}")
    Call<Shop> getShop(@Path("id") String id);

    @GET("events")
    Call<List<Action>> getAllActions(@Query("page") int page, @Query("per-page") int perPage);

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
            @Header("Authorization") String credentials,
            @Field("kind") String kind,
            @Field("source_id") String sourceId,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("user/favorite")
    Call<RequestBody> removeFavorites(
            @Header("Authorization") String credentials,
            @Field("kind") String kind,
            @Field("source_id") String sourceId,
            @Field("user_id") String userId,
            @Field("delete") String delete
    );

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("users/{id}")
    Call<User> updateUser(@Header("Authorization") String credentials, @Path("id") String id, @Body User user);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("shopaddresses")
    Call<ShopAddress> createShopAddress(@Header("Authorization") String credentials, @Body ShopAddress shopAddress);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PATCH("shopaddresses/{id}")
    Call<ShopAddress> updateShopAddress(@Header("Authorization") String credentials, @Body ShopAddress shopAddress, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("shopaddresses/{id}")
    Call<ShopAddress> deleteShopAddress(@Header("Authorization") String credentials, @Path("id") String id);

    @Multipart
    @POST("shops")
    Call<Shop> createShop(@Header("Authorization") String credentials,
                          @Part("creatorId") RequestBody creatorId,
                          @Part("shopShortName") RequestBody shopShortName,
                          @Part("shopTypeId") RequestBody shopTypeId,
                          @Part("shopPhone") RequestBody shopPhone,
                          @Part("shopWeb") RequestBody shopWeb,
                          @Part("shopAddressId") RequestBody shopAddressId,
                          @Part("shopCostMin") RequestBody shopCostMin,
                          @Part("shopCostMax") RequestBody shopCostMax,
                          @Part("shopWorkTime") RequestBody shopWorkTime,
                          @Part("shopShortDescription") RequestBody shopShortDescription,
                          @Part("shopFullDescription") RequestBody shopFullDescription,
                          @Part MultipartBody.Part[] file,
                          @Part MultipartBody.Part pdf
    );

    @Multipart
    @PATCH("shops/{id}")
    Call<Shop> updateShop(@Header("Authorization") String credentials,
                          @Part("creatorId") RequestBody creatorId,
                          @Part("shopShortName") RequestBody shopShortName,
                          @Part("shopTypeId") RequestBody shopTypeId,
                          @Part("shopPhone") RequestBody shopPhone,
                          @Part("shopWeb") RequestBody shopWeb,
                          @Part("shopAddressId") RequestBody shopAddressId,
                          @Part("shopCostMin") RequestBody shopCostMin,
                          @Part("shopCostMax") RequestBody shopCostMax,
                          @Part("shopWorkTime") RequestBody shopWorkTime,
                          @Part("shopShortDescription") RequestBody shopShortDescription,
                          @Part("shopFullDescription") RequestBody shopFullDescription,
                          @Part MultipartBody.Part[] file,
                          @Path("id") String id,
                          @Part MultipartBody.Part pdf
    );

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
                            @Part("shopId") RequestBody shopId,
                            @Part("happeningTypeId") RequestBody happeningTypeId,
                            @Part MultipartBody.Part file
    );

    @Multipart
    @PATCH("happenings/{id}")
    Call<Event> updateEvent(@Header("Authorization") String credentials,
                            @Part("creatorId") RequestBody creatorId,
                            @Part("title") RequestBody title,
                            @Part("description") RequestBody description,
                            @Part("address") RequestBody address,
                            @Part("price") RequestBody price,
                            @Part("begin") RequestBody begin,
                            @Part("end") RequestBody end,
                            @Part("shopId") RequestBody shopId,
                            @Part("happeningTypeId") RequestBody happeningTypeId,
                            @Part MultipartBody.Part file,
                            @Path("id") String id
    );

    @Multipart
    @POST("events")
    Call<Action> createAction(@Header("Authorization") String credentials,
                              @Part("eventOwnerId") RequestBody actionOwnerId,
                              @Part("eventTypeId") RequestBody actionTypeId,
                              @Part("title") RequestBody title,
                              @Part("fullDesc") RequestBody fullDesc,
                              @Part("begin") RequestBody begin,
                              @Part("end") RequestBody end,
                              @Part("creatorId") RequestBody creatorId,
                              @Part MultipartBody.Part[] file
    );

    @Multipart
    @PATCH("events/{id}")
    Call<Action> updateAction(@Header("Authorization") String credentials,
                              @Part("eventOwnerId") RequestBody actionOwnerId,
                              @Part("eventTypeId") RequestBody actionTypeId,
                              @Part("title") RequestBody title,
                              @Part("fullDesc") RequestBody fullDesc,
                              @Part("begin") RequestBody begin,
                              @Part("end") RequestBody end,
                              @Part("creatorId") RequestBody creatorId,
                              @Part MultipartBody.Part[] file,
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
    Call<Boolean> deleteShop(@Header("Authorization") String credentials, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("happenings/{id}")
    Call<Boolean> deleteEvent(@Header("Authorization") String credentials, @Path("id") int id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("events/{id}")
    Call<Boolean> deleteAction(@Header("Authorization") String credentials, @Path("id") String id);

    @FormUrlEncoded
    @POST("shop-rating/create")
    Call<ShopRating> addRating(@Header("Authorization") String credentials,
                               @Field("userId") int userId,
                               @Field("shopId") int shopId,
                               @Field("rating") int rating
    );

    @FormUrlEncoded
    @PUT("shop-ratings/{userId},{shopId}")
    Call<ShopRating> updateRating(@Header("Authorization") String credentials,
                                  @Path("userId") int userId,
                                  @Path("shopId") int shopId,
                                  @Field("rating") String rating
    );


    @GET("shop-ratings/{userId},{shopId}")
    Call<ShopRating> getRating(@Header("Authorization") String credentials,
                               @Path("userId") int userId,
                               @Path("shopId") int shopId
    );

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("happeningphotos/{id}")
    Call<EventPhoto> deleteEventPhoto(@Header("Authorization") String credentials, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("eventphotos/{id}")
    Call<ActionPhoto> deleteActionPhoto(@Header("Authorization") String credentials, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("shopphotos/{id}")
    Call<ShopPhoto> deleteShopPhoto(@Header("Authorization") String credentials, @Path("id") String id);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("users/{id}")
    Call<User> getCreated(@Header("Authorization") String credentials, @Path("id") String id, @Query("expand") String expand);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("users/{id}")
    Observable<User> getCreatedRX(@Header("Authorization") String credentials, @Path("id") String id, @Query("expand") String expand);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("users/{id}")
    Call<User> getFavorites(@Header("Authorization") String credentials, @Path("id") String id, @Query("expand") String expand);

}
