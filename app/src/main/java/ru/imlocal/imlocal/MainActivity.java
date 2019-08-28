package ru.imlocal.imlocal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.vk.sdk.VKSdk;

import ru.imlocal.imlocal.api.Api;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.network.RetrofitClient;
import ru.imlocal.imlocal.ui.FragmentFeedback;
import ru.imlocal.imlocal.ui.FragmentLogin;
import ru.imlocal.imlocal.ui.FragmentPolicy;
import ru.imlocal.imlocal.ui.FragmentViewPager;
import ru.imlocal.imlocal.ui.FragmentVitrinaAction;
import ru.imlocal.imlocal.ui.FragmentVitrinaEvent;
import ru.imlocal.imlocal.ui.FragmentVitrinaShop;
import ru.imlocal.imlocal.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    public void openPolicy()
    {
        Fragment fragment = new FragmentPolicy();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment).setCustomAnimations(R.anim.enter_act, R.anim.exit_act).addToBackStack("FragmentPolicy").commit();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                .add(R.id.frame, fragment)
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
                .addToBackStack("FragmentVitrinaShop")
                .commit();
    }

    public void openLogin() {
        Fragment fragment = new FragmentLogin();
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame_auth, fragment)
                .addToBackStack("FragmentL")
                .commit();
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
        openViewPager();
        super.onStart();
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
                user.setLogin(false);
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

    private void configAccessTokenTrakerFB() {
        accessTokenTracker = new AccessTokenTracker() {
            // This method is invoked everytime access token changes
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    user = PreferenceUtils.getUser(MainActivity.this);
                    enter.setTitle(user.getUsername());
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
        setSupportActionBar(toolbar);
        appBarLayout.setVisibility(View.VISIBLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

}
