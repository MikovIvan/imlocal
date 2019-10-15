package ru.imlocal.imlocal.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import ru.imlocal.imlocal.R;

public class FragmentDeleteDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private String entity;
    private int position;
    private DeleteDialogFragment deleteDialogFragment;

    public FragmentDeleteDialog(String entity, int position) {
        this.entity = entity;
        this.position = position;
    }

    void setDeleteDialogFragment(DeleteDialogFragment deleteDialogFragment) {
        this.deleteDialogFragment = deleteDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog();
        View view = inflater.inflate(R.layout.dialog_delete, null);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_delete:
                deleteDialogFragment.onDeleted(entity, position);
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    public interface DeleteDialogFragment {
        void onDeleted(String entity, int position);
    }
}
