package ru.imlocal.imlocal.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.suke.widget.SwitchButton;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEvent;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.decorators.DisableDaysDecorator;
import ru.imlocal.imlocal.entity.Event;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.MainActivity.showLoadingIndicator;

public class FragmentListEvents extends Fragment implements View.OnClickListener, RecyclerViewAdapterEvent.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM");
    private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RecyclerView recyclerView;
    private RecyclerView rvCategory;
    private RecyclerViewAdapterEvent adapter;
    private SwitchButton sbFreeEvents;
    private TextView tvDatePicker;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> copyList = new ArrayList<>();
    private boolean isShowFree;
    private int category = 0;
    private ConstraintLayout constraintCalendar;
    private ConstraintLayout constraintMain;
    private TextView tvReady;
    private TextView tvClear;
    private MaterialCalendarView materialCalendarView;
    private LocalDate localDateStart;
    private LocalDate localDateEnd;
    private LocalDate localDateSingle;
    private String dateRange;
    private LocalDate instance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_list_events, container, false);
        appBarLayout.setVisibility(View.VISIBLE);
        showLoadingIndicator(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getAllEvents();
        instance = LocalDate.now();

        initView(view);
        initRV();
        initCalendarListeners();
        setSwitchBtnListener();
        setOnClickListeners();

        setTodayToDatePicker();
        return view;
    }

    private void setSwitchBtnListener() {
        sbFreeEvents.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                isShowFree = isChecked;
                List<Event> filterList = new ArrayList<>();
                filter(filterList, category);
                Toast.makeText(getActivity(), "Отслеживание переключения: " + (isChecked ? "on" : "off"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setOnClickListeners() {
        tvDatePicker.setOnClickListener(this);
        tvReady.setOnClickListener(this);
        tvClear.setOnClickListener(this);
    }

    private void initRV() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext());
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.rv_fragment_list_events);
        rvCategory = view.findViewById(R.id.rv_category);
        sbFreeEvents = view.findViewById(R.id.switch_free);
        tvDatePicker = view.findViewById(R.id.tv_date_picker);
        constraintCalendar = view.findViewById(R.id.constraint_calendar);
        constraintMain = view.findViewById(R.id.main_constraint);
        tvReady = view.findViewById(R.id.tv_ready);
        tvClear = view.findViewById(R.id.tv_clear);
        materialCalendarView = view.findViewById(R.id.calendarView);
    }

    private void initCalendarListeners() {
        materialCalendarView.addDecorator(new DisableDaysDecorator(instance));
        materialCalendarView.setDateTextAppearance(R.style.DateTextAppearance);
        materialCalendarView.setWeekDayTextAppearance(R.style.WeekDayTextAppearance);
        materialCalendarView.setLeftArrow(R.drawable.ic_arrow_back);
        materialCalendarView.setRightArrow(R.drawable.ic_arrow_forward);
        materialCalendarView.setSelectionColor(getResources().getColor(R.color.color_background_tab_button));
        materialCalendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull List<CalendarDay> list) {
                localDateStart = list.get(0).getDate();
                localDateEnd = list.get(list.size() - 1).getDate();
                dateRange = "c " + FORMATTER.format(localDateStart) + " по " + FORMATTER.format(localDateEnd);
                Log.d("TAG", list.toString());
            }
        });
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                localDateSingle = calendarDay.getDate();
                localDateEnd = null;
                Log.d("TAG", calendarDay.toString());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(int position) {
        Event event = eventList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        ((MainActivity) getActivity()).openVitrinaEvent(bundle);
    }

    @Override
    public void onItemClickCategory(int position) {
        List<Event> filterList = new ArrayList<>();
        switch (position) {
            case 0:
                category = 1;
                Toast.makeText(getContext(), "Еда", Toast.LENGTH_SHORT).show();
                filter(filterList, category);
                break;
            case 1:
                category = 2;
                Toast.makeText(getContext(), "Дети", Toast.LENGTH_SHORT).show();
                filter(filterList, category);
                break;
            case 2:
                category = 3;
                Toast.makeText(getContext(), "Фитнес", Toast.LENGTH_SHORT).show();
                filter(filterList, category);
                break;
            case 3:
                category = 4;
                Toast.makeText(getContext(), "Красота", Toast.LENGTH_SHORT).show();
                filter(filterList, category);
                break;
            case 4:
                category = 5;
                Toast.makeText(getContext(), "Покупки", Toast.LENGTH_SHORT).show();
                filter(filterList, category);
                break;
            case 5:
                category = 0;
                Toast.makeText(getContext(), "Все", Toast.LENGTH_SHORT).show();
                filter(filterList, category);
                break;
        }
    }

    @SuppressLint("CheckResult")
    private void getAllEvents() {
        api.getAllEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Event>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d("TAG", "onsub");
                    }

                    @Override
                    public void onNext(List<Event> events) {
                        Log.d("TAG", "onnext");
                        eventList.clear();
                        copyList.clear();
                        eventList.addAll(events);
                        copyList.addAll(events);
                        displayData(eventList);
                        showLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("TAG", "onsub");

                    }

                    @Override
                    public void onComplete() {
                        Log.d("TAG", "oncomplete");

                    }
                });
    }

    private void displayData(List<Event> events) {
        adapter = new RecyclerViewAdapterEvent(events, getContext());
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void filter(List<Event> filterList, int category) {
        eventList.clear();
        eventList.addAll(copyList);
        if (localDateEnd != null) {
            if (isShowFree && category != 0) {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))
                            && event.getPrice() == 0 && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else if (isShowFree) {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))
                            && event.getPrice() == 0) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else if (category != 0) {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))
                            && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            }
        } else if (localDateStart != null) {
            if (isShowFree && category != 0) {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)
                            && event.getPrice() == 0 && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else if (isShowFree) {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)
                            && event.getPrice() == 0) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else if (category != 0) {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)
                            && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else {
                for (Event event : eventList) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            }
        } else {
            if (isShowFree && category != 0) {
                for (Event event : eventList) {
                    if (event.getPrice() == 0 && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else if (isShowFree) {
                for (Event event : eventList) {
                    if (event.getPrice() == 0) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else if (category != 0) {
                for (Event event : eventList) {
                    if (event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                eventList.clear();
                eventList.addAll(filterList);
            } else {
                eventList.clear();
                eventList.addAll(copyList);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        List<Event> filterList = new ArrayList<>();
        switch (view.getId()) {
            case R.id.tv_date_picker:
                TransitionManager.beginDelayedTransition(constraintMain);
                constraintCalendar.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_ready:
                if (localDateEnd != null) {
                    filter(filterList, category);
                    tvDatePicker.setText(dateRange);
                } else if (localDateStart != null) {
                    filter(filterList, category);
                    tvDatePicker.setText(FORMATTER.format(localDateSingle));
                } else {
                    setTodayToDatePicker();
                }
                TransitionManager.beginDelayedTransition(constraintMain);
                constraintCalendar.setVisibility(View.INVISIBLE);
                break;
            case R.id.tv_clear:
                materialCalendarView.clearSelection();
                localDateStart = null;
                localDateEnd = null;
                localDateSingle = null;
                setDefaultEventList();
                break;
        }
    }

    private void setDefaultEventList() {
        eventList.clear();
        eventList.addAll(copyList);
        adapter.notifyDataSetChanged();
    }

    private void setTodayToDatePicker() {
        materialCalendarView.setSelectedDate(instance);
        tvDatePicker.setText(FORMATTER.format(instance));
    }

}