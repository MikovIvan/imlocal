package ru.imlocal.imlocal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.user;

public class FragmentFeedback extends Fragment {

    private Button btnSendFeedback;
    private TextInputEditText etTitle;
    private TextInputEditText etEmail;
    private TextInputEditText etMessage;
    private TextInputLayout errorInputLayoutEmail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, null);
        ((MainActivity) getActivity()).enableUpButtonViews(true);

        initView(view);

        etMessage.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etMessage.setRawInputType(InputType.TYPE_CLASS_TEXT);

        if (user.isLogin()) {
            etEmail.setText(user.getEmail());
        }

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Utils.isValidEmail(charSequence)) {
                    errorInputLayoutEmail.setError(null);
                } else {
                    errorInputLayoutEmail.setError("Неправильно указан email");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    errorInputLayoutEmail.setError(null);
                }
            }
        });

        btnSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(etEmail, etTitle, etMessage);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void sendEmail(TextInputEditText etEmail, TextInputEditText etTitle, TextInputEditText etMessage) {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[]{getActivity().getResources().getString(R.string.feedback_email)});
        Email.putExtra(Intent.EXTRA_SUBJECT, etTitle.getText().toString());
        Email.putExtra(Intent.EXTRA_TEXT, etMessage.getText().toString());
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    private void initView(View view) {
        btnSendFeedback = view.findViewById(R.id.btn_send_feedback);
        etTitle = view.findViewById(R.id.et_feedback_title);
        etEmail = view.findViewById(R.id.et_feedback_email);
        etMessage = view.findViewById(R.id.et_feedback_message);
        errorInputLayoutEmail = view.findViewById(R.id.textInputLayout2);
    }
}
