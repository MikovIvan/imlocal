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

import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;

public class RecyclerViewAdapterActionsLight extends RecyclerView.Adapter<RecyclerViewAdapterActionsLight.ViewHolder> {
    private List<Action> dataActions;
    private Context context;
    private OnItemClickListener mListener;

    public RecyclerViewAdapterActionsLight(List<Action> dataActions, Context context) {
        this.dataActions = dataActions;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterActionsLight.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shop_offer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterActionsLight.ViewHolder holder, int position) {
        Action action = dataActions.get(position);
        if (!action.getActionPhotos().isEmpty()) {
            Picasso.get().load(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + action.getActionPhotos().get(0).getActionPhoto())
                    .into(holder.ivActionIcon);
        }
        holder.tvEventTitle.setText(action.getTitle());
        holder.tvActionDescription.setText(action.getFullDesc());
    }

    @Override
    public int getItemCount() {
        return dataActions.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventTitle;
        ImageView ivActionIcon;
        TextView tvActionDescription;

        ViewHolder(View v) {
            super(v);
            tvEventTitle = v.findViewById(R.id.tv_action_title);
            tvActionDescription = v.findViewById(R.id.tv_action_description);
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
