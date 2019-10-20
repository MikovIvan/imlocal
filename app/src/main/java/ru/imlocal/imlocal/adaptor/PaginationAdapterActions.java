package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.yandex.mapkit.geometry.Geo;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.ui.FragmentListActions;
import ru.imlocal.imlocal.utils.PaginationAdapterCallback;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.favoritesActions;
import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;

public class PaginationAdapterActions extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;
    private String errorMsg;

    private List<Action> dataActions;
    private List<Action> dataActionsFiltered;
    private Context context;
    private PaginationAdapterActions.OnItemClickListener mListener;

    public PaginationAdapterActions(List<Action> dataActions, Context context, FragmentListActions fragmentListActions) {
        this.context = context;
        this.mCallback = fragmentListActions;
        this.dataActions = dataActions;
        this.dataActionsFiltered = dataActions;
    }

    public List<Action> getActions() {
        return dataActionsFiltered;
    }

    public void setActions(List<Action> dataActions) {
        this.dataActions = dataActions;
    }

    public void setOnItemClickListener(PaginationAdapterActions.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.list_item_action, parent, false);
                viewHolder = new ActionVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Action action = dataActionsFiltered.get(position); // Movie

        switch (getItemViewType(position)) {
            case ITEM:
                ActionVH actionVH = (ActionVH) holder;
                if (action != null) {
                    if (favoritesActions.containsKey(action.getId())) {
                        actionVH.ibAddToFavorites.setImageResource(R.drawable.ic_heart_pressed);
                    } else {
                        actionVH.ibAddToFavorites.setImageResource(R.drawable.ic_heart);
                    }

                    if (action.getShop() != null) {
                        if (action.getShop().getShopShortName() != null) {
                            actionVH.tvShopTitle.setText(action.getShop().getShopShortName());
                            Picasso.get().load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + action.getShop().getShopPhotoArray().get(0).getShopPhoto())
                                    .placeholder(R.drawable.placeholder)
                                    .into(actionVH.ivIcon);
                            actionVH.tvShopRating.setText(String.valueOf(action.getShop().getShopAvgRating()));
                        }
                    } else {
                        actionVH.tvShopTitle.setText("Неверный id");
                        actionVH.ivIcon.setImageResource(R.drawable.testimg);
                        actionVH.tvShopRating.setText("000");
                    }

                    if (!action.getActionPhotos().isEmpty()) {
                        actionVH.tvEventTitle.setText(action.getTitle());
                        actionVH.tvEventAdress.setText(Utils.replaceString(action.getShop().getShopAddress().toString()));
                        actionVH.tvActionDescription.setText(action.getFullDesc());
                        actionVH.tvDate.setText(action.getBegin() + "-" + action.getEnd());
                        Picasso.get().load(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + action.getActionPhotos().get(0).getActionPhoto())
                                .placeholder(R.drawable.placeholder)
                                .into(actionVH.ivActionIcon);
                    } else {
                        actionVH.ivActionIcon.setVisibility(View.GONE);
                    }
                }
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataActionsFiltered == null ? 0 : dataActionsFiltered.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == dataActionsFiltered.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataActionsFiltered = dataActions;
                } else {
                    List<Action> filteredList = new ArrayList<>();
                    for (Action row : dataActions) {
                        if (row.getShop().getShopShortName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    dataActionsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataActionsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                dataActionsFiltered = (ArrayList<Action>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void filter(List<Action> copy, int i) {
        List<Action> filterList = new ArrayList<>();
        dataActionsFiltered.clear();
        dataActionsFiltered.addAll(copy);
        if (i != 0) {
            for (Action action : dataActionsFiltered) {
                if (action.getActionTypeId() == i) {
                    filterList.add(action);
                }
            }
            dataActionsFiltered.clear();
        }
        addAll(filterList);
        notifyDataSetChanged();
    }

    public void sortByDistance() {
        Collections.sort(dataActionsFiltered, (s1, s2) ->
                Double.compare(Geo.distance(new Point(s1.getShop().getShopAddress().getLatitude(), s1.getShop().getShopAddress().getLongitude()), new Point(latitude, longitude)),
                        Geo.distance(new Point(s2.getShop().getShopAddress().getLatitude(), s2.getShop().getShopAddress().getLongitude()), new Point(latitude, longitude))));
    }

    public void sortByRating() {
        Collections.sort(dataActionsFiltered, (s1, s2) -> Double.compare(s1.getShop().getShopAvgRating(), s2.getShop().getShopAvgRating()));
        Collections.reverse(dataActionsFiltered);
    }

    public void add(Action action) {
        dataActionsFiltered.add(action);
        notifyItemInserted(dataActionsFiltered.size() - 1);
    }

    public void addAll(List<Action> dataActions) {
        for (Action action : dataActions) {
            add(action);
        }
    }

    public void remove(Action action) {
        int position = dataActionsFiltered.indexOf(action);
        if (position > -1) {
            dataActionsFiltered.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
//        add(new Shop());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = dataActionsFiltered.size() - 1;
        Action action = getItem(position);

        if (action != null) {
            dataActionsFiltered.remove(position);
            notifyItemRemoved(position);
        }
    }

    private Action getItem(int position) {
        return dataActionsFiltered.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(dataActionsFiltered.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemShare(int position);

        void onItemAddToFavorites(int position, ImageButton imageButton);
    }

    class ActionVH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvEventTitle;
        TextView tvEventAdress;
        ImageView ivActionIcon;
        TextView tvShopTitle;
        TextView tvActionDescription;
        TextView tvDate;
        TextView tvShopRating;
        ImageButton ibShare;
        ImageButton ibAddToFavorites;

        ActionVH(View v) {
            super(v);
            tvShopTitle = v.findViewById(R.id.tv_shop_title);
            tvShopRating = v.findViewById(R.id.tv_shop_rating);
            tvEventTitle = v.findViewById(R.id.tv_action_title);
            tvEventAdress = v.findViewById(R.id.tv_shop_adress);
            tvActionDescription = v.findViewById(R.id.tv_action_description);
            tvDate = v.findViewById(R.id.tv_date);
            ibShare = v.findViewById(R.id.ib_share);
            ibAddToFavorites = v.findViewById(R.id.ib_add_to_favorites);
            ivIcon = v.findViewById(R.id.iv_icon);
            ivActionIcon = v.findViewById(R.id.iv_action_icon);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });

            ibShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemShare(position);
                        }
                    }
                }
            });

            ibAddToFavorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemAddToFavorites(position, ibAddToFavorites);
                        }
                    }
                }
            });
        }
    }
}
