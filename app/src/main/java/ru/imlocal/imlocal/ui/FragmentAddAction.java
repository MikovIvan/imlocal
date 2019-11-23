package ru.imlocal.imlocal.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
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
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.ActionPhoto;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.PreferenceUtils;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.ui.FragmentBusiness.shopListBusiness;
import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER4;
import static ru.imlocal.imlocal.utils.Constants.STATUS_UPDATE;

public class FragmentAddAction extends Fragment implements RecyclerViewAdapterPhotos.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener, FragmentCalendarDialog.DatePickerDialogFragmentEvents {

    private static final int PERMISSIONS_REQUEST_CODE = 7459;

    private RecyclerView rvCategory;
    private TextView tvDatePicker;
    private Button btnAddPhoto;

    private RecyclerView rvPhotos;
    private RecyclerViewAdapterPhotos adapterPhotos;
    private RecyclerViewAdaptorCategory adaptorCategory;
    private List<String> photosPathList = new ArrayList<>();
    private List<String> photosIdList = new ArrayList<>();
    private ArrayList<String> photosDeleteList = new ArrayList<>();
    private ArrayList<MediaFile> photos = new ArrayList<>();
    private EasyImage easyImage;

    private Action action = new Action(-1, -1, "", "", "", "", -1, null);

    private TextInputEditText etActionName;
    private TextInputEditText etActionDescription;

    private MaterialSpinner spinner;

    private List<String> shopsName = new ArrayList<>();

    private Bundle bundle;
    private String update = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_action, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_background)));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        if (PreferenceUtils.getPhotoList(getActivity()) != null && !PreferenceUtils.getPhotoList(getActivity()).isEmpty()) {
            photos.clear();
            photos.addAll(PreferenceUtils.getPhotoList(getActivity()));
        }

        action.setCreatorId(Integer.parseInt(user.getId()));

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

        etActionName = view.findViewById(R.id.et_add_action_enter_name);
        etActionDescription = view.findViewById(R.id.et_add_action_full_description);

        bundle = getArguments();
        if (bundle != null) {
            update = bundle.getString("update");
            photosPathList.clear();
            action = (Action) bundle.getSerializable("action");
            loadActionData(action);
            List<String> photos = new ArrayList<>();
            for (ActionPhoto actionPhoto : action.getActionPhotos()) {
                photos.add(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + actionPhoto.getActionPhoto());
                photosIdList.add(String.valueOf(actionPhoto.getId()));
            }
            photosPathList.addAll(photos);
            if (photosPathList.size() < 3) {
                btnAddPhoto.setVisibility(View.VISIBLE);
            } else {
                btnAddPhoto.setVisibility(View.GONE);
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
        if (PreferenceUtils.getAction(getActivity()) != null) {
            action = PreferenceUtils.getAction(getContext());
            loadActionData(action);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!update.equals(STATUS_UPDATE)) {
            saveActionData(action);
            PreferenceUtils.saveAction(action, getActivity());
            PreferenceUtils.savePhotoList(photos, getActivity());
        }
        Utils.hideKeyboardFrom(getContext(), getView());
    }

    private void initDatePicker(View view) {
        tvDatePicker = view.findViewById(R.id.tv_add_action_select_date);
        tvDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentCalendarDialog calendarDialogFragment = new FragmentCalendarDialog();
                calendarDialogFragment.setDatePickerDialogFragmentEvents(FragmentAddAction.this);
                calendarDialogFragment.show(getActivity().getSupportFragmentManager(), "calendarDialog");
            }
        });
    }

    private void initSpinner(View view) {
        for (Shop shop : shopListBusiness) {
            shopsName.add(shop.getShopShortName());
        }

        spinner = view.findViewById(R.id.spinner_add_action_choose_place);
        if (!shopsName.isEmpty()) {
            spinner.setItems(shopsName);
        } else {
            spinner.setHint("У Вас нет мест");
        }
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                action.setActionOwnerId(shopListBusiness.get(position).getShopId());
                action.setShop(shopListBusiness.get(position));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_business, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.go_to_preview) {
            if (!etActionName.getText().toString().equals("") && etActionName.getText().length() <= 50) {
                action.setTitle(String.valueOf(etActionName.getText()));
            } else {
                Snackbar.make(getView(), "Название неправильное", Snackbar.LENGTH_LONG).show();
            }
            if (!etActionDescription.getText().toString().equals("")) {
                action.setFullDesc(String.valueOf(etActionDescription.getText()));
            } else {
                Snackbar.make(getView(), "Введите описание акции", Snackbar.LENGTH_LONG).show();
            }
            if (action.getActionTypeId() == -1) {
                Snackbar.make(getView(), "Выберите категорию", Snackbar.LENGTH_LONG).show();
            }
            if (action.getBegin().equals("")) {
                Snackbar.make(getView(), "Выберите даты акции", Snackbar.LENGTH_LONG).show();
            }
            if (action.getShop() == null) {
                Snackbar.make(getView(), "Выберите место", Snackbar.LENGTH_LONG).show();
            }
            if (photos.isEmpty() && photosPathList.isEmpty()) {
                Snackbar.make(getView(), "Прикрепите фотографию", Snackbar.LENGTH_LONG).show();
            }

            if (action.getActionOwnerId() != -1 && action.getActionTypeId() != -1 && action.getTitle().length() > 0
                    && action.getFullDesc().length() > 0 && action.getCreatorId() != -1 && action.getShop() != null
                    && !photos.isEmpty()
                    && !action.getBegin().equals("")) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("action", action);
                bundle.putParcelableArrayList("photos", photos);
                bundle.putStringArrayList("photoId", photosDeleteList);
                ((MainActivity) getActivity()).openVitrinaAction(bundle);
            } else if (action.getActionOwnerId() != -1 && action.getActionTypeId() != -1 && action.getTitle().length() > 0
                    && action.getFullDesc().length() > 0 && action.getCreatorId() != -1 && action.getShop() != null
                    && !photosPathList.isEmpty()
                    && !action.getBegin().equals("")) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("action", action);
                bundle.putParcelableArrayList("photos", photos);
                bundle.putStringArrayList("photoId", photosDeleteList);
                bundle.putString("update", STATUS_UPDATE);
                ((MainActivity) getActivity()).openVitrinaAction(bundle);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClickCategory(int position) {
        action.setActionTypeId(position + 1);
        adaptorCategory.notifyDataSetChanged();
    }

    private void initRvCategory(View view) {
        rvCategory = view.findViewById(R.id.rv_category);
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "add_action");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);
    }

    @Override
    public void onDateSelected(String date, LocalDate start, LocalDate end) {
        tvDatePicker.setText(date);
        tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
        action.setBegin(FORMATTER4.format(start));
        action.setEnd(FORMATTER4.format(end));
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

        if (photos.size() < 3 && photosPathList.size() < 3) {
            btnAddPhoto.setVisibility(View.VISIBLE);
        }
    }

    private void loadActionData(Action action) {
        if (action.getShop() != null && action.getShop().getShopId() != -1) {
            String name = "";
            for (Shop shop : shopListBusiness) {
                if (shop.getShopId() == action.getShop().getShopId()) {
                    name = shop.getShopShortName();
                }
            }
            for (int i = 0; i < shopsName.size(); i++) {
                if (shopsName.get(i).contains(name)) {
                    spinner.setSelectedIndex(i);
                }
            }
        }
        if (!action.getBegin().equals("")) {
            if (action.getBegin().equals(action.getEnd())) {
                tvDatePicker.setText(action.getBegin());
            } else {
                tvDatePicker.setText("c " + action.getBegin() + " по " + action.getEnd());
            }
            tvDatePicker.setTextColor(getResources().getColor(R.color.color_text));
        }
        if (!action.getTitle().equals("")) {
            etActionName.setText(action.getTitle());
        }
        if (!action.getFullDesc().equals("")) {
            etActionDescription.setText(action.getFullDesc());
        }
        if (action.getActionTypeId() != 0) {
            adaptorCategory.setCategory_index(action.getActionTypeId() - 1);
        }
    }

    private void saveActionData(Action action) {
        if (!etActionName.getText().toString().equals("")) {
            action.setTitle(etActionName.getText().toString());
        }
        if (!etActionDescription.getText().toString().equals("")) {
            action.setFullDesc(etActionDescription.getText().toString());
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
                easyImage.openCameraForImage(FragmentAddAction.this);
            } else if (items[item].equals("Выбрать из галереи")) {
                easyImage.openGallery(FragmentAddAction.this);
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
        if (photos.size() >= 3) {
            btnAddPhoto.setVisibility(View.GONE);
        }
        if (adapterPhotos.getItemCount() >= 3) {
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
