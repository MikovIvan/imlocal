package ru.imlocal.imlocal.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;

import org.threeten.bp.LocalDate;

import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.decorators.DisableDaysDecorator;

import static ru.imlocal.imlocal.utils.Constants.FORMATTER;

public class FragmentCalendarDialog extends AppCompatDialogFragment implements OnDateSelectedListener, OnRangeSelectedListener {
    private LocalDate localDateStart;
    private LocalDate localDateEnd;
    private LocalDate localDateSingle;
    private String dateRange;
    private DatePickerDialogFragmentEvents dpdfe;

    void setDatePickerDialogFragmentEvents(DatePickerDialogFragmentEvents dpdfe) {
        this.dpdfe = dpdfe;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_calendar, null);
        LocalDate instance = LocalDate.now();
        MaterialCalendarView materialCalendarView = view.findViewById(R.id.calendarView);
        materialCalendarView.addDecorator(new DisableDaysDecorator(instance));
        materialCalendarView.setDateTextAppearance(R.style.DateTextAppearance);
        materialCalendarView.setWeekDayTextAppearance(R.style.WeekDayTextAppearance);
        materialCalendarView.setLeftArrow(R.drawable.ic_arrow_back);
        materialCalendarView.setRightArrow(R.drawable.ic_arrow_forward);
        materialCalendarView.setSelectionColor(getResources().getColor(R.color.color_background_tab_button));
        materialCalendarView.setOnDateChangedListener(this);
        materialCalendarView.setOnRangeSelectedListener(this);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Выберете продолжительность акции")
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onDateSelected(
            @NonNull MaterialCalendarView widget,
            @NonNull CalendarDay calendarDay,
            boolean selected) {
        localDateSingle = calendarDay.getDate();
        localDateEnd = null;
        dpdfe.onDateSelected(FORMATTER.format(localDateSingle), localDateSingle, localDateSingle);
    }

    @Override
    public void onRangeSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull List<CalendarDay> list) {
        localDateStart = list.get(0).getDate();
        localDateEnd = list.get(list.size() - 1).getDate();
        dateRange = "c " + FORMATTER.format(localDateStart) + " по " + FORMATTER.format(localDateEnd);
        dpdfe.onDateSelected(dateRange, localDateStart, localDateEnd);
    }

    public interface DatePickerDialogFragmentEvents {
        void onDateSelected(String date, LocalDate start, LocalDate end);
    }
}
