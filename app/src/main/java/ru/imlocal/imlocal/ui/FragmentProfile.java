package ru.imlocal.imlocal.ui;


import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.User;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.user;


public class FragmentProfile extends Fragment implements View.OnClickListener, FragmentAddressDialog.AddAddressFragmentAddressDialog {

    private boolean isEditMode;
    private TextInputEditText etFamilyName;
    private TextInputEditText etName;
    private TextInputEditText etMiddleName;
    private TextInputEditText etAdress;
    private ImageButton ibVK;
    private ImageButton ibFB;
    private ImageButton ibGoogle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, null);
        ((MainActivity) getActivity()).enableUpButtonViews(true);

        initViews(view);
        setEditable(false);
        setData();
        setOnClickListeners();

        return view;
    }

    private void setData() {
        etFamilyName.setText(user.getLastName());
        etName.setText(user.getFirstName());
    }

    private void initViews(View view) {
        etFamilyName = view.findViewById(R.id.et_profile_family_name);
        etName = view.findViewById(R.id.et_profile_name);
        etMiddleName = view.findViewById(R.id.et_profile_middle_name);
        etAdress = view.findViewById(R.id.et_profile_adress);
        ibVK = view.findViewById(R.id.btn_login_vk);
        ibFB = view.findViewById(R.id.btn_login_fb);
        ibGoogle = view.findViewById(R.id.btn_login_google);
    }

    private void setEditable(boolean isEditMode) {
        etFamilyName.setClickable(isEditMode);
        etFamilyName.setCursorVisible(isEditMode);
        etFamilyName.setFocusable(isEditMode);
        etFamilyName.setFocusableInTouchMode(isEditMode);
        etName.setClickable(isEditMode);
        etName.setCursorVisible(isEditMode);
        etName.setFocusable(isEditMode);
        etName.setFocusableInTouchMode(isEditMode);
        etMiddleName.setClickable(isEditMode);
        etMiddleName.setCursorVisible(isEditMode);
        etMiddleName.setFocusable(isEditMode);
        etMiddleName.setFocusableInTouchMode(isEditMode);

        ibFB.setClickable(isEditMode);
        ibVK.setClickable(isEditMode);
        ibGoogle.setClickable(isEditMode);
    }

    private void setOnClickListeners() {
        ibFB.setOnClickListener(this);
        ibVK.setOnClickListener(this);
        ibGoogle.setOnClickListener(this);
        etAdress.setOnClickListener(this);
        etFamilyName.setOnClickListener(this);
        etName.setOnClickListener(this);
        etMiddleName.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editmode) {
            if (!isEditMode) {
                setEditable(true);
                item.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_save));
                isEditMode = true;
            } else {
                setEditable(false);
                item.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_edit));
                isEditMode = false;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login_fb:
                if (isEditMode) {
                    Toast.makeText(getActivity(), "Ждем Апи", Toast.LENGTH_LONG).show();
                } else {
                    showSnackBar();
                }
                break;
            case R.id.btn_login_vk:
                if (isEditMode) {
                    Toast.makeText(getActivity(), "Ждем Апи", Toast.LENGTH_LONG).show();
                } else {
                    showSnackBar();
                }
                break;
            case R.id.btn_login_google:
                if (isEditMode) {
                    Toast.makeText(getActivity(), "Ждем Апи", Toast.LENGTH_LONG).show();
                } else {
                    showSnackBar();
                }
                break;
            case R.id.et_profile_adress:
                if (isEditMode) {
                    openAdressDialog();
                } else {
                    showSnackBar();
                }
                break;
            case R.id.et_profile_family_name:
                if (!isEditMode) {
                    showSnackBar();
                }
                break;
            case R.id.et_profile_middle_name:
                if (!isEditMode) {
                    showSnackBar();
                }
                break;
            case R.id.et_profile_name:
                if (!isEditMode) {
                    showSnackBar();
                }
                break;
        }
    }

    private void openAdressDialog() {
        FragmentAddressDialog fragmentAddressDialog = new FragmentAddressDialog();
        fragmentAddressDialog.setAddAddressFragmentAddressDialog(FragmentProfile.this);
        fragmentAddressDialog.show(getActivity().getSupportFragmentManager(), "addressDialog");
    }

    private void showSnackBar() {
        Snackbar.make(getView(), getResources().getString(R.string.need_editable), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onAddressSelected(String address) throws IOException {
        etAdress.setText(address);

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());


        List<Address> userAddress = geocoder.getFromLocationName(address, 1);

        if (userAddress != null) {
            Address returnedAddress = userAddress.get(0);
            ru.imlocal.imlocal.entity.Address currentUserAdress = new ru.imlocal.imlocal.entity.Address();
            currentUserAdress.setCity(returnedAddress.getLocality());
            currentUserAdress.setStreet(returnedAddress.getThoroughfare());
            currentUserAdress.setLatitude(String.valueOf(returnedAddress.getLatitude()));
            currentUserAdress.setLongitude(String.valueOf(returnedAddress.getLongitude()));
            currentUserAdress.setHouseNumber(returnedAddress.getSubThoroughfare());
            user.setMiddleName(etMiddleName.getText().toString());
            user.setUserAddress(currentUserAdress);
            Call<User> call = api.updateUser(user.getId(), user);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    Log.d("ADDRESS", response.toString());
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {

                }
            });

            Log.d("ADDRESS", returnedAddress.getLocality()
                    + " " + returnedAddress.getThoroughfare()
                    + " " + returnedAddress.getLatitude()
                    + " " + returnedAddress.getLongitude()
                    + " " + returnedAddress.getSubThoroughfare());

        }
    }
}
