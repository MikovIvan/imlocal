package ru.imlocal.imlocal.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import ru.imlocal.imlocal.R;

public class FragmentDeleteDialog extends AppCompatDialogFragment implements View.OnClickListener {

    String text;

    private DeleteDialogFragment deleteDialogFragment;

    public FragmentDeleteDialog() {

    }

    void setDeleteDialogFragment(DeleteDialogFragment deleteDialogFragment, String text) {
        this.deleteDialogFragment = deleteDialogFragment;
        this.text = text;
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
        TextView tvTitle = view.findViewById(R.id.tv_delete_title);
        tvTitle.setText(text);

        btnDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_delete:
                deleteDialogFragment.onDeleted();
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    public interface DeleteDialogFragment {
        void onDeleted();
    }
}
