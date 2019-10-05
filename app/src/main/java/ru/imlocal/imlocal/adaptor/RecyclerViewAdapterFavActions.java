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

import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;


public class RecyclerViewAdapterFavActions extends RecyclerView.Adapter<RecyclerViewAdapterFavActions.ViewHolder> implements Filterable {
    private List<Action> dataActions;
    private List<Action> dataActionsFiltered;
    private Context context;
    private OnItemClickListener mListener;
    public boolean full_show;

    public RecyclerViewAdapterFavActions(List<Action> dataActions, Context context) {
        this.dataActions = dataActions;
        this.dataActionsFiltered = dataActions;
        this.context = context;
        full_show = false;
    }

    public void setFullShow(boolean fact)
    {
        full_show = fact;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterFavActions.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_action, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterFavActions.ViewHolder holder, int position) {
        Action action = dataActionsFiltered.get(position);
        holder.ibAddToFavorites.setImageResource(R.drawable.ic_heart_pressed);

        if (action.getShop() != null) {
            if (action.getShop().getShopShortName() != null) {
                holder.tvShopTitle.setText(action.getShop().getShopShortName());
            }
        } else {
            holder.tvShopTitle.setText("Неверный id");
        }

        if (!action.getActionPhotos().isEmpty()) {
            holder.tvEventTitle.setText(action.getTitle());
            holder.tvActionDescription.setText(action.getFullDesc());
            Picasso.get().load(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + action.getActionPhotos().get(0).getActionPhoto())
                    .into(holder.ivActionIcon);
        } else {
            holder.ivActionIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (full_show) return dataActionsFiltered.size(); else return Math.min(dataActionsFiltered.size(), 2);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemAddToFavorites(int position, ImageButton imageButton);
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

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle;
        ImageView ivActionIcon;
        TextView tvShopTitle;
        TextView tvActionDescription;
        ImageButton ibAddToFavorites;


        ViewHolder(View v) {
            super(v);
            tvShopTitle = v.findViewById(R.id.tv_shop_title);
            tvEventTitle = v.findViewById(R.id.tv_action_title);
            tvActionDescription = v.findViewById(R.id.tv_action_description);
            ibAddToFavorites = v.findViewById(R.id.ib_add_to_favorites);
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
