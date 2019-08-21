package ru.imlocal.imlocal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;

public class FragmentFeedback extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, null);
        ((MainActivity) getActivity()).enableUpButtonViews(true);
        Button btnSendFeedback = view.findViewById(R.id.btn_send_feedback);
        TextInputEditText etTitle = view.findViewById(R.id.et_feedback_title);
        TextInputEditText etEmail = view.findViewById(R.id.et_feedback_email);
        TextInputEditText etMessage = view.findViewById(R.id.et_feedback_message);

        btnSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[]{etEmail.getText().toString()});
                Email.putExtra(Intent.EXTRA_SUBJECT, etTitle.getText().toString());
                Email.putExtra(Intent.EXTRA_TEXT, etMessage.getText().toString());
                startActivity(Intent.createChooser(Email, "Send Feedback:"));
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
