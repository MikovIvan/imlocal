package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Action;
import ru.imlocal.imlocal.entity.Shop;

import static ru.imlocal.imlocal.utils.Utils.makeMap;


public class RecyclerViewAdapterActions extends RecyclerView.Adapter<RecyclerViewAdapterActions.ViewHolder> {
    private List<Action> dataActions;
    private List<Shop> dataShops;
    private Context context;
    private OnItemClickListener mListener;


    public RecyclerViewAdapterActions(List<Action> dataActions, List<Shop> dataShops, Context context) {
        this.dataActions = dataActions;
        this.dataShops = dataShops;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterActions.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterActions.ViewHolder holder, int position) {
        Action action = dataActions.get(position);
        Shop shop = makeMap(dataShops).get(action.getActionOwnerId());

        if (shop != null) {
            if (shop.getShopShortName() != null) {
                holder.tvShopTitle.setText(shop.getShopShortName());
                Picasso.with(context).load("https://imlocal.ru/img/shopPhoto/" + shop.getShopPhotoArray().get(0).getShopPhoto())
                        .into(holder.ivIcon);
                holder.tvShopRating.setText(String.valueOf(shop.getShopRating()));
            }
        } else {
            holder.tvShopTitle.setText("Неверный id");
            holder.ivIcon.setImageResource(R.drawable.testimg);
            holder.tvShopRating.setText("000");
        }

        if (!action.getActionPhotos().isEmpty()) {
            holder.tvEventTitle.setText(action.getTitle());
            holder.tvEventAdress.setText(action.getTitle());
            holder.tvActionDescription.setText(action.getFullDesc());
            holder.tvDate.setText(action.getBegin() + "-" + action.getEnd());
            Picasso.with(context).load("https://imlocal.ru/img/shopPhoto/" + action.getActionPhotos().get(0).getActionPhoto())
                    .into(holder.ivActionIcon);
        } else {
            holder.ivActionIcon.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return dataActions.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIcon;
        TextView tvEventTitle;
        TextView tvEventAdress;
        ImageView ivActionIcon;
        TextView tvShopTitle;
        TextView tvActionDescription;
        TextView tvDate;
        TextView tvShopRating;


        ViewHolder(View v) {
            super(v);
            tvShopTitle = v.findViewById(R.id.tv_shop_title);
            tvShopRating = v.findViewById(R.id.tv_shop_rating);
            tvEventTitle = v.findViewById(R.id.tv_action_title);
            tvEventAdress = v.findViewById(R.id.tv_shop_adress);
            tvActionDescription = v.findViewById(R.id.tv_action_description);
            tvDate = v.findViewById(R.id.tv_date);

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
        }
    }
}
