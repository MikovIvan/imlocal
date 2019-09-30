package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.ui.FragmentListPlaces;
import ru.imlocal.imlocal.utils.PaginationAdapterCallback;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;
    private String errorMsg;

    private List<Shop> dataShops;
    private List<Shop> dataShopsFiltered;
    private Context context;
    private RecyclerViewAdapterShops.OnItemClickListener mListener;

    public PaginationAdapter(List<Shop> dataShops, Context context, FragmentListPlaces fragmentListPlaces) {
        this.context = context;
        this.mCallback = fragmentListPlaces;
        this.dataShops = dataShops;
        this.dataShopsFiltered = dataShops;
    }

    public List<Shop> getShops() {
        return dataShopsFiltered;
    }

    public void setShops(List<Shop> dataShops) {
        this.dataShops = dataShops;
    }

    public void setOnItemClickListener(RecyclerViewAdapterShops.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.list_item_shop, parent, false);
                viewHolder = new ShopVH(viewItem);
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
        Shop shop = dataShopsFiltered.get(position); // Movie

        switch (getItemViewType(position)) {
            case ITEM:
                ShopVH shopVH = (ShopVH) holder;
                if (shop != null) {
                    shopVH.tvShopTitle.setText(shop.getShopShortName());

//       для отображения описания магазина, чтобы соответсвовало макетам
                    shopVH.tvShopTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            shopVH.tvShopTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int linecount = shopVH.tvShopTitle.getLineCount();
                            switch (linecount) {
                                case 1:
                                    shopVH.tvShopDescription.setMaxLines(3);
                                    break;
                                case 2:
                                    shopVH.tvShopDescription.setMaxLines(2);
                                    break;
                                case 3:
                                    shopVH.tvShopDescription.setMaxLines(3);
                                    break;
                            }
                        }
                    });

                    if (shop.getShopPhotoArray() != null) {
                        Picasso.get().load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + shop.getShopPhotoArray().get(0).getShopPhoto())
                                .placeholder(R.drawable.placeholder)
                                .into(shopVH.ivShopIcon);
                    }
                    shopVH.tvShopDescription.setText(shop.getShopShortDescription());
                    shopVH.tvShopRating.setText(String.valueOf(shop.getShopAvgRating()));
                    if (latitude != 0 && longitude != 0) {
                        shopVH.tvDistance.setText(Utils.getDistanceInList(shop.getShopAddress().getLatitude(), shop.getShopAddress().getLongitude(), latitude, longitude));
                    } else {
                        shopVH.tvDistance.setText("");
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
        return dataShopsFiltered == null ? 0 : dataShopsFiltered.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == dataShopsFiltered.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    dataShopsFiltered = dataShops;
                } else {
                    List<Shop> filteredList = new ArrayList<>();
                    for (Shop row : dataShops) {
                        if (row.getShopShortName() != null) {
                            if (row.getShopShortName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }
                    }
                    dataShopsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataShopsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                dataShopsFiltered = (ArrayList<Shop>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void filter(List<Shop> copy, int i) {
        List<Shop> filterList = new ArrayList<>();
        dataShopsFiltered.clear();
        dataShopsFiltered.addAll(copy);
        if (i != 0) {
            for (Shop shop : dataShopsFiltered) {
                if (shop.getShopTypeId() == i) {
                    filterList.add(shop);
                }
            }
            dataShopsFiltered.clear();
        }
        addAll(filterList);
        notifyDataSetChanged();
    }

    public void sortByDistance() {
//        Collections.sort(dataShopsFiltered.subList(0,dataShopsFiltered.size()-1), (s1, s2) ->
        Collections.sort(dataShopsFiltered, (s1, s2) ->
                Double.compare(Geo.distance(new Point(s1.getShopAddress().getLatitude(), s1.getShopAddress().getLongitude()), new Point(latitude, longitude)),
                        Geo.distance(new Point(s2.getShopAddress().getLatitude(), s2.getShopAddress().getLongitude()), new Point(latitude, longitude))));
    }

    public void sortByRating() {
        Collections.sort(dataShopsFiltered, (s1, s2) -> Double.compare(s1.getShopAvgRating(), s2.getShopAvgRating()));
        Collections.reverse(dataShopsFiltered);
    }

    public void add(Shop shop) {
        dataShopsFiltered.add(shop);
        notifyItemInserted(dataShopsFiltered.size() - 1);
    }

    public void addAll(List<Shop> dataShops) {
        for (Shop shop : dataShops) {
            add(shop);
        }
    }

    public void remove(Shop shop) {
        int position = dataShopsFiltered.indexOf(shop);
        if (position > -1) {
            dataShopsFiltered.remove(position);
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

        int position = dataShopsFiltered.size() - 1;
        Shop shop = getItem(position);

        if (shop != null) {
            dataShopsFiltered.remove(position);
            notifyItemRemoved(position);
        }
    }

    private Shop getItem(int position) {
        return dataShopsFiltered.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(dataShopsFiltered.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
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

    class ShopVH extends RecyclerView.ViewHolder {
        TextView tvDistance;
        ImageView ivShopIcon;
        TextView tvShopTitle;
        TextView tvShopDescription;
        TextView tvShopRating;

        ShopVH(View v) {
            super(v);
            tvDistance = v.findViewById(R.id.tv_distance);
            ivShopIcon = v.findViewById(R.id.iv_shopimage);
            tvShopTitle = v.findViewById(R.id.tv_title);
            tvShopDescription = v.findViewById(R.id.tv_description);
            tvShopRating = v.findViewById(R.id.tv_rating);
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
        }
    }
}
