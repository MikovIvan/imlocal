package ru.imlocal.imlocal.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterPhotos;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.entity.ShopAddress;
import ru.imlocal.imlocal.entity.ShopPhoto;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentBusiness.status;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.KEY_RUB;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;
import static ru.imlocal.imlocal.utils.Utils.hideKeyboardFrom;

public class FragmentAddShop extends Fragment implements RecyclerViewAdapterPhotos.OnItemClickListener, FragmentAddressDialog.AddAddressFragmentAddressDialog {

    private static final int PERMISSIONS_REQUEST_CODE = 7459;

    private ShopAddress shopAddress;

    private RecyclerView rvCategory;

    private EditText etWorkTime;
    private EditText etWebsite;
    private EditText etPhoneNumber;
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private Button btnAddPhoto;

    private RecyclerView rvPhotos;
    private RecyclerViewAdapterPhotos adapterPhotos;
    private RecyclerViewAdaptorCategory adaptorCategory;
    private List<String> photosPathList = new ArrayList<>();
    private List<String> photosIdList = new ArrayList<>();
    private ArrayList<String> photosDeleteList = new ArrayList<>();
    private ArrayList<MediaFile> photos = new ArrayList<>();
    private EasyImage easyImage;

    private TextView tvAddAddress;
    private TextView tvPdfdocument;

    private TextInputEditText etShopName;
    private TextInputEditText etShopShortDescription;
    private TextInputEditText etShopFullDescription;

    private Shop shop = new Shop("", "", -1, "", "", "", "", "", "", "", null);

    private Bundle bundle;
    private File pdfFile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_shop, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_background)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        if (PreferenceUtils.getPhotoList(getActivity()) != null && !PreferenceUtils.getPhotoList(getActivity()).isEmpty()) {
            photos.clear();
            photos.addAll(PreferenceUtils.getPhotoList(getActivity()));
        }

        tvPdfdocument = view.findViewById(R.id.tv_add_shop_add_document);
        tvPdfdocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooserDialog chooserDialog = new ChooserDialog(getActivity(), R.style.FileChooserStyle);
                chooserDialog
                        .withResources(R.string.choose_pdf_file, R.string.ok, R.string.cancel)
                        .withFilter(false, false, "pdf")
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String dir, File dirFile) {
                                pdfFile = dirFile;
                                tvPdfdocument.setText(dirFile.getName());
                            }
                        })
                        .build()
                        .show();
            }
        });

        btnAddPhoto = view.findViewById(R.id.btn_add_photo);
        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] necessaryPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (arePermissionsGranted(necessaryPermissions)) {
                    selectImage();
                } else {
                    requestPermissionsCompat(necessaryPermissions, PERMISSIONS_REQUEST_CODE);
                }
            }
        });

        rvPhotos = view.findViewById(R.id.rv_shop_photo);
        adapterPhotos = new RecyclerViewAdapterPhotos(photos, photosPathList, getActivity());
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
        initPhone();
        initRvCategory(view);

        bundle = getArguments();
        if (bundle != null) {
            shop = (Shop) bundle.getSerializable("shop");
            loadShopData(shop);
            List<String> photos = new ArrayList<>();
            for (ShopPhoto shopPhoto : shop.getShopPhotoArray()) {
                photos.add(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + shopPhoto.getShopPhoto());
                photosIdList.add(String.valueOf(shopPhoto.getId()));
            }
            photosPathList.addAll(photos);
            if (photosPathList.size() < 11) {
                btnAddPhoto.setVisibility(View.VISIBLE);
            } else {
                btnAddPhoto.setVisibility(View.GONE);
            }
        }

        easyImage = new EasyImage.Builder(getActivity())
                .setCopyImagesToPublicGalleryFolder(false)
                .setChooserType(ChooserType.CAMERA_AND_GALLERY)
                .setFolderName("EasyImage sample")
                .allowMultiple(false)
                .build();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PreferenceUtils.getShop(getActivity()) != null) {
            shop = PreferenceUtils.getShop(getContext());
            loadShopData(shop);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!status.equals(STATUS_UPDATE)) {
            saveShopData(shop);
            PreferenceUtils.saveShop(shop, getActivity());
            PreferenceUtils.savePhotoList(photos, getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_business, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go_to_preview) {
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
                String regex = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{10}$";
                if (etPhoneNumber.getText().toString().matches(regex)) {
                    shop.setShopPhone(etPhoneNumber.getText().toString());
                } else {
                    Snackbar.make(getView(), "Неверный формат телефона", Snackbar.LENGTH_LONG).show();
                }
            }
//            else {
//                Snackbar.make(getView(), "Введите телефон", Snackbar.LENGTH_LONG).show();
//            }
            if (!etWebsite.getText().toString().equals("")) {
                if (isValidUrl(etWebsite.getText().toString())) {
                    shop.setShopWeb(etWebsite.getText().toString());
                } else {
                    Snackbar.make(getView(), "Неверный формат сайта", Snackbar.LENGTH_LONG).show();
                }
            }
//            else {
//                Snackbar.make(getView(), "Введите адрес сайт", Snackbar.LENGTH_LONG).show();
//            }

            if (!etMinPrice.getText().toString().endsWith(KEY_RUB) && !etMinPrice.getText().toString().equals("")) {
                shop.setShopCostMin(etMinPrice.getText().toString());
            }
            if (!etMaxPrice.getText().toString().endsWith(KEY_RUB) && !etMaxPrice.getText().toString().equals("")) {
                shop.setShopCostMax(etMaxPrice.getText().toString());
            }
//            if (etMinPrice.getText().toString().equals("")) {
//                Snackbar.make(getView(), "Введите минимальную стоимость", Snackbar.LENGTH_LONG).show();
//            } else
            if (etMinPrice.getText().toString().contains(KEY_RUB) && !etMinPrice.getText().toString().endsWith(KEY_RUB)) {
                Snackbar.make(getView(), "Неверно указана минимальная стоимость", Snackbar.LENGTH_LONG).show();
            }
//            if (etMaxPrice.getText().toString().equals("")) {
//                Snackbar.make(getView(), "Введите максимальную стоимость", Snackbar.LENGTH_LONG).show();
//            } else
            if (etMaxPrice.getText().toString().contains(KEY_RUB) && !etMaxPrice.getText().toString().endsWith(KEY_RUB)) {
                Snackbar.make(getView(), "Неверно указана максимальная стоимость", Snackbar.LENGTH_LONG).show();
            }
            if (tvAddAddress.getText().equals("")) {
                Snackbar.make(getView(), "Укажите адрес", Snackbar.LENGTH_LONG).show();
            } else if (status.equals(STATUS_UPDATE)) {
                try {
                    setShopAddress(tvAddAddress.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!etWorkTime.getText().toString().equals("")) {
                String regex = "^((Пн|Вт|Ср|Чт|Пт|Сб|Вс)-(Пн|Вт|Ср|Чт|Пт|Сб|Вс)\\s((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])-((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]))|((Пн|Вт|Ср|Чт|Пт|Сб|Вс)-(Пн|Вт|Ср|Чт|Пт|Сб|Вс)\\s((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])-((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]))\\s(Пн|Вт|Ср|Чт|Пт|Сб|Вс)-(Пн|Вт|Ср|Чт|Пт|Сб|Вс)\\s((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])-((0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9])$";
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(etWorkTime.getText().toString());
                if (matcher.find()) {
                    shop.setShopWorkTime(etWorkTime.getText().toString());
                } else {
                    Snackbar.make(getView(), "Неверный формат. Пн-Пт 16:00-21:00 Сб-Вс 10:00-20:00 или Пн-Пт 16:00-21:00 ", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(getView(), "Укажите время работы", Snackbar.LENGTH_LONG).show();
            }

            if (photos.isEmpty() && photosPathList.isEmpty()) {
                Snackbar.make(getView(), "Прикрепите фотографию", Snackbar.LENGTH_LONG).show();
            }

            if (!shop.getCreatorId().equals("") && !shop.getShopShortName().equals("") && !shop.getShopShortDescription().equals("")
                    && !shop.getShopFullDescription().equals("") && shop.getShopTypeId() != -1 && shopAddress != null
//                    && !shop.getShopPhone().equals("") && !shop.getShopWeb().equals("") && !shop.getShopCostMin().equals("")
//                    && !shop.getShopCostMax().equals("")
                    && !shop.getShopWorkTime().equals("")
                    && !photos.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                bundle.putParcelableArrayList("photos", photos);
                bundle.putSerializable("pdf", pdfFile);
                bundle.putStringArrayList("photoId", photosDeleteList);
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
            } else if (!shop.getCreatorId().equals("") && !shop.getShopShortName().equals("") && !shop.getShopShortDescription().equals("")
                    && !shop.getShopFullDescription().equals("") && shop.getShopTypeId() != -1 && shopAddress != null
                    && !shop.getShopWorkTime().equals("")
                    && !photosPathList.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("shop", shop);
                bundle.putParcelableArrayList("photos", photos);
                bundle.putSerializable("pdf", pdfFile);
                bundle.putStringArrayList("photoId", photosDeleteList);
                ((MainActivity) getActivity()).openVitrinaShop(bundle);
            }
        }
        return super.onOptionsItemSelected(item);
    }

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
                    s += ") ";
                    etPhoneNumber.setText(s);
                    etPhoneNumber.setSelection(s.length());
                }
                if (s.length() == 11 && len < s.length()) {
                    s += " ";
                    etPhoneNumber.setText(s);
                    etPhoneNumber.setSelection(s.length());
                }
                if (s.length() == 14 && len < s.length()) {
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

    private void initRvCategory(View view) {
        rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "add_shop");
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
        setShopAddress(address);
    }

    private void setShopAddress(String address) throws IOException {
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

        easyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                for (MediaFile imageFile : imageFiles) {
                    Log.d("EasyImage", "Image file returned: " + imageFile.getFile().toString());
                    Log.d("EasyImage", "Image file returned: " + imageFile.getFile().getPath());
                }
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        if (bundle != null && !photosPathList.isEmpty()) {
            photosPathList.remove(position);
            photosDeleteList.add(photosIdList.get(position));
            adapterPhotos.notifyDataSetChanged();
        } else if (!photos.isEmpty()) {
            photos.remove(position);
            adapterPhotos.notifyItemRemoved(position);
        }

        if (photos.size() < 11 && photosPathList.size() < 11) {
            btnAddPhoto.setVisibility(View.VISIBLE);
        }
    }

    private void loadShopData(Shop shop) {
        if (!shop.getShopShortName().equals("")) {
            etShopName.setText(shop.getShopShortName());
        }
        if (!shop.getShopShortDescription().equals("")) {
            etShopShortDescription.setText(shop.getShopShortDescription());
        }
        if (!shop.getShopFullDescription().equals("")) {
            etShopFullDescription.setText(shop.getShopFullDescription());
        }
        if (!shop.getShopPhone().equals("")) {
            etPhoneNumber.setText(shop.getShopPhone());
        }
        if (!shop.getShopWeb().equals("")) {
            etWebsite.setText(shop.getShopWeb());
        }
        if (!shop.getShopCostMin().equals("")) {
            etMinPrice.setText(shop.getShopCostMin());
        }
        if (!shop.getShopCostMax().equals("")) {
            etMaxPrice.setText(shop.getShopCostMax());
        }
        if (shop.getShopAddress() != null) {
            tvAddAddress.setText(shop.getShopAddress().toString());
        }
        if (!shop.getShopWorkTime().equals("")) {
            etWorkTime.setText(shop.getShopWorkTime());
        }
        if (shop.getShopTypeId() != 0) {
            adaptorCategory.setCategory_index(shop.getShopTypeId() - 1);
        }
    }

    private void saveShopData(Shop shop) {
        if (!etShopName.getText().toString().equals("")) {
            shop.setShopShortName(String.valueOf(etShopName.getText()));
        }
        if (!etShopShortDescription.getText().toString().equals("")) {
            shop.setShopShortDescription(String.valueOf(etShopShortDescription.getText()));
        }
        if (!etShopFullDescription.getText().toString().equals("")) {
            shop.setShopFullDescription(String.valueOf(etShopFullDescription.getText()));
        }
        if (!etPhoneNumber.getText().toString().equals("")) {
            shop.setShopPhone(etPhoneNumber.getText().toString());
        }
        if (!etWebsite.getText().toString().equals("")) {
            shop.setShopWeb(etWebsite.getText().toString());
        }
        if (!etMinPrice.getText().toString().equals("")) {
            shop.setShopCostMin(etMinPrice.getText().toString());
        }
        if (!etMaxPrice.getText().toString().equals("")) {
            shop.setShopCostMax(etMaxPrice.getText().toString());
        }
//        if (!tvAddAddress.getText().equals("")) {
//            shop.setShopAddress(shopAddress);
//        }
        if (!etWorkTime.getText().toString().equals("")) {
            shop.setShopWorkTime(etWorkTime.getText().toString());
        }
        if (photosPathList.size() > 1) {
            PreferenceUtils.savePhotoPathList(photosPathList, getActivity());
        }
    }

    private void selectImage() {
        final CharSequence[] items = {
                "Сделать фото", "Выбрать из галереи",
                "Отмена"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Сделать фото")) {
                easyImage.openCameraForImage(FragmentAddShop.this);
            } else if (items[item].equals("Выбрать из галереи")) {
                easyImage.openGallery(FragmentAddShop.this);
            } else if (items[item].equals("Отмена")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }

    private void onPhotosReturned(@NonNull MediaFile[] returnedPhotos) {
        photos.addAll(Arrays.asList(returnedPhotos));
        adapterPhotos.notifyDataSetChanged();
        if (photos.size() >= 11) {
            btnAddPhoto.setVisibility(View.GONE);
        }
    }

    private boolean arePermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void requestPermissionsCompat(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(getActivity(), permissions, requestCode);
    }
}


