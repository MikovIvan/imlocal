package ru.imlocal.imlocal.ui;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import ru.imlocal.imlocal.R;

import static ru.imlocal.imlocal.MainActivity.mGoogleSignInClient;
import static ru.imlocal.imlocal.MainActivity.navigationView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLogin extends DialogFragment implements View.OnClickListener {
    private String[] scope = new String[]{VKScope.PHOTOS, VKScope.EMAIL};

    public FragmentLogin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_login, null);


        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setRetainInstance(true);
        final ImageButton ibLoginVk = view.findViewById(R.id.btn_login_vk);
        final ImageButton ibLoginFb = view.findViewById(R.id.btn_login_fb);
        final ImageButton ibLoginGoogle = view.findViewById(R.id.btn_login_google);

        ibLoginVk.setOnClickListener(this);
        ibLoginFb.setOnClickListener(this);
        ibLoginGoogle.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login_google:
                signInGoogle();
                navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                dismiss();
                break;
            case R.id.btn_login_vk:
                signInVk();
                navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                dismiss();
                break;
            case R.id.btn_login_fb:
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

}
