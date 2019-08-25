package ru.imlocal.imlocal.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;

import java.util.List;

import ru.imlocal.imlocal.R;


public class FragmentDialog extends DialogFragment implements View.OnClickListener {

    MaterialCalendarView materialCalendarView;
    Button btnAccept, btnCancel;
    private CalendarDay calendarDay1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle("Title!");
        View view = inflater.inflate(R.layout.fragment_calendar, null);
        materialCalendarView = view.findViewById(R.id.calendarView);
        btnAccept = view.findViewById(R.id.btn_accept);
        btnCancel = view.findViewById(R.id.btn_cancel);

        ;

        btnAccept.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        materialCalendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull List<CalendarDay> list) {
                Log.d("TAG", list.toString());
            }
        });
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                calendarDay1 = calendarDay;
                Log.d("TAG", calendarDay.toString());
            }
        });
        return view;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("TAG", "Dialog 1: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d("TAG", "Dialog 1: onCancel");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_accept:
                Intent intent = new Intent();
                intent.putExtra("date", calendarDay1.toString());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

}
