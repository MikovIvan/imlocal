package ru.imlocal.imlocal.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.threeten.bp.LocalDate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.imlocal.imlocal.BuildConfig;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterPhotos;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.FileCompressor;

import static android.app.Activity.RESULT_OK;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentListPlaces.shopList;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER4;
import static ru.imlocal.imlocal.utils.Utils.hideKeyboardFrom;

public class FragmentAddEvent extends Fragment implements RecyclerViewAdapterPhotos.OnItemClickListener, View.OnClickListener, FragmentAddressDialog.AddAddressFragmentAddressDialog, RecyclerViewAdaptorCategory.OnItemCategoryClickListener, FragmentCalendarDialog.DatePickerDialogFragmentEvents {

    private static final int REQUEST_GALLERY_PHOTO = 2;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private RecyclerView rvCategory;
    private TextView tvDatePicker;
    private TextView tvTimePicker;
    private TextView tvAddAddress;
    private EditText etAddPrice;
    private TextView tvAddFreePrice;
    private RecyclerView rvPhotos;
    private RecyclerViewAdapterPhotos adapterPhotos;
    private List<String> photosPathList = new ArrayList<>();
    private File mPhotoFile;
    private FileCompressor mCompressor;

    private Event event = new Event();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);

        initRvCategory(view);
        initSpinner(view);
        initDatePicker(view);
        initTimePicker(view);

        initSetPrice(view);

        if (photosPathList.isEmpty()) {
            photosPathList.add("add");
        }
        rvPhotos = view.findViewById(R.id.rv_add_photo);
        adapterPhotos = new RecyclerViewAdapterPhotos(photosPathList, getActivity());
        rvPhotos.setLayoutManager(new GridLayoutManager(getActivity(), 2, RecyclerView.VERTICAL, false));
        rvPhotos.setAdapter(adapterPhotos);
        adapterPhotos.setOnItemClickListener(this);

        return view;
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
                    String s = etAddPrice.getText().toString().concat(Constants.KEY_RUB);
                    etAddPrice.setText(s);
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
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "add_event");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);
    }

    private void initSpinner(View view) {
        //        это потом заменить на места юзера
        List<Shop> userShops = new ArrayList<>();
        List<String> shopsName = new ArrayList<>();
        for (Shop shop : shopList) {
            if (shop.getCreatorId().equals(user.getId())) {
                userShops.add(shop);
                shopsName.add(shop.getShopShortName());
            }
        }

        MaterialSpinner spinner = view.findViewById(R.id.spinner_add_event_choose_place);
        spinner.setItems(shopsName);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                event.setShopId(userShops.get(position).getShopId());
                event.setCreatorId(Integer.parseInt(user.getId()));
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
                        tvTimePicker.setText(selectedHour + ":" + selectedMinute);
                        tvTimePicker.setTextColor(getResources().getColor(R.color.color_text));
                    }
                }, hour, minute, true);
                timePicker.show();
            }
        });
    }

    @Override
    public void onItemClickCategory(int position) {
        event.setEventTypeId(position + 1);
    }

    @Override
    public void onDateSelected(String date, LocalDate start, LocalDate end) {
        tvDatePicker.setText(date);
        tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
        event.setBegin(FORMATTER4.format(start));
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
    public void onAddressSelected(String address) throws IOException {
        tvAddAddress.setText(address);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_business, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go_to_preview) {
//            if (!etActionName.getText().toString().equals("") && etActionName.getText().length() <= 30) {
//                action.setTitle(String.valueOf(etActionName.getText()));
//            } else {
//                Snackbar.make(getView(), "Название неправильное", Snackbar.LENGTH_LONG).show();
//            }
//            if (!etActionSubTitle.getText().toString().equals("") && etActionSubTitle.getText().length() <= 40) {
//                action.setShortDesc(String.valueOf(etActionSubTitle.getText()));
//            } else {
//                Snackbar.make(getView(), "Подзаголовок неправильный", Snackbar.LENGTH_LONG).show();
//            }
//            if (!etActionDescription.getText().toString().equals("")) {
//                action.setFullDesc(String.valueOf(etActionDescription.getText()));
//            } else {
//                Snackbar.make(getView(), "Введите описание акции", Snackbar.LENGTH_LONG).show();
//            }
//            if (action.getActionTypeId() == 0) {
//                Snackbar.make(getView(), "Выберите категорию", Snackbar.LENGTH_LONG).show();
//            }
//            if (action.getBegin().equals("") || action.getEnd().equals("")) {
//                Snackbar.make(getView(), "Выберите даты акции", Snackbar.LENGTH_LONG).show();
//            } else {
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("action", action);
//                bundle.putStringArrayList("photosPathList", (ArrayList<String>) photosPathList);
//                ((MainActivity) getActivity()).openVitrinaAction(bundle);
//            }

            Toast.makeText(getActivity(), "Go to preview", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
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
            if (photosPathList.size() == 2) {
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

        if (photosPathList.isEmpty()) {
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
