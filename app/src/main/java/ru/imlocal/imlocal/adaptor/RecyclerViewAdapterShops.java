package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;

public class RecyclerViewAdapterShops extends RecyclerView.Adapter<RecyclerViewAdapterShops.ViewHolder> implements Filterable {
    private List<Shop> dataShops;
    private List<Shop> dataShopsFiltered;
    private Context context;
    private OnItemClickListener mListener;

    public RecyclerViewAdapterShops(List<Shop> dataShops, Context context) {
        this.dataShops = dataShops;
        this.context = context;
        this.dataShopsFiltered = dataShops;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterShops.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shop, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterShops.ViewHolder holder, int position) {
        Shop shop = dataShopsFiltered.get(position);
        holder.tvShopTitle.setText(shop.getShopShortName());

//       для отображения описания магазина, чтобы соответсвовало макетам
        holder.tvShopTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.tvShopTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int linecount = holder.tvShopTitle.getLineCount();
                switch (linecount) {
                    case 1:
                        holder.tvShopDescription.setMaxLines(3);
                        break;
                    case 2:
                        holder.tvShopDescription.setMaxLines(2);
                        break;
                    case 3:
                        holder.tvShopDescription.setMaxLines(3);
                        break;
                }
            }
        });

        Picasso.with(context).load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + shop.getShopPhotoArray().get(0).getShopPhoto())
                .into(holder.ivShopIcon);
        holder.tvShopDescription.setText(shop.getShopShortDescription());
        holder.tvShopRating.setText(String.valueOf(shop.getShopAvgRating()));
        if (latitude != 0 && longitude != 0) {
            holder.tvDistance.setText(Utils.getDistanceInList(shop.getShopAddress().getLatitude(), shop.getShopAddress().getLongitude(), latitude, longitude));
        } else {
            holder.tvDistance.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return dataShopsFiltered.size();
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
                        if (row.getShopShortName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
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

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance;
        ImageView ivShopIcon;
        TextView tvShopTitle;
        TextView tvShopDescription;
        TextView tvShopRating;

        ViewHolder(View v) {
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
