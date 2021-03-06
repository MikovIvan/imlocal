package ru.imlocal.imlocal;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.vk.sdk.VKSdk;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.search.SearchFactory;
import com.yayandroid.locationmanager.base.LocationBaseActivity;
import com.yayandroid.locationmanager.configuration.Configurations;
import com.yayandroid.locationmanager.configuration.LocationConfiguration;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.api.Api;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.network.RetrofitClient;
import ru.imlocal.imlocal.ui.FragmentAddAction;
import ru.imlocal.imlocal.ui.FragmentAddEvent;
import ru.imlocal.imlocal.ui.FragmentAddShop;
import ru.imlocal.imlocal.ui.FragmentBusiness;
import ru.imlocal.imlocal.ui.FragmentFavorites;
import ru.imlocal.imlocal.ui.FragmentFeedback;
import ru.imlocal.imlocal.ui.FragmentLogin;
import ru.imlocal.imlocal.ui.FragmentMap;
import ru.imlocal.imlocal.ui.FragmentPolicy;
import ru.imlocal.imlocal.ui.FragmentProfile;
import ru.imlocal.imlocal.ui.FragmentViewPager;
import ru.imlocal.imlocal.ui.FragmentVitrinaAction;
import ru.imlocal.imlocal.ui.FragmentVitrinaEvent;
import ru.imlocal.imlocal.ui.FragmentVitrinaShop;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.utils.Constants.MAPKIT_API_KEY;
import static ru.imlocal.imlocal.utils.Utils.actionMap;
import static ru.imlocal.imlocal.utils.Utils.eventMap;
import static ru.imlocal.imlocal.utils.Utils.setSnackbarOnClickListener;
import static ru.imlocal.imlocal.utils.Utils.shopMap;

public class MainActivity extends LocationBaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 23;
    public static double latitude;
    public static double longitude;
    private long backPressedTime;
    private Toast backToast;
    private ProgressDialog progressDialog;

    public static Map<String, Shop> favoritesShops = new HashMap<>();
    public static Map<String, Event> favoritesEvents = new HashMap<>();
    public static Map<String, Action> favoritesActions = new HashMap<>();

    public static User user = new User();
    public static AccessToken accessToken;

    public static Api api;
    public static AppBarLayout appBarLayout;
    public static NavigationView navigationView;
    public static MenuItem enter;
    public static GoogleSignInClient mGoogleSignInClient;
    public static CallbackManager callbackManager;
    private GoogleSignInAccount account;

    private boolean mToolBarNavigationListenerIsRegistered = false;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private AccessTokenTracker accessTokenTracker;
    private String currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initYandexMaps();
        setContentView(R.layout.activity_main);

        getLocation();

        api = RetrofitClient.getInstance(this).getApi();

        initToolbar();
        initNavigationDrawer();

        enableUpButtonViews(false);

        configFbAuth();
        configGoogleAuth();
        configAccessTokenTrakerFB();

        TextView footer_policy_link = findViewById(R.id.footer_policy_link);
        footer_policy_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
                openPolicy();
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
//            openViewPager();
            currentFragment = "FragmentViewPager";
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                List<Fragment> f = getSupportFragmentManager().getFragments();
                Fragment frag = f.get(1);
                currentFragment = frag.getClass().getSimpleName();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getLocationManager().isWaitingForLocation()
                && !getLocationManager().isAnyDialogShowing()) {
            displayProgress();
        }
    }

    public void openPolicy() {
        Fragment fragment = new FragmentPolicy();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment).setCustomAnimations(R.anim.enter_act, R.anim.exit_act).addToBackStack("FragmentPolicy").commit();
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != null) {
            switch (currentFragment) {
                case "FragmentBusiness":
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("FragmentBusiness");
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                    openViewPager();
                    return;
                case "FragmentViewPager":
                    Fragment fragmentViewPager = getSupportFragmentManager().findFragmentByTag("FragmentViewPager");
                    if (fragmentViewPager != null && fragmentViewPager.isVisible()) {
                        if (backPressedTime + 2000 > System.currentTimeMillis()) {
                            backToast.cancel();
                            super.onBackPressed();
                            return;
                        } else {
                            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                            backToast.show();
                        }
                        backPressedTime = System.currentTimeMillis();
                    }
                    return;
                default:
                    getSupportFragmentManager().popBackStack();
            }
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    public void openFeedback() {
        Fragment fragment = new FragmentFeedback();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentFeedback")
                .commit();
    }

    public void openProfile() {
        Fragment fragment = new FragmentProfile();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentProfile")
                .commit();
    }

    public void closeLogin() {
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(getSupportFragmentManager().findFragmentByTag("FragmentLogin"))
                .commitAllowingStateLoss();
    }

    public void openViewPager() {
        Fragment fragment = new FragmentViewPager();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .add(R.id.frame, fragment, "FragmentViewPager")
                .commitAllowingStateLoss();
    }

    public void openMap() {
        Fragment fragment = new FragmentMap();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentMap")
                .commit();
    }

    public void openVitrinaShop(Bundle bundle) {
        Fragment fragment = new FragmentVitrinaShop();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment)
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .addToBackStack("FragmentVitrinaShop")
                .commit();
    }

    public void openVitrinaEvent(Bundle bundle) {
        Fragment fragment = new FragmentVitrinaEvent();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment)
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .addToBackStack("FragmentVitrinaEvent")
                .commit();
    }

    public void openVitrinaAction(Bundle bundle) {
        Fragment fragment = new FragmentVitrinaAction();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment)
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .addToBackStack("FragmentVitrinaAction")
                .commit();
    }

    public void openLogin() {
        Fragment fragment = new FragmentLogin();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame_auth, fragment, "FragmentLogin")
                .addToBackStack("FragmentLogin")
                .commit();
    }

    public void openBusiness() {
        Fragment fragment = new FragmentBusiness();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame, fragment, "FragmentBusiness")
                .addToBackStack("FragmentBusiness")
                .commit();
    }

    public void openAddAction(Bundle bundle) {
        Fragment fragment = new FragmentAddAction();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentAddAction")
                .commit();
    }

    public void openAddEvent(Bundle bundle) {
        Fragment fragment = new FragmentAddEvent();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentAddEvent")
                .commit();
    }

    public void openAddShop(Bundle bundle) {
        Fragment fragment = new FragmentAddShop();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentAddShop")
                .commit();
    }

    public void openFavorites() {
        Fragment fragment = new FragmentFavorites();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment)
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .addToBackStack("FragmentFavorites")
                .commit();
    }


    private void refreshFragmentViewPager() {
        Fragment frg = null;
        frg = getSupportFragmentManager().findFragmentByTag("FragmentViewPager");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.detach(frg);
        ft.attach(frg);
        ft.commit();
    }


    public void enableUpButtonViews(boolean enable) {
        // To keep states of Ac
        // tionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if (enable) {
            //You may not want to open the drawer on swipe from the left in this case
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            // Remove hamburger
            toggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if (!mToolBarNavigationListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });
                mToolBarNavigationListenerIsRegistered = true;
            }
        } else {
            //You must regain the power of swipe for the drawer.
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            toggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            toggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }
    }

    @Override
    protected void onStart() {
        MapKitFactory.getInstance().onStart();
        accessTokenTracker.startTracking();
        accessToken = AccessToken.getCurrentAccessToken();
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (VKSdk.isLoggedIn() || account != null || (accessToken != null && !accessToken.isExpired())) {
            user = PreferenceUtils.getUser(MainActivity.this);
            getFavorites();
            enter.setTitle(user.getUsername());
            setFavoritesAndLogoutButtonsInNavigationDrawer(true);
        }
        if (!VKSdk.isLoggedIn() && account == null && accessToken == null) {
            setFavoritesAndLogoutButtonsInNavigationDrawer(false);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_login:
                if (!user.isLogin()) {
                    openLogin();
                    return false;
                } else {
                    openProfile();
                }
                break;
            case R.id.nav_help:
                openFeedback();
                break;
            case R.id.nav_for_business:
                if (user.isLogin()) {
                    openBusiness();
                } else {
                    Snackbar.make(getWindow().getDecorView().findViewById(R.id.drawer_layout), getResources().getString(R.string.need_login), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.login), setSnackbarOnClickListener(this)).show();
                }
                break;
            case R.id.nav_favorites:
                openFavorites();
                return false;
            case R.id.nav_logout:
                if (VKSdk.isLoggedIn()) {
                    VKSdk.logout();
                    Toast.makeText(this, "VK logout", Toast.LENGTH_SHORT).show();
                }
                if (account != null) {
                    mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "Google logout", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (accessToken != null && !accessToken.isExpired()) {
                    LoginManager.getInstance().logOut();
                    Toast.makeText(this, "FB logout", Toast.LENGTH_SHORT).show();
                }
                user = new User();
                favoritesActions.clear();
                favoritesEvents.clear();
                favoritesShops.clear();
                user.setLogin(false);
                refreshFragmentViewPager();
                PreferenceUtils.saveUser(user, this);
                enter.setTitle("Вход");
                setFavoritesAndLogoutButtonsInNavigationDrawer(false);
                break;
        }
        return true;
    }

    private void setFavoritesAndLogoutButtonsInNavigationDrawer(boolean b) {
        navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(b);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(b);
    }

    private void configGoogleAuth() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!user.isLogin()) {
            closeLogin();
        }
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void configFbAuth() {
        callbackManager = CallbackManager.Factory.create();
    }

    private void initNavigationDrawer() {
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        enter = menu.findItem(R.id.nav_login);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initYandexMaps() {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        SearchFactory.initialize(this);
    }

    private void configAccessTokenTrakerFB() {
        accessTokenTracker = new AccessTokenTracker() {
            // This method is invoked everytime access token changes
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (oldAccessToken == currentAccessToken) {
                    enter.setTitle(user.getUsername());
//                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
//                        @Override
//                        public void onCompleted(JSONObject object, GraphResponse response) {
//                            try {
//                                String firstName = object.getString("first_name");
//                                String lastName = object.getString("last_name");
//                                String email = object.getString("email");
//                                String id = object.getString("id");
//                                Toast.makeText(MainActivity.this, "успешно facebook", Toast.LENGTH_LONG).show();
//                                saveUser(id, email, firstName, lastName, "facebook", accessToken.getToken(), MainActivity.this);
//                                addFavoritesAndLogoutButtonsToNavigationDrawer();
//                                Log.d("TAG", user.toString());
//                                enter.setTitle(firstName + " " + lastName);
//                                Log.d("TAG", firstName + " " + lastName + " " + email + " " + id);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//
//                    Bundle parameters = new Bundle();
//                    parameters.putString("fields", "first_name,last_name,email,id");
//                    request.setParameters(parameters);
//                    request.executeAsync();
                } else {
                    enter.setTitle("Вход");
                    user.setLogin(false);
                    navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                }
            }
        };
        accessToken = AccessToken.getCurrentAccessToken();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(null);
        }
        setSupportActionBar(toolbar);
        appBarLayout.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void getFavorites() {
        Call<User> call1 = api.getFavorites(Credentials.basic(user.getAccessToken(), ""), user.getId(), "shopsFavorites,eventsFavorites,happeningsFavorites");
        call1.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    favoritesShops.putAll(shopMap(response.body().getShopsFavoritesList()));
                    favoritesEvents.putAll(eventMap(response.body().getEventsFavoritesList()));
                    favoritesActions.putAll(actionMap(response.body().getActionsFavoritesList()));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
//                        openViewPager();
                    }
                } else {
                    Toast.makeText(this, "Без этого разрешения ничего работать не будет", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        dismissProgress();
        latitude = location.getLatitude();
        longitude = location.getLongitude();

    }

    @Override
    public void onLocationFailed(int type) {
        dismissProgress();
        switch (type) {
            case FailType.TIMEOUT: {
                Toast.makeText(this, "Couldn't get location, and timeout!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.PERMISSION_DENIED: {
                Toast.makeText(this, "Couldn't get location, because user didn't give permission!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.NETWORK_NOT_AVAILABLE: {
                Toast.makeText(this, "Couldn't get location, because network is not accessible!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE: {
                Toast.makeText(this, "Couldn't get location, because Google Play Services not available!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_CONNECTION_FAIL: {
                Toast.makeText(this, "Couldn't get location, because Google Play Services connection failed!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG: {
                Toast.makeText(this, "Couldn't display settingsApi dialog!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED: {
                Toast.makeText(this, "Couldn't get location, because user didn't activate providers via settingsApi!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.VIEW_DETACHED: {
                Toast.makeText(this, "Couldn't get location, because in the process view was detached!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.VIEW_NOT_REQUIRED_TYPE: {
                Toast.makeText(this, "Couldn't get location, "
                        + "because view wasn't sufficient enough to fulfill given configuration!", Toast.LENGTH_LONG).show();
                break;
            }
            case FailType.UNKNOWN: {
                Toast.makeText(this, "Ops! Something went wrong!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return Configurations.defaultConfiguration("Необходимо разрешение для определения местоположения!", "Не могли бы вы включить GPS?");
    }

    private void displayProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Getting location...");
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void updateProgress(String text) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(text);
        }
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        switch (processType) {
            case ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES: {
                updateProgress("Getting Location from Google Play Services...");
                if (latitude != 0.0 && longitude != 0.0) {
                    openViewPager();
                    currentFragment = "FragmentViewPager";
                }
                break;
            }
            case ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER: {
                updateProgress("Getting Location from GPS...");
                break;
            }
            case ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER: {
                updateProgress("Getting Location from Network...");
                break;
            }
            case ProcessType.ASKING_PERMISSIONS:
            case ProcessType.GETTING_LOCATION_FROM_CUSTOM_PROVIDER:
                // Ignored
                break;
        }
    }
}
