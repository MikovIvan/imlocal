package ru.imlocal.imlocal.ui;

import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateSource;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;

public class FragmentMap extends Fragment implements UserLocationObjectListener, CameraListener, InputListener {

    private boolean isSelected;
    private PlacemarkMapObject selected;

    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private MapObjectCollection mapObjects;

    private boolean followUserLocation = false;

    private Shop shop;
    private TextView tvDistance;
    private ImageView ivShopIcon;
    private TextView tvShopTitle;
    private TextView tvShopDescription;
    private TextView tvShopRating;
    private CardView cardView;
    private MapObjectTapListener mapObjectTapListener = new MapObjectTapListener() {
        @Override
        public boolean onMapObjectTap(MapObject mapObject, Point point) {
            cardView.setVisibility(View.VISIBLE);

            if (mapObject instanceof PlacemarkMapObject) {
                PlacemarkMapObject shopObject = (PlacemarkMapObject) mapObject;
                Object userData = shopObject.getUserData();
                if (isSelected) {
                    isSelected = false;
                    selected.setIconStyle(new IconStyle().setScale(0.5f));
                }
                isSelected = true;
                selected = shopObject;
                shopObject.setIconStyle(new IconStyle().setScale(1.5f).setAnchor(new PointF(0.5f, 0.52f))
                        .setRotationType(RotationType.ROTATE)
                        .setZIndex(0f));
                if (userData instanceof Shop) {
                    shop = (Shop) userData;
                    tvShopTitle.setText(shop.getShopShortName());
                    Picasso.with(getActivity()).load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + shop.getShopPhotoArray().get(0).getShopPhoto())
                            .into(ivShopIcon);
                    tvShopDescription.setText(shop.getShopShortDescription());
                    tvShopRating.setText(String.valueOf(shop.getShopAvgRating()));

//                    Log.d("DIS", latitude + " " + longitude);
//                    Log.d("DIS", String.valueOf(shopObject.getGeometry().getLatitude() +" "+ shopObject.getGeometry().getLongitude()));
//                    Log.d("DIS", String.valueOf(Geo.distance(shopObject.getGeometry(), new Point(latitude, longitude))));

                    tvDistance.setText(Utils.getDistance(shopObject, latitude, longitude));
                }
            }
            return true;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.toolbar_transparent));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        cardView = view.findViewById(R.id.cardview_map);
        tvDistance = view.findViewById(R.id.tv_distance_map);
        ivShopIcon = view.findViewById(R.id.iv_shopimage);
        tvShopTitle = view.findViewById(R.id.tv_title);
        tvShopDescription = view.findViewById(R.id.tv_description);
        tvShopRating = view.findViewById(R.id.tv_rating);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
            }
        });

        mapView = view.findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().addInputListener(this);

//        mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), 14, 0, 0));
//        для теста
        mapView.getMap().move(new CameraPosition(new Point(55.7739, 37.4719), 14, 0, 0));
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().addCameraListener(this);

//        MapKit mapKit = MapKitFactory.getInstance();
//        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
//        userLocationLayer.setVisible(true);
//        userLocationLayer.setHeadingEnabled(true);
//        userLocationLayer.setObjectListener(this);

        createMapObjects();
        return view;
    }

    private void createMapObjects() {
        for (Shop shop : shopList) {
            PlacemarkMapObject placemarkMapObject = mapObjects.addPlacemark(new Point(shop.getShopAddress().getLatitude(), shop.getShopAddress().getLongitude()),
                    ImageProvider.fromResource(getActivity(), R.drawable.untitled));
            placemarkMapObject.setIconStyle(new IconStyle().setScale(0.5f).setAnchor(new PointF(0.5f, 0.5f)));
            placemarkMapObject.setUserData(shop);
            placemarkMapObject.addTapListener(mapObjectTapListener);
        }
    }

    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView) {
        userLocationLayer.setAnchor(
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
                new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));
        followUserLocation = false;
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

    @Override
    public void onCameraPositionChanged(@NonNull Map map, @NonNull CameraPosition cameraPosition, @NonNull CameraUpdateSource cameraUpdateSource, boolean finish) {
//        rvPlaces.setVisibility(View.INVISIBLE);
        cardView.setVisibility(View.INVISIBLE);
//        if (finish) {
//            if (followUserLocation) {
//                userLocationLayer.setAnchor(
//                        new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.5)),
//                        new PointF((float) (mapView.getWidth() * 0.5), (float) (mapView.getHeight() * 0.83)));
//                followUserLocation = false;
//            } else {
//                if (!followUserLocation) {
//                    userLocationLayer.resetAnchor();
//                }
//            }
//        }
    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if (selected != null) {
            isSelected = false;
            selected.setIconStyle(new IconStyle().setScale(0.5f));
        }
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }
}
