package ru.imlocal.imlocal.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;
import com.suke.widget.SwitchButton;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.PaginationAdapterEvents;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdapterEvent;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.decorators.DisableDaysDecorator;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.utils.PaginationAdapterCallback;
import ru.imlocal.imlocal.utils.PaginationScrollListener;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER;

public class FragmentListEvents extends Fragment implements View.OnClickListener, RecyclerViewAdapterEvent.OnItemClickListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener, PaginationAdapterCallback, PaginationAdapterEvents.OnItemClickListener {

    private RecyclerView recyclerView;
    //    private RecyclerViewAdapterEvent adapter;
    private SwitchButton sbFreeEvents;
    private TextView tvDatePicker;
    //    пока не будет апи
    public static List<Event> eventList = new ArrayList<>();
    private List<Event> copyList = new ArrayList<>();
    private RecyclerView rvEvents, rvCategory;
    private static final int PAGE_START = 1;
    private static int CATEGORY = 0;
    private static int TOTAL_PAGES = 2;
    private PaginationAdapterEvents adapter;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private TextView txtError;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;

    private boolean isShowFree;
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        instance = LocalDate.now();

        initView(view);

        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);

        linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rvEvents.setLayoutManager(linearLayoutManager);
        rvEvents.setItemAnimator(new DefaultItemAnimator());

        rvEvents.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        initRV();
        initCalendarListeners();
        setSwitchBtnListener();
        setOnClickListeners();

        setTodayToDatePicker();

        loadFirstPage();
        btnRetry.setOnClickListener(view1 -> loadFirstPage());
        return view;
    }

    private void setSwitchBtnListener() {
        sbFreeEvents.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                isShowFree = isChecked;
                filter(copyList, CATEGORY);
            }
        });
    }

    private void setOnClickListeners() {
        tvDatePicker.setOnClickListener(this);
        tvReady.setOnClickListener(this);
        tvClear.setOnClickListener(this);
    }

    private void initRV() {
        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdaptorCategory adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "event");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);
    }

    private void initView(View view) {
        rvEvents = view.findViewById(R.id.rv_fragment_list_events);
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
    public void onItemEventClick(int position) {
        Event event = eventList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        ((MainActivity) getActivity()).openVitrinaEvent(bundle);
    }

    @Override
    public void onItemClickCategory(int position) {
        switch (position) {
            case 0:
                CATEGORY = 1;
                filter(copyList, CATEGORY);
                break;
            case 1:
                CATEGORY = 2;
                filter(copyList, CATEGORY);
                break;
            case 2:
                CATEGORY = 3;
                filter(copyList, CATEGORY);
                break;
            case 3:
                CATEGORY = 4;
                filter(copyList, CATEGORY);
                break;
            case 4:
                CATEGORY = 7;
                filter(copyList, CATEGORY);
                break;
            case 5:
                CATEGORY = 8;
                filter(copyList, CATEGORY);
                break;
            case 6:
                CATEGORY = 5;
                filter(copyList, CATEGORY);
                break;
            case 7:
                CATEGORY = 6;
                filter(copyList, CATEGORY);
                break;
            case 8:
                CATEGORY = 0;
                filter(copyList, CATEGORY);
                break;
        }
    }

    private void filter(List<Event> filterList, int i) {
        adapter.filter(filterList, i, localDateStart, localDateEnd, localDateSingle, isShowFree);
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    private void loadNextPage() {
        Log.d("loadNextPage", "loadNextPage: " + currentPage);
        callAllEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                Log.d("GPS2", response.body().toString());
                Log.d("GPS2", response.toString());
                adapter.removeLoadingFooter();
                isLoading = false;

                List<Event> results = fetchResults(response);
                eventList.addAll(results);
                copyList.addAll(results);
                filter(copyList, CATEGORY);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private Call<List<Event>> callAllEvents() {
        return api.getAllEvents(currentPage, 10);
    }

    private void showErrorView(Throwable throwable) {
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);
        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }
        return errorMsg;
    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void loadFirstPage() {

        hideErrorView();
        currentPage = PAGE_START;
        callAllEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                hideErrorView();
                Log.d("GPS2", response.toString());
                Log.d("GPS2", response.body().toString());
                if (response.headers().get("X-Pagination-Page-Count") == null) {
                    if (errorLayout.getVisibility() == View.GONE) {
                        errorLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        txtError.setText("нет событий около вас");
                    }
                } else {
                    TOTAL_PAGES = Integer.parseInt(response.headers().get("X-Pagination-Page-Count"));
                    List<Event> results = fetchResults(response);
                    eventList.clear();
                    copyList.clear();
                    eventList.addAll(results);
                    copyList.addAll(results);
                    progressBar.setVisibility(View.GONE);
                    displayData(eventList);

                    isLastPage = false;
                    if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private void displayData(List<Event> events) {
        adapter = new PaginationAdapterEvents(events, getContext(), FragmentListEvents.this);
        rvEvents.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private List<Event> fetchResults(Response<List<Event>> response) {
        return response.body();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_date_picker:
                TransitionManager.beginDelayedTransition(constraintMain);
                constraintCalendar.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_ready:
                if (localDateEnd != null) {
                    filter(copyList, CATEGORY);
                    tvDatePicker.setText(dateRange);
                } else if (localDateStart != null) {
                    filter(copyList, CATEGORY);
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