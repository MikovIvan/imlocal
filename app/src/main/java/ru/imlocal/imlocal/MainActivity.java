package ru.imlocal.imlocal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.vk.sdk.VKSdk;
import com.yandex.mapkit.MapKitFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ru.imlocal.imlocal.api.Api;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.network.RetrofitClient;
import ru.imlocal.imlocal.ui.FragmentFeedback;
import ru.imlocal.imlocal.ui.FragmentLogin;
import ru.imlocal.imlocal.ui.FragmentMap;
import ru.imlocal.imlocal.ui.FragmentPolicy;
import ru.imlocal.imlocal.ui.FragmentViewPager;
import ru.imlocal.imlocal.ui.FragmentVitrinaAction;
import ru.imlocal.imlocal.ui.FragmentVitrinaEvent;
import ru.imlocal.imlocal.ui.FragmentVitrinaShop;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.ui.FragmentLogin.addFavoritesAndLogoutButtonsToNavigationDrawer;
import static ru.imlocal.imlocal.ui.FragmentLogin.saveUser;
import static ru.imlocal.imlocal.utils.Constants.MAPKIT_API_KEY;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static double latitude;
    public static double longitude;
    private long backPressedTime;
    private Toast backToast;
    private static boolean requestingLocationPermission;

    public static Map<String, Shop> favoritesShops = new HashMap<>();
    public static Map<String, Event> favoritesEvents = new HashMap<>();
    public static Map<String, Action> favoritesActions = new HashMap<>();

    public static boolean isLoading;
    public static User user = new User();
    public static AccessToken accessToken;

    public static Api api;
    public static AppBarLayout appBarLayout;
    public static ProgressBar progressBar;
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

    public static void showLoadingIndicator(boolean show) {
        isLoading = show;
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initYandexMaps();
        setContentView(R.layout.activity_main);

        api = RetrofitClient.getInstance().getApi();

        initView();
        initToolbar();
        initNavigationDrawer();

        enableUpButtonViews(false);
        showLoadingIndicator(false);

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

        requestingLocationPermission = PreferenceUtils.getRequestingLocationPermission(this);
        if (!requestingLocationPermission) {
            checkLocationPermission();
        } else if (savedInstanceState == null) {
            openViewPager();
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
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
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

    public void closeLogin() {
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .remove(getSupportFragmentManager().findFragmentById(R.id.frame_auth))
                .commit();
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
                .replace(R.id.frame_auth, fragment)
                .addToBackStack("FragmentLogin")
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
                }
                return false;
            case R.id.nav_help:
                openFeedback();
                break;
            case R.id.nav_for_business:
                Toast.makeText(this, "Раздел в разработке", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_favorites:
                Toast.makeText(this, "избранное", Toast.LENGTH_SHORT).show();
                break;
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
        closeLogin();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void configFbAuth() {
        callbackManager = CallbackManager.Factory.create();
    }

    private void initView() {
        progressBar = findViewById(R.id.progressbar);
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
    }

    private void configAccessTokenTrakerFB() {
        accessTokenTracker = new AccessTokenTracker() {
            // This method is invoked everytime access token changes
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                String firstName = object.getString("first_name");
                                String lastName = object.getString("last_name");
                                String email = object.getString("email");
                                String id = object.getString("id");
                                Toast.makeText(MainActivity.this, "успешно facebook", Toast.LENGTH_LONG).show();
                                saveUser(id, email, firstName, lastName, "facebook", accessToken.getToken(), MainActivity.this);
                                addFavoritesAndLogoutButtonsToNavigationDrawer();
                                Log.d("TAG", user.toString());
                                enter.setTitle(firstName + " " + lastName);
                                Log.d("TAG", firstName + " " + lastName + " " + email + " " + id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "first_name,last_name,email,id");
                    request.setParameters(parameters);
                    request.executeAsync();
                } else {
                    enter.setTitle("Вход");
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        requestingLocationPermission = true;
                        PreferenceUtils.saveRequestingLocationPermission(requestingLocationPermission, this);
                        openViewPager();
//                        Utils.getCurrentLocation(getApplicationContext());
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
}
