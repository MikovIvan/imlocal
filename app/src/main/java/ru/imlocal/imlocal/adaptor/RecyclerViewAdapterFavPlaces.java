package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Shop;
import ru.imlocal.imlocal.utils.Utils;

import static ru.imlocal.imlocal.MainActivity.latitude;
import static ru.imlocal.imlocal.MainActivity.longitude;
import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;


public class RecyclerViewAdapterFavPlaces extends RecyclerView.Adapter<RecyclerViewAdapterFavPlaces.ViewHolder> {
    private List<Shop> dataPlaces;
    private Context context;
    private OnItemClickListener mListener;


    public RecyclerViewAdapterFavPlaces(List<Shop> dataPlaces, Context context) {
        this.dataPlaces = dataPlaces;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterFavPlaces.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_place, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterFavPlaces.ViewHolder holder, int position) {
        Shop place = dataPlaces.get(position);
        holder.ibAddToFavorites.setImageResource(R.drawable.ic_heart_pressed);

        if (place.getShopShortName() != null) {
            holder.tvPlaceTitle.setText(place.getShopShortName());
        }
        String distancestr = Utils.getDistanceInList(place.getShopAddress().getLatitude(), place.getShopAddress().getLongitude(), latitude, longitude);
        holder.tvDistance.setText(distancestr);

        if (!place.getShopPhotoArray().isEmpty()) {
            Picasso.with(context).load(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + place.getShopPhotoArray().get(0).getShopPhoto()).into(holder.ivPlaceIcon);
        } else {
            holder.ivPlaceIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataPlaces.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemShare(int position);

        void onItemAddToFavorites(int position, ImageButton imageButton);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaceTitle;
        ImageView ivPlaceIcon;
        TextView tvDistance;
        ImageButton ibAddToFavorites;


        ViewHolder(View v) {
            super(v);
            tvPlaceTitle = v.findViewById(R.id.tv_place_title);
            tvDistance = v.findViewById(R.id.distance_text);
            ibAddToFavorites = v.findViewById(R.id.ib_add_to_favorites);
            ivPlaceIcon = v.findViewById(R.id.iv_place_icon);

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
