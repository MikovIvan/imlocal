package ru.imlocal.imlocal.ui;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Shop;

import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;

public class FragmentMap extends Fragment implements UserLocationObjectListener {

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        appBarLayout.setVisibility(View.INVISIBLE);
        mapView = view.findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
//        mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), 14, 0, 0));
//        для теста
        mapView.getMap().move(new CameraPosition(new Point(55.7739, 37.4719), 14, 0, 0));
        mapObjects = mapView.getMap().getMapObjects().addCollection();

//        MapKit mapKit = MapKitFactory.getInstance();
//        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
//        userLocationLayer.setVisible(true);
//        userLocationLayer.setHeadingEnabled(true);
//
//        userLocationLayer.setObjectListener(this);

        createMapObjects();
        return view;
    }

    private void createMapObjects() {
        for (Shop shop : shopList) {
            mapObjects.addPlacemark(new Point(shop.getShopAddress().getLatitude(), shop.getShopAddress().getLongitude()))
                    .setIcon(ImageProvider.fromResource(getActivity(), R.drawable.ic_place));
        }
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));

        CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

        pinIcon.setIcon(
                "pin",
                ImageProvider.fromResource(getActivity(), R.drawable.search_result),
                new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(1f)
                        .setScale(0.5f)
        );

//        userLocationView.getAccuracyCircle().setFillColor(Color.BLUE);
    }

    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView) {

    }

    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }
}
