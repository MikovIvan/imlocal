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
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;

public interface Api {

    @GET("shops")
    Call<List<Shop>> getShops(@Query("page") int page);

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
    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") String id, @Body User user);

    @POST("shops")
    Call<Action> createAction(@Header("Authorization") String credential, @Body Action action);
}
