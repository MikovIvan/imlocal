package ru.imlocal.imlocal.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.imlocal.imlocal.MainActivity;
import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.adaptor.PaginationAdapterActions;
import ru.imlocal.imlocal.adaptor.RecyclerViewAdaptorCategory;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.utils.PaginationAdapterCallback;
import ru.imlocal.imlocal.utils.PaginationScrollListener;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.api;
import static ru.imlocal.imlocal.MainActivity.appBarLayout;
import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.MainActivity.user;
import static ru.imlocal.imlocal.utils.Constants.Kind;
import static ru.imlocal.imlocal.utils.Utils.addToFavorites;
import static ru.imlocal.imlocal.utils.Utils.removeFromFavorites;

public class FragmentListActions extends Fragment implements PaginationAdapterCallback, MenuItem.OnActionExpandListener, SearchView.OnQueryTextListener, RecyclerViewAdaptorCategory.OnItemCategoryClickListener, PaginationAdapterActions.OnItemClickListener {

    private List<Action> actionList = new ArrayList<>();
    private List<Action> copyList = new ArrayList<>();

    private RecyclerView rvActions, rvCategory;
    private static final int PAGE_START = 1;
    private static int CATEGORY = 0;
    private static int TOTAL_PAGES = 2;
    private PaginationAdapterActions adapter;
    private RecyclerViewAdaptorCategory adaptorCategory;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private TextView txtError;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    private boolean isCategoryPressed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        View view = inflater.inflate(R.layout.fragment_list_actions, container, false);
        appBarLayout.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_toolbar_icon);

        rvActions = view.findViewById(R.id.rv_fragment_list_actions);
        rvCategory = view.findViewById(R.id.rv_category);

        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);

        linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rvActions.setLayoutManager(linearLayoutManager);
        rvActions.setItemAnimator(new DefaultItemAnimator());

        MaterialSpinner spinner = view.findViewById(R.id.spinner_sort);
        spinner.setItems("по рейтингу", "по удаленности");
        spinner.setSelectedIndex(1);
        spinner.setTextColor(getActivity().getResources().getColor(R.color.color_main));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                if (position == 0) {
                    sortByRating();
                } else {
                    sortByDistance();
                    Log.d("List", adapter.getActions().toString());
                    Log.d("List", "Coord: " + latitude + " " + longitude);
                }
                adapter.notifyDataSetChanged();
            }
        });

        rvCategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adaptorCategory = new RecyclerViewAdaptorCategory(getContext(), "action");
        rvCategory.setAdapter(adaptorCategory);
        adaptorCategory.setOnItemClickListener(this);

        rvActions.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
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

        loadFirstPage();
        btnRetry.setOnClickListener(view1 -> loadFirstPage());
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint("Введите название акции");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onItemClick(int position) {
        Action action = actionList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("action", action);
        ((MainActivity) getActivity()).openVitrinaAction(bundle);
    }

    @Override
    public void onItemShare(int position) {
        Action action = actionList.get(position);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, action.getTitle());
        send.putExtra(Intent.EXTRA_TEXT, action.getShop().getShopShortName() + " " + action.getShop().getShopWeb() + " " + action.getTitle() + " " + "https://imlocal.ru/events/" + action.getId());
        startActivity(Intent.createChooser(send, "Share using"));
    }

    @Override
    public void onItemAddToFavorites(int position, ImageButton imageButton) {
        if (user.isLogin()) {
            if (!favoritesActions.containsKey(actionList.get(position).getId())) {
                addToFavorites(user.getAccessToken(), Kind.event, actionList.get(position).getId(), user.getId());
                favoritesActions.put(actionList.get(position).getId(), actionList.get(position));
                imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_pressed));
                Snackbar.make(getView(), getResources().getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT).show();
            } else {
                removeFromFavorites(user.getAccessToken(), Kind.event, actionList.get(position).getId(), user.getId());
                favoritesActions.remove(actionList.get(position).getId());
                imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart));
                Snackbar.make(getView(), getResources().getString(R.string.delete_from_favorites), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(getView(), getResources().getString(R.string.need_login), Snackbar.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.login), Utils.setSnackbarOnClickListener(getActivity())).show();
        }
    }


    @Override
    public void onItemClickCategory(int position) {
        switch (position) {
            case 0:
                isCatPressed(1);
                break;
            case 1:
                isCatPressed(2);
                break;
            case 2:
                isCatPressed(3);
                break;
            case 3:
                isCatPressed(4);
                break;
            case 4:
                isCatPressed(5);
                break;
            case 5:
                isCatPressed(0);
                break;
        }
    }

    private void isCatPressed(int cat) {
        if (isCategoryPressed && CATEGORY == cat) {
            isCategoryPressed = false;
            CATEGORY = 0;
            filter(copyList, 0);
        } else {
            isCategoryPressed = true;
            CATEGORY = cat;
            filter(copyList, cat);
        }
        adaptorCategory.notifyDataSetChanged();
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    private void sortByRating() {
        adapter.sortByRating();
    }

    private void sortByDistance() {
        adapter.sortByDistance();
    }

    private void filter(List<Action> filterList, int i) {
        adapter.filter(filterList, i);
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    private void loadNextPage() {
        Log.d("loadNextPage", "loadNextPage: " + currentPage);
        callAllActions().enqueue(new Callback<List<Action>>() {
            @Override
            public void onResponse(Call<List<Action>> call, Response<List<Action>> response) {
                Log.d("GPS2", response.body().toString());
                Log.d("GPS2", response.toString());
                adapter.removeLoadingFooter();
                isLoading = false;

                List<Action> results = fetchResults(response);
                actionList.addAll(results);
                copyList.addAll(results);
                filter(copyList, CATEGORY);
                sortByDistance();

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<List<Action>> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private Call<List<Action>> callAllActions() {
        return api.getAllActions(currentPage, 10);
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
        callAllActions().enqueue(new Callback<List<Action>>() {
            @Override
            public void onResponse(Call<List<Action>> call, Response<List<Action>> response) {
                hideErrorView();
                Log.d("GPS2", response.toString());
                Log.d("GPS2", response.body().toString());
                if (response.headers().get("X-Pagination-Page-Count") == null) {
                    if (errorLayout.getVisibility() == View.GONE) {
                        errorLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        txtError.setText("нет акций около вас");
                    }
                } else {
                    TOTAL_PAGES = Integer.parseInt(response.headers().get("X-Pagination-Page-Count"));
                    List<Action> results = fetchResults(response);
                    actionList.clear();
                    copyList.clear();
                    actionList.addAll(results);
                    copyList.addAll(results);
                    progressBar.setVisibility(View.GONE);
                    displayData(actionList);
                    Log.d("List", actionList.toString());
                    Log.d("List", "Coord: " + latitude + " " + longitude);
                    sortByDistance();
                    Log.d("List", actionList.toString());
                    Log.d("List", "Coord: " + latitude + " " + longitude);
                    isLastPage = false;
                    if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<List<Action>> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private void displayData(List<Action> actionList) {
        adapter = new PaginationAdapterActions(actionList, getActivity(), FragmentListActions.this);
        rvActions.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private List<Action> fetchResults(Response<List<Action>> response) {
        return response.body();
    }
}
