package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Shop;

import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.SHOP_IMAGE_DIRECTION;

public class RecyclerViewAdapterShopsBusiness extends RecyclerView.Adapter<RecyclerViewAdapterShopsBusiness.ViewHolder> {
    private List<Shop> dataActions;
    private Context context;
    private OnItemClickListener mListener;

    public RecyclerViewAdapterShopsBusiness(List<Shop> dataActions, Context context) {
        this.dataActions = dataActions;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterShopsBusiness.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shop_business, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterShopsBusiness.ViewHolder holder, int position) {
        Shop shop = dataActions.get(position);
        Picasso.get().load(BASE_IMAGE_URL + SHOP_IMAGE_DIRECTION + shop.getShopPhotoArray().get(0).getShopPhoto())
                .into(holder.ivShopIcon);
        holder.tvShopTitle.setText(shop.getShopShortName());
    }

    @Override
    public int getItemCount() {
        return dataActions.size();
    }

    public interface OnItemClickListener {
        void onEditShopClick(int position);

        void onDeleteShopClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvShopTitle;
        ImageView ivShopIcon;
        ImageButton ibEdit;
        ImageButton ibDelete;

        ViewHolder(View v) {
            super(v);
            tvShopTitle = v.findViewById(R.id.tv_shop_title_business);
            ivShopIcon = v.findViewById(R.id.iv_shop_icon_business);

            ibEdit = v.findViewById(R.id.ib_edit_business);
            ibDelete = v.findViewById(R.id.ib_delete_business);


            ibEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onEditShopClick(position);
                        }
                    }
                }
            });

            ibDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteShopClick(position);
                        }
                    }
                }
            });
        }
    }
}
