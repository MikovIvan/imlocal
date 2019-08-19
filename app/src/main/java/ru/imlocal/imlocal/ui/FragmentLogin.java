package ru.imlocal.imlocal.ui;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;

//import static com.qoffee.ilocal.MainActivity.navigationView;

//import static com.qoffee.ilocal.MainActivity.mGoogleSignInClient;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLogin extends DialogFragment {

//    если вызывать из активити как диалогфрагмент, то не отрабатывает метод onActivityResult

    public FragmentLogin() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_login, null);
//        SignInButton signInButton = (SignInButton) view.findViewById(R.id.sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_WIDE);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setRetainInstance(true);
        final ImageButton ibLoginVk = view.findViewById(R.id.btn_login_vk);
        final ImageButton ibLoginFb = view.findViewById(R.id.btn_login_fb);
        final ImageButton ibLoginGoogle = view.findViewById(R.id.btn_login_google);
        ibLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
//                navigationView.getMenu().findItem(R.id.nav_favorites).setVisible(true);
//                navigationView.getMenu().findItem(R.id.nav_logout).setVisible(true);
                dismiss();
            }
        });

        return view;
    }


    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, 0);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(getActivity(), "успешно google", Toast.LENGTH_LONG).show();
            Log.d("User name", account.getIdToken() + " " + account.getEmail() + " " + account.getId() + " "
                    + account.getDisplayName() + " " + account.getFamilyName() + " " + account.getGivenName());
            ((MainActivity) getActivity()).openViewPager();
        } catch (ApiException e) {
            Toast.makeText(getActivity(), "ошибка входа google", Toast.LENGTH_LONG).show();
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 0) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
}
