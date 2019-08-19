package ru.imlocal.imlocal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import ru.imlocal.imlocal.api.Api;
import ru.imlocal.imlocal.network.RetrofitClient;
import ru.imlocal.imlocal.ui.FragmentLogin;
import ru.imlocal.imlocal.ui.FragmentViewPager;
import ru.imlocal.imlocal.ui.FragmentVitrinaAction;
import ru.imlocal.imlocal.ui.FragmentVitrinaEvent;
import ru.imlocal.imlocal.ui.FragmentVitrinaShop;
import ru.imlocal.imlocal.utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean isLogin = false;
    public static boolean isLoading;
    //    FragmentLogin frLogin;
    public static Api api;
    public static AppBarLayout appBarLayout;
    public static ProgressBar progressBar;
    private String[] scope = new String[]{VKScope.PHOTOS, VKScope.EMAIL};
    private boolean mToolBarNavigationListenerIsRegistered = false;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private MenuItem enter;

    private GoogleSignInClient mGoogleSignInClient;

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
//        frLogin = new FragmentLogin();

        api = RetrofitClient.getInstance().getApi();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appBarLayout = findViewById(R.id.appbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        enter = menu.findItem(R.id.nav_login);
        navigationView.setNavigationItemSelectedListener(this);

        progressBar = findViewById(R.id.progressbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appBarLayout.setVisibility(View.VISIBLE);
        showLoadingIndicator(false);

        configGoogleAuth();
        enableUpButtonViews(false);
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

    public void openLogin() {
        Fragment fragment = new FragmentLogin();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentL")
                .commit();
    }

    public void openViewPager() {
        Fragment fragment = new FragmentViewPager();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_act, R.anim.exit_act)
                .replace(R.id.frame, fragment)
                .addToBackStack("FragmentViewPager")
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

    @Override
    protected void onStart() {
        if (VKSdk.isLoggedIn()) {
            enter.setTitle(PreferenceUtils.getUserName(this));
            navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            enter.setTitle(account.getDisplayName());
            navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }
        if (!VKSdk.isLoggedIn() && account == null) {
            navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
        }
        openViewPager();
        super.onStart();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_login:
                if (!isLogin) {
                    showLoginDialog();
                }
                break;
            case R.id.nav_help:
                Toast.makeText(this, "помощь", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_for_business:
                Toast.makeText(this, "бизнесу", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_favorites:
                Toast.makeText(this, "избранное", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                if (VKSdk.isLoggedIn()) {
                    VKSdk.logout();
                    Toast.makeText(this, "выход", Toast.LENGTH_SHORT).show();
                }
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
                enter.setTitle("Вход");
                navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(false);
                isLogin = false;
                break;
        }
        return true;
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(dialogView);

        final ImageButton ibLoginVk = dialogView.findViewById(R.id.btn_login_vk);
        final ImageButton ibLoginFb = dialogView.findViewById(R.id.btn_login_fb);
        final ImageButton ibLoginGoogle = dialogView.findViewById(R.id.btn_login_google);

        final AlertDialog b = builder.create();
        b.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        b.show();

        ibLoginVk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInVk();
                navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                b.dismiss();
            }


        });

        ibLoginFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ibLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
                navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                b.dismiss();
            }
        });

    }

    private void signInVk() {
        VKSdk.login(this, scope);
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
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getApplicationContext(), "успешно vk", Toast.LENGTH_LONG).show();
                Log.d("TAG", "VKTOKEN " + res.accessToken);
                final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_200, contacts, bdate, mobile_phone"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        VKApiUserFull user = ((VKList<VKApiUserFull>) response.parsedModel).get(0);
                        PreferenceUtils.saveUserName(user.first_name + " " + user.last_name, getApplicationContext());
                        enter.setTitle(user.first_name + " " + user.last_name);
                        Log.d("TAG", user.first_name + " " + user.last_name + " " + user.bdate + " " + user.id
                                + " " + user.mobile_phone);
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "ошибка входа vk", Toast.LENGTH_LONG).show();
            }
        }))

            if (requestCode == 0) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(this, "успешно google", Toast.LENGTH_LONG).show();
            enter.setTitle(account.getDisplayName());
            isLogin = true;
            Log.d("TAG", account.getIdToken() + " " + account.getEmail() + " " + account.getId() + " "
                    + account.getDisplayName() + " " + account.getFamilyName() + " " + account.getGivenName());
            this.openViewPager();
        } catch (ApiException e) {
            Toast.makeText(this, "ошибка входа google", Toast.LENGTH_LONG).show();
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 0);
    }
}
