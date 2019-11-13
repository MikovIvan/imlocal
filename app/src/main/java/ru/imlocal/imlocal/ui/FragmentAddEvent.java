package ru.imlocal.imlocal.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.threeten.bp.LocalDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterPhotos;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.EventPhoto;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.PreferenceUtils;

import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentBusiness.shopListBusiness;
import static ru.imlocal.imlocal.ui.FragmentBusiness.status;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.EVENT_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER5;
import static ru.imlocal.imlocal.utils.Constants.KEY_RUB;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;
import static ru.imlocal.imlocal.utils.Utils.hideKeyboardFrom;
import static ru.imlocal.imlocal.utils.Utils.simpleDateFormat;

public class FragmentAddEvent extends Fragment implements RecyclerViewAdapterPhotos.OnItemClickListener, View.OnClickListener, FragmentAddressDialog.AddAddressFragmentAddressDialog, RecyclerViewAdaptorCategory.OnItemCategoryClickListener, FragmentCalendarDialog.DatePickerDialogFragmentEvents {

    private static final int PERMISSIONS_REQUEST_CODE = 7459;

    private RecyclerView rvCategory;
    private TextView tvDatePicker;
    private TextView tvTimePicker;
    private TextView tvAddAddress;
    private EditText etAddPrice;
    private TextView tvAddFreePrice;
    private Button btnAddPhoto;
    private MaterialSpinner spinner;
    private RecyclerView rvPhotos;
    private RecyclerViewAdapterPhotos adapterPhotos;
    private List<String> photosPathList = new ArrayList<>();
    private List<String> photosIdList = new ArrayList<>();
    private ArrayList<String> photosDeleteList = new ArrayList<>();
    private ArrayList<MediaFile> photos = new ArrayList<>();
    private EasyImage easyImage;

    private RecyclerViewAdaptorCategory adaptorCategory;

    private String beginDate, endDate, time, defaultTime = " 00:00:00";

    private Event event = new Event(-1, -1, "", "", "", -1, "", -1);

    private TextInputEditText etEventName;
    private TextInputEditText etEventDescription;

    //        это потом заменить на места юзера
    private List<Shop> userShops = new ArrayList<>();
    private List<String> shopsName = new ArrayList<>();

    private Bundle bundle;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_background)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        if (PreferenceUtils.getPhotoList(getActivity()) != null && !PreferenceUtils.getPhotoList(getActivity()).isEmpty()) {
            photos.clear();
            photos.addAll(PreferenceUtils.getPhotoList(getActivity()));
        }

        event.setCreatorId(Integer.parseInt(user.getId()));

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
        initRvCategory(view);
        initSpinner(view);
        initDatePicker(view);
        initTimePicker(view);
        initSetPrice(view);

        etEventName = view.findViewById(R.id.et_add_event_enter_name);
        etEventDescription = view.findViewById(R.id.et_add_event_full_description);

        bundle = getArguments();
        if (bundle != null) {
            event = (Event) bundle.getSerializable("event");
            try {
                loadEventData(event);
                List<String> photos = new ArrayList<>();
                for (EventPhoto eventPhoto : event.getEventPhotoList()) {
                    photos.add(BASE_IMAGE_URL + EVENT_IMAGE_DIRECTION + eventPhoto.getEventPhoto());
                    photosIdList.add(String.valueOf(eventPhoto.getId()));
                }
                photosPathList.clear();
                photosPathList.addAll(photos);
                btnAddPhoto.setVisibility(View.GONE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        rvPhotos = view.findViewById(R.id.rv_add_photo);
        adapterPhotos = new RecyclerViewAdapterPhotos(photos, photosPathList, getActivity());
        rvPhotos.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvPhotos.setAdapter(adapterPhotos);
        adapterPhotos.setOnItemClickListener(this);

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
        if (PreferenceUtils.getEvent(getActivity()) != null) {
            event = PreferenceUtils.getEvent(getContext());
            try {
                loadEventData(event);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!status.equals(STATUS_UPDATE)) {
            saveEventData(event);
            PreferenceUtils.saveEvent(event, getActivity());
            PreferenceUtils.savePhotoList(photos, getActivity());
        }
    }

    private void initSetPrice(View view) {
        etAddPrice = view.findViewById(R.id.et_add_event_add_price);
        etAddPrice.setOnClickListener(this);
        etAddPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                etAddPrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_main_color));
                etAddPrice.setTextColor(getResources().getColor(android.R.color.white));

                tvAddFreePrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_white));
                tvAddFreePrice.setTextColor(getResources().getColor(R.color.color_text));
            }
        });
        etAddPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!etAddPrice.getText().toString().equals("")) {
                        String s = "";
                        if (etAddPrice.getText().toString().endsWith(KEY_RUB)) {
                            event.setPrice(Integer.parseInt(etAddPrice.getText().toString().substring(0, etAddPrice.getText().length() - 1)));
                            s = etAddPrice.getText().toString();
                        } else if (etAddPrice.getText().toString().contains(KEY_RUB) && !etAddPrice.getText().toString().endsWith(KEY_RUB)) {
                            event.setPrice(-1);
                            Snackbar.make(getView(), "Неверно указана цена", Snackbar.LENGTH_LONG).show();
                            s = etAddPrice.getText().toString();
                        } else {
                            event.setPrice(Integer.parseInt(etAddPrice.getText().toString()));
                            s = etAddPrice.getText().toString().concat(KEY_RUB);
                        }
                        etAddPrice.setText(s);
                    } else {
                        etAddPrice.setHint("1000" + KEY_RUB);
                        etAddPrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_white));
                        etAddPrice.setTextColor(getResources().getColor(R.color.color_text));
                    }
                    hideKeyboardFrom(getActivity(), textView);
                    return true;
                }
                return false;
            }
        });

        tvAddFreePrice = view.findViewById(R.id.tv_add_event_free);
        tvAddFreePrice.setOnClickListener(this);

        tvAddAddress = view.findViewById(R.id.tv_add_event_add_address);
        tvAddAddress.setOnClickListener(this);
    }

    private void initRvCategory(View view) {
        rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "add_event");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);
    }

    private void initSpinner(View view) {
        //        это потом заменить на места юзера

        for (Shop shop : shopListBusiness) {
            shopsName.add(shop.getShopShortName());
        }

        spinner = view.findViewById(R.id.spinner_add_event_choose_place);
        if (!shopsName.isEmpty()) {
            spinner.setItems(shopsName);
        } else {
            spinner.setHint("У Вас нет мест");
        }
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                event.setShopId(shopListBusiness.get(position).getShopId());
                event.setAddress(shopListBusiness.get(position).getShopAddress().toString());
                tvAddAddress.setText(shopListBusiness.get(position).getShopAddress().toString());
            }
        });
    }

    private void initDatePicker(View view) {
        tvDatePicker = view.findViewById(R.id.tv_add_event_select_date);
        tvDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentCalendarDialog calendarDialogFragment = new FragmentCalendarDialog();
                calendarDialogFragment.setDatePickerDialogFragmentEvents(FragmentAddEvent.this);
                calendarDialogFragment.show(getActivity().getSupportFragmentManager(), "calendarDialog");
            }
        });
    }

    private void initTimePicker(View view) {
        tvTimePicker = view.findViewById(R.id.tv_add_event_select_time);
        tvTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        tvTimePicker.setText(checkDigit(selectedHour) + ":" + checkDigit(selectedMinute));
                        tvTimePicker.setTextColor(getResources().getColor(R.color.color_text));
                        StringBuilder sb = new StringBuilder();
                        sb.append(" ").append(checkDigit(selectedHour)).append(":").append(checkDigit(selectedMinute)).append(":00");
                        time = sb.toString();
                    }
                }, hour, minute, true);
                timePicker.show();
            }
        });
    }

    @Override
    public void onItemClickCategory(int position) {
        event.setEventTypeId(position + 1);
        adaptorCategory.notifyDataSetChanged();
    }

    @Override
    public void onDateSelected(String date, LocalDate start, LocalDate end) {
        if (date.length() > 8) {
            setHorizontalWeight(tvDatePicker, 2.0f);
            setHorizontalWeight(tvTimePicker, 1.0f);
        } else {
            setHorizontalWeight(tvDatePicker, 1.0f);
            setHorizontalWeight(tvTimePicker, 1.0f);
        }
        tvDatePicker.setText(date);
        tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
        beginDate = FORMATTER5.format(start);
        endDate = FORMATTER5.format(end);
//        event.setBegin(FORMATTER5.format(start));
//        event.setEnd(FORMATTER4.format(end));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_add_event_add_address:
                openAdressDialog();
                break;
            case R.id.tv_add_event_free:
                tvAddFreePrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_main_color));
                tvAddFreePrice.setTextColor(getResources().getColor(android.R.color.white));

                etAddPrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_white));
                etAddPrice.setTextColor(getResources().getColor(R.color.color_text));
                etAddPrice.setText("");

                event.setPrice(0);
                break;
            case R.id.et_add_event_add_price:
                etAddPrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_main_color));
                etAddPrice.setTextColor(getResources().getColor(android.R.color.white));

                tvAddFreePrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_white));
                tvAddFreePrice.setTextColor(getResources().getColor(R.color.color_text));
                break;
        }
    }

    private void openAdressDialog() {
        FragmentAddressDialog fragmentAddressDialog = new FragmentAddressDialog();
        fragmentAddressDialog.setAddAddressFragmentAddressDialog(FragmentAddEvent.this);
        fragmentAddressDialog.show(getActivity().getSupportFragmentManager(), "addressDialog");
    }

    @Override
    public void onAddressSelected(String address) {
        tvAddAddress.setText(address);
        event.setAddress(address);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_business, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go_to_preview) {
            if (!etAddPrice.getText().toString().endsWith(KEY_RUB) && !etAddPrice.getText().toString().equals("") && event.getPrice() != 0) {
                event.setPrice(Integer.parseInt(etAddPrice.getText().toString()));
            }
            if (!etEventName.getText().toString().equals("") && etEventName.getText().length() <= 30) {
                event.setTitle(String.valueOf(etEventName.getText()));
            } else {
                Snackbar.make(getView(), "Название неправильное", Snackbar.LENGTH_LONG).show();
            }
            if (!etEventDescription.getText().toString().equals("")) {
                event.setDescription(String.valueOf(etEventDescription.getText()));
            } else {
                Snackbar.make(getView(), "Введите описание события", Snackbar.LENGTH_LONG).show();
            }
            if (event.getEventTypeId() == -1) {
                Snackbar.make(getView(), "Выберите категорию", Snackbar.LENGTH_LONG).show();
            }
            if (event.getAddress().equals("")) {
                Snackbar.make(getView(), "Укажите адрес", Snackbar.LENGTH_LONG).show();
            }
            if (beginDate == null) {
                Snackbar.make(getView(), "Выберите дату события", Snackbar.LENGTH_LONG).show();
            }
            if (time == null) {
                Snackbar.make(getView(), "Укажите время", Snackbar.LENGTH_LONG).show();
            }
            if (event.getPrice() == -1) {
                Snackbar.make(getView(), "Выберите стоимость события", Snackbar.LENGTH_LONG).show();
            }
            if (photos.isEmpty() && photosPathList.isEmpty()) {
                Snackbar.make(getView(), "Прикрепите фотографию", Snackbar.LENGTH_LONG).show();
            }
            if (etAddPrice.getText().toString().contains(KEY_RUB) && !etAddPrice.getText().toString().endsWith(KEY_RUB)) {
                Snackbar.make(getView(), "Неверно указана цена", Snackbar.LENGTH_LONG).show();
            }
            if (event.getCreatorId() != -1 && !event.getTitle().equals("") && !event.getDescription().equals("") && !event.getAddress().equals("")
                    && event.getPrice() != -1 && event.getEventTypeId() != -1 && time != null && beginDate != null && !photos.isEmpty()) {
                event.setBegin(beginDate + time);
                event.setEnd(endDate + defaultTime);
                if (!event.getBegin().equals("") && event.getBegin().endsWith(":00")) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", event);
                    bundle.putParcelableArrayList("photos", photos);
                    bundle.putStringArrayList("photoId", photosDeleteList);
                    ((MainActivity) getActivity()).openVitrinaEvent(bundle);
                }
//                это для обновления инфы,т.к. photos empty может быть
            } else if (event.getCreatorId() != -1 && !event.getTitle().equals("") && !event.getDescription().equals("") && !event.getAddress().equals("")
                    && event.getPrice() != -1 && event.getEventTypeId() != -1 && time != null && beginDate != null
                    && !photosPathList.isEmpty()) {
                if (!event.getBegin().equals("") && event.getBegin().endsWith(":00")) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("event", event);
                    bundle.putParcelableArrayList("photos", photos);
                    bundle.putStringArrayList("photoId", photosDeleteList);
                    ((MainActivity) getActivity()).openVitrinaEvent(bundle);
                }
            }
        }
        return super.onOptionsItemSelected(item);
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

        if (photos.isEmpty() && photosPathList.isEmpty()) {
            btnAddPhoto.setVisibility(View.VISIBLE);
        }
    }

    private void setHorizontalWeight(View view, float weight) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.horizontalWeight = weight;
        view.setLayoutParams(params);
    }

    private void loadEventData(Event event) throws ParseException {
        if (event.getShopId() != -1) {
            String name = "";
            for (Shop shop : shopListBusiness) {
                if (shop.getShopId() == event.getShopId()) {
                    name = shop.getShopShortName();
                }
            }
            for (int i = 0; i < shopsName.size(); i++) {
                if (shopsName.get(i).contains(name)) {
                    spinner.setSelectedIndex(i);
                    tvAddAddress.setText(shopListBusiness.get(i).getShopAddress().toString());
                }
            }
        }
        if (event.getPrice() == 0) {
            tvAddFreePrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_main_color));
            tvAddFreePrice.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (event.getPrice() > 0) {
            etAddPrice.setText(event.getPrice() + KEY_RUB);
            etAddPrice.setBackground(getActivity().getResources().getDrawable(R.drawable.btn_round_main_color));
            etAddPrice.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (!event.getTitle().equals("")) {
            etEventName.setText(event.getTitle());
        }
        if (!event.getDescription().equals("")) {
            etEventDescription.setText(event.getDescription());
        }
        if (!event.getAddress().equals("")) {
            tvAddAddress.setText(event.getAddress());
        }
        if (!event.getBegin().equals("") && event.getBegin().length() == 10) {
            beginDate = event.getBegin();
            tvDatePicker.setText(simpleDateFormat(beginDate));
            tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
        }
        if (!event.getBegin().equals("") && event.getBegin().length() == 9) {
            time = event.getBegin();
            tvTimePicker.setText(time.substring(0, 6));
            tvTimePicker.setTextColor(getResources().getColor(R.color.color_text));
        }
        if (!event.getBegin().equals("") && event.getBegin().length() == 19) {
            beginDate = event.getBegin().substring(0, 10);
            time = event.getBegin().substring(10, 19);
            tvTimePicker.setText(time.substring(0, 6));
            tvTimePicker.setTextColor(getResources().getColor(R.color.color_text));
            tvDatePicker.setText(simpleDateFormat(beginDate));
            tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
        }
        if (event.getEnd() != null) {
            endDate = event.getEnd().substring(0, 10);
            if (!beginDate.equals(endDate)) {
                tvDatePicker.setText("c " + simpleDateFormat(beginDate) + " по " + simpleDateFormat(endDate));
                tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
            }
        }
        if (event.getEventTypeId() != 0) {
            adaptorCategory.setCategory_index(event.getEventTypeId() - 1);
        }
    }

    private void saveEventData(Event event) {
        if (!etAddPrice.getText().toString().equals("")) {
            if (etAddPrice.getText().toString().endsWith(KEY_RUB)) {
                String s = etAddPrice.getText().toString().substring(0, etAddPrice.getText().length() - 1);
                event.setPrice(Integer.parseInt(s));
            } else {
                event.setPrice(Integer.parseInt(etAddPrice.getText().toString()));
            }
        }
        if (!etEventName.getText().toString().equals("")) {
            event.setTitle(etEventName.getText().toString());
        }
        if (!etEventDescription.getText().toString().equals("")) {
            event.setDescription(String.valueOf(etEventDescription.getText()));
        }
        if (!tvAddAddress.getText().equals("")) {
            event.setAddress(tvAddAddress.getText().toString());
        }
        if (beginDate != null && time != null) {
            event.setBegin(beginDate + time);
        }
        if (beginDate != null && time == null) {
            event.setBegin(beginDate);
        }
        if (beginDate == null && time != null) {
            event.setBegin(time);
        }
        if (endDate != null) {
            event.setEnd(endDate + defaultTime);
        }
    }

    private void selectImage() {
        final CharSequence[] items = {
                "Сделать фото",
                "Выбрать из галереи",
                "Отмена"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Сделать фото")) {
                easyImage.openCameraForImage(FragmentAddEvent.this);
            } else if (items[item].equals("Выбрать из галереи")) {
                easyImage.openGallery(FragmentAddEvent.this);
            } else if (items[item].equals("Отмена")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
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
        btnAddPhoto.setVisibility(View.GONE);
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


