package ru.imlocal.imlocal.api;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;

public interface Api {

    @GET("shops")
    Observable<List<Shop>> getAllShops();

    @GET("events")
    Observable<List<Action>> getAllActions();

    @GET("happenings")
    Observable<List<Event>> getAllEvents();

    @GET("shops/{shopId}")
    Observable<Shop> getShop(@Path("shopId") int shopId);

}
