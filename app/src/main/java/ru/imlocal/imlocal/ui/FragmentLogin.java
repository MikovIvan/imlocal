package ru.imlocal.imlocal.ui;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.User;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static com.vk.sdk.VKUIHelper.getApplicationContext;
import static ru.imlocal.imlocal.MainActivity.accessToken;
import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.callbackManager;
import static ru.imlocal.imlocal.MainActivity.enter;
import static ru.imlocal.imlocal.MainActivity.mGoogleSignInClient;
import static ru.imlocal.imlocal.MainActivity.navigationView;
import static ru.imlocal.imlocal.MainActivity.user;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLogin extends Fragment implements View.OnClickListener {
    private String[] scope = new String[]{VKScope.PHOTOS, VKScope.EMAIL};

    public FragmentLogin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_login, null);
        setRetainInstance(true);

        final ImageButton ibLoginVk = view.findViewById(R.id.btn_login_vk);
        final ImageButton ibLoginFb = view.findViewById(R.id.btn_login_fb);
        final ImageButton ibLoginGoogle = view.findViewById(R.id.btn_login_google);

        ibLoginVk.setOnClickListener(this);
        ibLoginFb.setOnClickListener(this);
        ibLoginGoogle.setOnClickListener(this);
        setUpCondLinks(view);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login_google:
                signInGoogle();
                break;
            case R.id.btn_login_vk:
                signInVk();
                break;
            case R.id.btn_login_fb:
                signInFB();
                break;
        }
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        getActivity().startActivityForResult(signInIntent, 0);
    }

    private void signInVk() {
        VKSdk.login(getActivity(), scope);
    }

    private void signInFB() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        accessToken = loginResult.getAccessToken();
                        loadUserProfile(accessToken);
                    }

                    @Override
                    public void onCancel() {
                        Log.d("TAG", "On cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("TAG", error.toString());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.d("TAG", "VKTOKEN " + res.accessToken);
                final VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "photo_200, contacts, bdate, mobile_phone"));

                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        VKApiUserFull userVK = ((VKList<VKApiUserFull>) response.parsedModel).get(0);
                        saveUser(String.valueOf(userVK.id), res.email, userVK.first_name, userVK.last_name, "vkontakte", res.accessToken);
                        addFavoritesAndLogoutButtonsToNavigationDrawer();
                        Log.d("TAG", user.toString());
                        enter.setTitle(userVK.first_name + " " + userVK.last_name);
                        Log.d("TAG", userVK.first_name + " " + userVK.last_name + " " + userVK.bdate + " " + userVK.id
                                + " " + userVK.mobile_phone + " " + userVK.id);
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "ошибка входа vk", Toast.LENGTH_LONG).show();
            }
        });

        if (requestCode == 0) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void loginUser(User user) {
        Call<User> call = api.loginUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
//                падает с ошибкой
                Log.d("AUTH", "response" + response.toString());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("AUTH", "body: " + response.body().toString());
                    Log.d("AUTH", "message: " + response.message());
                    Log.d("AUTH", "error: " + response.errorBody());
                } else {
                    Call<User> call2 = api.registerUser(user);
                    call2.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            Log.d("AUTH", response.body().toString());
                            Log.d("AUTH", "message: " + response.message());
                            Log.d("AUTH", "error: " + response.errorBody());
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Log.d("AUTH", "onFailure " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("AUTH", "onFailure " + t.getMessage());
            }
        });
    }

//    {
//        "email": "mikov190189@gmail.com",
//            "source": "google",
//            "source_id": "102998265505229591444",
//            "username": "Иван Миков",
//            "accessToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6ImY2ZjgwZjM3ZjIxYzIzZTYxZjJiZTQyMzFlMjdkMjY5ZDY2OTUzMjkiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2NDcxOTcxNTAwMjEtdTNjaDlzaW1nNTI1NThtbWU2aHM4cTB2MGRjazBlZTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2NDcxOTcxNTAwMjEtb2NvOHV0NHJudmVsaW1zODBzZGYyN3ZsNzZpcDh0aHMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDI5OTgyNjU1MDUyMjk1OTE0NDQiLCJlbWFpbCI6Im1pa292MTkwMTg5QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoi0JjQstCw0L0g0JzQuNC60L7QsiIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vLUFwSDlsdHkyeGlzL0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FDSGkzcmZQX1htQW5YbmZ6aUo1TTdQVlkxUFdTbTF2Q3cvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6ItCY0LLQsNC9IiwiZmFtaWx5X25hbWUiOiLQnNC40LrQvtCyIiwibG9jYWxlIjoicnUiLCJpYXQiOjE1NjcwOTY2NjgsImV4cCI6MTU2NzEwMDI2OH0.mnPNgzP9ZvkUQhH0MhwIGvULSgtVpgJfdvQQpwevXbq2pmjYFNd6aAem1DEN_5qKsge4-tPXBSNGhCDu602sMTlenct6Ussgzjya0q5xs5ut00qmgMQEMeKJcuoK50SrDy98Gch4aNiHbSLWVCeu2D24-AW8T0isSpIvQPmejujZ_gGNzJ147SBT12t3iVorw2AuZxETxskhsxqbnZIlrPOVu5-gyHVlm6JRLwIoxtZL6UPG8f3L_4yWYexP_sgI5YolEuPUAYImLZOSgDb75Prqj6FcSh3krhP3ZOAofVhcL96SdDLu_RDS303DzIRQrhYh-xbh8hC1HaeCdYTmQQ"
//    }
//
//    {
//        "email": "mikov1989@rambler.ru",
//            "source": "facebook",
//            "source_id": "2393256167427509",
//            "username": "Иван Миков",
//            "accessToken": "EAAjBP6yi2PMBAPAsJmTwEBo9hZB7Yyy9sWiXPTltpZCSqBTybbzrZANsPZB7DZCzpBkbQ438ZBlUtQZCYOszST0IuWTTzv6mMIAGujV2rH8IGZB7t2UK93SH6P8w3kWAcO1VL1TLeYZBIhot15Dz6YnzOjNEm79qGC1HKbZB2u5SeZCTKJL8ZCxiTcsYpRcbgaynkmz1JVfbILA7ogZDZD"
//    }
//
//    {
//        "email": "mikov1989@rambler.ru",
//            "source": "vkontakte",
//            "source_id": "485338511",
//            "username": "Ivan Mikov",
//            "accessToken": "3925a167025512533eb97db4eafc113d46b7da0801d6260d467e4131f8012c6643ec5a572a6a5eee0081b"
//    }

    private void saveUser(String source_id, String email, String firstName, String lastName, String source, String accessToken) {
        user.setSource_id(source_id);
        user.setSource(source);
        user.setEmail(email);
        user.setAccessToken(accessToken);
        user.setUsername(firstName + " " + lastName);
        Log.d("AUTH", "parametrs: " + user.toString());
        loginUser(user);
        user.setLogin(true);
        PreferenceUtils.saveUser(user, getActivity());
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(getActivity(), "успешно google", Toast.LENGTH_LONG).show();
            enter.setTitle(account.getDisplayName());
            saveUser(account.getId(), account.getEmail(), account.getGivenName(), account.getFamilyName(), "google", account.getIdToken());
            addFavoritesAndLogoutButtonsToNavigationDrawer();
            Log.d("TAG", user.toString());
            Log.d("TAG", account.getIdToken() + " " + account.getEmail() + " " + account.getId() + " "
                    + account.getDisplayName() + " " + account.getFamilyName() + " " + account.getGivenName());

        } catch (ApiException e) {
            Toast.makeText(getActivity(), "ошибка входа google", Toast.LENGTH_LONG).show();
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void loadUserProfile(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String firstName = object.getString("first_name");
                    String lastName = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    saveUser(id, email, firstName, lastName, "facebook", accessToken.getToken());
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
    }

    private void addFavoritesAndLogoutButtonsToNavigationDrawer() {
        navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
    }

    public void setUpCondLinks(View view) {
        String login_disclaimer = "Продолжая, Вы соглашаетесь с нашими Условиями использования и подтверждаете, что прочли нашу Политику конфиденциальности.";
        SpannableString ss = new SpannableString(login_disclaimer);

        ClickableSpan cTOU = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                getFragmentManager().beginTransaction().remove(FragmentLogin.this).commit();
                openTOU();
            }
        };

        ClickableSpan cPolicy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                getFragmentManager().beginTransaction().remove(FragmentLogin.this).commit();
                openPolicy();
            }
        };

        ss.setSpan(cTOU, login_disclaimer.indexOf("Условиями использования"), login_disclaimer.indexOf("Условиями использования") + "Условиями использования".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(cPolicy, login_disclaimer.indexOf("Политику конфиденциальности"), login_disclaimer.indexOf("Политику конфиденциальности") + "Политику конфиденциальности".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView tvCond = view.findViewById(R.id.tv_enter_social_networks_conditions);
        tvCond.setText(ss);
        tvCond.setLinksClickable(true);
        tvCond.setMovementMethod(LinkMovementMethod.getInstance());
        tvCond.setHighlightColor(Color.TRANSPARENT);
        tvCond.setLinkTextColor(tvCond.getCurrentTextColor());
    }

    public void openPolicy() {
        Fragment fragment = new FragmentPolicy();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment).setCustomAnimations(R.anim.enter_act, R.anim.exit_act).addToBackStack("FragmentPolicy").commit();
    }

    public void openTOU() {
        Fragment fragment = new FragmentTOU();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment).setCustomAnimations(R.anim.enter_act, R.anim.exit_act).addToBackStack("FragmentTOU").commit();
    }
}
