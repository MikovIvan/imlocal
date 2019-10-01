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
import ru.imlocal.imlocal.entity.Action;

import static ru.imlocal.imlocal.utils.Constants.ACTION_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;

public class RecyclerViewAdapterActionsBusiness extends RecyclerView.Adapter<RecyclerViewAdapterActionsBusiness.ViewHolder> {
    private List<Action> dataActions;
    private Context context;
    private OnItemClickListener mListener;

    public RecyclerViewAdapterActionsBusiness(List<Action> dataActions, Context context) {
        this.dataActions = dataActions;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterActionsBusiness.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action_business, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterActionsBusiness.ViewHolder holder, int position) {
        Action action = dataActions.get(position);
        Picasso.get().load(BASE_IMAGE_URL + ACTION_IMAGE_DIRECTION + action.getActionPhotos().get(0).getActionPhoto())
                .into(holder.ivActionIcon);
        holder.tvEventTitle.setText(action.getTitle());
        holder.tvActionDescription.setText(action.getFullDesc());
    }

    @Override
    public int getItemCount() {
        return dataActions.size();
    }

    public interface OnItemClickListener {
        void onEditClick(int position);

        void onDeleteClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvEventTitle;
        ImageView ivActionIcon;
        TextView tvActionDescription;
        ImageButton ibEdit;
        ImageButton ibDelete;

        ViewHolder(View v) {
            super(v);
            tvEventTitle = v.findViewById(R.id.tv_action_title_business);
            tvActionDescription = v.findViewById(R.id.tv_action_description_business);
            ivActionIcon = v.findViewById(R.id.iv_action_icon_business);

            ibEdit = v.findViewById(R.id.ib_edit_business);
            ibDelete = v.findViewById(R.id.ib_delete_business);


            ibEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onEditClick(position);
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
                            mListener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}
