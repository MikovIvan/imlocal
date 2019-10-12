package ru.imlocal.imlocal.ui;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.imlocal.imlocal.BuildConfig;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterPhotos;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Category;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.utils.FileCompressor;

import static android.app.Activity.RESULT_OK;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.KEY_RUB;
import static ru.imlocal.imlocal.utils.Utils.hideKeyboardFrom;

public class FragmentAddShop extends Fragment implements RecyclerViewAdapterPhotos.OnItemClickListener, FragmentAddressDialog.AddAddressFragmentAddressDialog {

    private TextView result;
    private ShopAddress shopAddress;
    private List<Category> weekDays = new ArrayList<>();
    private List<Category> selectedWeekDays = new ArrayList<>();

    private RecyclerView rvCategory;
    private RecyclerView rvWeekDays;

    private EditText etWorkTime;
    private EditText etDinnerTime;
    private EditText etWebsite;
    private EditText etPhoneNumber;
    private EditText etMinPrice;
    private EditText etMaxPrice;

    private static final int REQUEST_GALLERY_PHOTO = 2;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private RecyclerView rvPhotos;
    private RecyclerViewAdapterPhotos adapterPhotos;
    private List<String> photosPathList = new ArrayList<>();
    private File mPhotoFile;
    private FileCompressor mCompressor;

    private TextView tvAddAddress;

    private TextInputEditText etShopName;
    private TextInputEditText etShopShortDescription;
    private TextInputEditText etShopFullDescription;

    private Shop shop = new Shop("", "", -1, "", "", "", "", "", "", "", null);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_shop, container, false);
        mCompressor = new FileCompressor(getActivity());

        if (photosPathList.isEmpty()) {
            photosPathList.add("add");
        }
//        result = view.findViewById(R.id.tv_result);
//        initWeekDays();

        rvPhotos = view.findViewById(R.id.rv_shop_photo);
        adapterPhotos = new RecyclerViewAdapterPhotos(photosPathList, getActivity());
        rvPhotos.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvPhotos.setAdapter(adapterPhotos);
        adapterPhotos.setOnItemClickListener(this);

        shop.setCreatorId(user.getId());

        etShopName = view.findViewById(R.id.et_add_shop_enter_name);
        etShopShortDescription = view.findViewById(R.id.et_add_shop_subtitle);
        etShopFullDescription = view.findViewById(R.id.et_add_shop_full_description);

        etWorkTime = view.findViewById(R.id.et_add_shop_work_time);

        etWebsite = view.findViewById(R.id.et_add_shop_add_website);
        etPhoneNumber = view.findViewById(R.id.et_add_shop_add_phone);

        tvAddAddress = view.findViewById(R.id.tv_add_shop_add_address);
        tvAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAdressDialog();
            }
        });

        initMinMaxPrice(view);

//        initWorkTimePicker(view);
//        initDinnerTimePicker(view);

        initPhone();
        initRvCategory(view);
//        initRvWeekDays(view);
        return view;
    }

//    private void initWeekDays() {
//        weekDays.add(new Category("Пн", 1));
//        weekDays.add(new Category("Вт", 2));
//        weekDays.add(new Category("Ср", 3));
//        weekDays.add(new Category("Чт", 4));
//        weekDays.add(new Category("Пт", 5));
//        weekDays.add(new Category("Сб", 6));
//        weekDays.add(new Category("Вс", 7));
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_business, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go_to_preview) {
//            calculateTimeTable();

            if (!etShopName.getText().toString().equals("") && etShopName.getText().length() <= 38) {
                shop.setShopShortName(String.valueOf(etShopName.getText()));
            } else {
                Snackbar.make(getView(), "Название неправильное", Snackbar.LENGTH_LONG).show();
            }
            if (!etShopShortDescription.getText().toString().equals("") && etShopShortDescription.getText().length() <= 186) {
                shop.setShopShortDescription(String.valueOf(etShopShortDescription.getText()));
            } else {
                Snackbar.make(getView(), "Введите краткое описание места", Snackbar.LENGTH_LONG).show();
            }
            if (!etShopFullDescription.getText().toString().equals("")) {
                shop.setShopFullDescription(String.valueOf(etShopFullDescription.getText()));
            } else {
                Snackbar.make(getView(), "Введите полное описание места", Snackbar.LENGTH_LONG).show();
            }
            if (shop.getShopTypeId() == -1) {
                Snackbar.make(getView(), "Выберите категорию", Snackbar.LENGTH_LONG).show();
            }
            if (!etPhoneNumber.getText().toString().equals("")) {
                String regex = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$";
                if (etPhoneNumber.getText().toString().matches(regex)) {
                    shop.setShopPhone(etPhoneNumber.getText().toString());
                } else {
                    Snackbar.make(getView(), "Неверный формат телефона", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(getView(), "Введите телефон", Snackbar.LENGTH_LONG).show();
            }
            if (!etWebsite.getText().toString().equals("")) {
                if (isValidUrl(etWebsite.getText().toString())) {
                    shop.setShopWeb(etWebsite.getText().toString());
                } else {
                    Snackbar.make(getView(), "Неверный формат сайта", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(getView(), "Введите адрес сайт", Snackbar.LENGTH_LONG).show();
            }
            if (!etMinPrice.getText().toString().endsWith(KEY_RUB) && !etMinPrice.getText().toString().equals("")) {
                shop.setShopCostMin(etMinPrice.getText().toString());
            }
            if (!etMaxPrice.getText().toString().endsWith(KEY_RUB) && !etMaxPrice.getText().toString().equals("")) {
                shop.setShopCostMax(etMaxPrice.getText().toString());
            }
            if (etMinPrice.getText().toString().equals("")) {
                Snackbar.make(getView(), "Введите минимальную стоимость", Snackbar.LENGTH_LONG).show();
            } else if (etMinPrice.getText().toString().contains(KEY_RUB) && !etMinPrice.getText().toString().endsWith(KEY_RUB)) {
                Snackbar.make(getView(), "Неверно указана минимальная стоимость", Snackbar.LENGTH_LONG).show();
            }
            if (etMaxPrice.getText().toString().equals("")) {
                Snackbar.make(getView(), "Введите максимальную стоимость", Snackbar.LENGTH_LONG).show();
            } else if (etMaxPrice.getText().toString().contains(KEY_RUB) && !etMaxPrice.getText().toString().endsWith(KEY_RUB)) {
                Snackbar.make(getView(), "Неверно указана максимальная стоимость", Snackbar.LENGTH_LONG).show();
            }
            if (tvAddAddress.getText().equals("")) {
                Snackbar.make(getView(), "Укажите адрес", Snackbar.LENGTH_LONG).show();
            }
            if(!etWorkTime.getText().toString().equals("")){
                String regex = "^((Пн|Вт|Ср|Чт|Пт|Сб|Вс)-(Пн|Вт|Ср|Чт|Пт|Сб|Вс)\\s((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])-((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]))|((Пн|Вт|Ср|Чт|Пт|Сб|Вс)-(Пн|Вт|Ср|Чт|Пт|Сб|Вс)\\s((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])-((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]))\\s(Пн|Вт|Ср|Чт|Пт|Сб|Вс)-(Пн|Вт|Ср|Чт|Пт|Сб|Вс)\\s((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])-((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])$";
                if (etWorkTime.getText().toString().matches(regex)) {
                    shop.setShopWorkTime(etWorkTime.getText().toString());
                } else {
                    Snackbar.make(getView(), "Неверный формат ", Snackbar.LENGTH_LONG).show();
                }
            }

            if (photosPathList.size() == 1) {
                Snackbar.make(getView(), "Прикрепите фотографию", Snackbar.LENGTH_LONG).show();
            }

            if (!shop.getCreatorId().equals("") && !shop.getShopShortName().equals("") && !shop.getShopShortDescription().equals("")
                    && !shop.getShopFullDescription().equals("") && shop.getShopTypeId() != -1 && shopAddress != null
                    && !shop.getShopPhone().equals("") && !shop.getShopWeb().equals("") && !shop.getShopCostMin().equals("")
                    && !shop.getShopCostMax().equals("") && !shop.getShopWorkTime().equals("")) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                bundle.putStringArrayList("photosPathList", (ArrayList<String>) photosPathList);
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
//                Call<ShopAddress> call = api.createShopAddress(Credentials.basic(user.getAccessToken(), ""), shopAddress);
//                call.enqueue(new Callback<ShopAddress>() {
//                    @Override
//                    public void onResponse(Call<ShopAddress> call, Response<ShopAddress> response) {
//                        Log.d("Adres", "Id:" + response.body().getId());
//                        shop.setShopAddressId(String.valueOf(response.body().getId()));
//
//                        Call<Shop> call1 = api.createShop(Credentials.basic(user.getAccessToken(), ""), shop);
//                        call1.enqueue(new Callback<Shop>() {
//                            @Override
//                            public void onResponse(Call<Shop> call, Response<Shop> response) {
//                                Log.d("Adres", "Id:" + response.body());
//                            }
//
//                            @Override
//                            public void onFailure(Call<Shop> call, Throwable t) {
//
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFailure(Call<ShopAddress> call, Throwable t) {
//
//                    }
//                });
            }

        }
        return super.onOptionsItemSelected(item);
    }

//    private void calculateTimeTable() {
//        for (Category category : weekDays) {
//            if (category.isSelected()) {
//                selectedWeekDays.add(category);
//            }
//        }
//        Collections.sort(selectedWeekDays, new Comparator<Category>() {
//            @Override
//            public int compare(Category c1, Category c2) {
//                return c1.getDayOfWeek() - c2.getDayOfWeek();
//            }
//        });
//
//        for (Category category : selectedWeekDays) {
//            Log.d("SELECTED", String.valueOf(category.getDayOfWeek()));
//        }
//        String timetable = "";
//        String workTime =  etWorkTime.getText().toString();
//        timetable = selectedWeekDays.get(0).getName() + "-" + selectedWeekDays.get(selectedWeekDays.size()-1).getName() + " " + workTime;
//
//        result.setText(timetable);
//    }

    private void openAdressDialog() {
        FragmentAddressDialog fragmentAddressDialog = new FragmentAddressDialog();
        fragmentAddressDialog.setAddAddressFragmentAddressDialog(FragmentAddShop.this);
        fragmentAddressDialog.show(getActivity().getSupportFragmentManager(), "addressDialog");
    }

    private void initMinMaxPrice(View view) {
        etMinPrice = view.findViewById(R.id.et_add_shop_min_price);
        etMaxPrice = view.findViewById(R.id.et_add_shop_max_price);

        etMinPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!etMinPrice.getText().toString().equals("")) {
                        String s = "";
                        if (etMinPrice.getText().toString().endsWith(KEY_RUB)) {
                            shop.setShopCostMin(etMinPrice.getText().toString().substring(0, etMinPrice.getText().length() - 1));
                            s = etMinPrice.getText().toString();
                        } else if (etMinPrice.getText().toString().contains(KEY_RUB) && !etMinPrice.getText().toString().endsWith(KEY_RUB)) {
                            shop.setShopCostMin("");
                            Snackbar.make(getView(), "Неверно указана цена", Snackbar.LENGTH_LONG).show();
                            s = etMinPrice.getText().toString();
                        } else {
                            shop.setShopCostMin(etMinPrice.getText().toString());
                            s = etMinPrice.getText().toString().concat(KEY_RUB);
                        }
                        etMinPrice.setText(s);
                    } else {
                        etMinPrice.setHint("min");
                    }
                    hideKeyboardFrom(getActivity(), textView);
                    return true;
                }
                return false;
            }
        });

        etMaxPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!etMaxPrice.getText().toString().equals("")) {
                        String s = "";
                        if (etMaxPrice.getText().toString().endsWith(KEY_RUB)) {
                            shop.setShopCostMax(etMaxPrice.getText().toString().substring(0, etMaxPrice.getText().length() - 1));
                            s = etMaxPrice.getText().toString();
                        } else if (etMaxPrice.getText().toString().contains(KEY_RUB) && !etMaxPrice.getText().toString().endsWith(KEY_RUB)) {
                            shop.setShopCostMax("");
                            Snackbar.make(getView(), "Неверно указана цена", Snackbar.LENGTH_LONG).show();
                            s = etMaxPrice.getText().toString();
                        } else {
                            shop.setShopCostMax(etMaxPrice.getText().toString());
                            s = etMaxPrice.getText().toString().concat(KEY_RUB);
                        }
                        etMaxPrice.setText(s);
                    } else {
                        etMaxPrice.setHint("max");
                    }
                    hideKeyboardFrom(getActivity(), textView);
                    return true;
                }
                return false;
            }
        });
    }

    private void initPhone() {
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            int len = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = etPhoneNumber.getText().toString();
                len = str.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = etPhoneNumber.getText().toString();
                if (!s.startsWith("+7(")) {
                    etPhoneNumber.setText("+7(");
                    etPhoneNumber.setSelection(etPhoneNumber.getText().length());
                }
                if (s.length() == 6 && len < s.length()) {
                    s += ")";
                    etPhoneNumber.setText(s);
                    etPhoneNumber.setSelection(s.length());
                }
                if (s.length() == 10 && len < s.length()) {
                    s += " ";
                    etPhoneNumber.setText(s);
                    etPhoneNumber.setSelection(s.length());
                }
                if (s.length() == 13 && len < s.length()) {
                    s += " ";
                    etPhoneNumber.setText(s);
                    etPhoneNumber.setSelection(s.length());
                }
                String regex = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$";
                System.out.println(etPhoneNumber.getText().toString().matches(regex));
            }
        });
    }

    private boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url.toLowerCase()).matches();
    }

//    private void initDinnerTimePicker(View view) {
//        etDinnerTime = view.findViewById(R.id.et_add_shop_select_dinner_time);
//        initTextCahngeListener(etDinnerTime);
//    }
//
//    private void initWorkTimePicker(View view) {
//        etWorkTime = view.findViewById(R.id.et_add_shop_select_work_time);
//        initTextCahngeListener(etWorkTime);
//    }

    private void initTextCahngeListener(EditText etWorkTime) {
        etWorkTime.addTextChangedListener(new TextWatcher() {
            int len = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = etWorkTime.getText().toString();
                len = str.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = etWorkTime.getText().toString();
                if (s.length() == 2 && len < s.length()) {
                    s += ":";
                    etWorkTime.setText(s);
                    etWorkTime.setSelection(s.length());
                }
                if (s.length() == 5 && len < s.length()) {
                    s += "-";
                    etWorkTime.setText(s);
                    etWorkTime.setSelection(s.length());
                }
                if (s.length() == 8 && len < s.length()) {
                    s += ":";
                    etWorkTime.setText(s);
                    etWorkTime.setSelection(s.length());
                }
            }
        });
    }

//    private void initRvWeekDays(View view) {
//        rvWeekDays = view.findViewById(R.id.rv_timetable);
//        rvWeekDays.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
//        RecyclerViewAdapterWeekDays adaptorCategory = new RecyclerViewAdapterWeekDays(getContext(), weekDays);
//        rvWeekDays.setAdapter(adaptorCategory);
//        adaptorCategory.setOnItemClickListener(new RecyclerViewAdapterWeekDays.OnItemCategoryClickListener() {
//            @Override
//            public void onItemClickCategory(int position) {
//
//            }
//        });
//    }

    private void initRvCategory(View view) {
        rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "add_shop");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(new RecyclerViewAdaptorCategory.OnItemCategoryClickListener() {
            @Override
            public void onItemClickCategory(int position) {
                shop.setShopTypeId(position + 1);
            }
        });
    }

    @Override
    public void onAddressSelected(String address) throws IOException {
        tvAddAddress.setText(address);

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> userAddress = geocoder.getFromLocationName(address, 1);

        if (userAddress != null) {
            Address returnedAddress = userAddress.get(0);
            shopAddress = new ShopAddress();
            shopAddress.setCity(returnedAddress.getLocality());
            shopAddress.setStreet(returnedAddress.getThoroughfare());
            shopAddress.setLatitude((float) returnedAddress.getLatitude());
            shopAddress.setLongitude((float) returnedAddress.getLongitude());
            shopAddress.setHouseNumber(returnedAddress.getSubThoroughfare());

            shop.setShopAddress(shopAddress);
            Log.d("ADDRESS", returnedAddress.getLocality()
                    + " " + returnedAddress.getThoroughfare()
                    + " " + returnedAddress.getLatitude()
                    + " " + returnedAddress.getLongitude()
                    + " " + returnedAddress.getSubThoroughfare());

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mCompressor.compressToFile(mPhotoFile);
                    photosPathList.add(String.valueOf(Uri.fromFile(mPhotoFile)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                Log.d("PATH", String.valueOf(selectedImage));
                photosPathList.add(String.valueOf(selectedImage));
            }
            if (photosPathList.size() == 12) {
                photosPathList.remove(0);
            }
            adapterPhotos.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        Toast.makeText(getActivity(), String.valueOf(position), Toast.LENGTH_LONG).show();
        photosPathList.remove(position);
        adapterPhotos.notifyItemRemoved(position);

        if (photosPathList.size() == 10 && !photosPathList.get(0).equals("add")) {
            photosPathList.add(0, "add");
            adapterPhotos.notifyItemInserted(0);
        }
    }

    @Override
    public void onItemClick(int position) {
        if (photosPathList.get(0).equals("add") && position == 0) {
            selectImage();
        }
    }

    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }

    private void selectImage() {
        final CharSequence[] items = {
                "Сделать фото", "Выбрать из галереи",
                "Отмена"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Сделать фото")) {
                requestStoragePermission(true);
            } else if (items[item].equals("Выбрать из галереи")) {
                requestStoragePermission(false);
            } else if (items[item].equals("Отмена")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void requestStoragePermission(boolean isCamera) {
        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (isCamera) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryIntent();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(
                        error -> Toast.makeText(getActivity(), "Error occurred! ", Toast.LENGTH_SHORT)
                                .show())
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Need Permissions");
        builder.setMessage(
                "This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);

                mPhotoFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(mFileName, ".jpg", storageDir);
    }
}


