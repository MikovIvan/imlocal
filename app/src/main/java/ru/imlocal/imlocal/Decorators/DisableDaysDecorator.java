package ru.imlocal.imlocal.Decorators;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

public class DisableDaysDecorator implements DayViewDecorator {
    private LocalDate instance;

    public DisableDaysDecorator(LocalDate instance) {
        this.instance = instance;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.getDate().isBefore(instance);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setDaysDisabled(true);
    }
}

