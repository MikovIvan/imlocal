package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.imlocal.imlocal.R;

public class RecyclerViewAdapterPhotos extends RecyclerView.Adapter<RecyclerViewAdapterPhotos.ViewHolder> {
    private List<String> photosPath;
    private Context context;
    private RecyclerViewAdapterPhotos.OnItemClickListener mListener;

    public RecyclerViewAdapterPhotos(List<String> photosPath, Context context) {
        this.photosPath = photosPath;
        this.context = context;
    }

    public void setOnItemClickListener(RecyclerViewAdapterPhotos.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterPhotos.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_photos, parent, false);
        return new RecyclerViewAdapterPhotos.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterPhotos.ViewHolder holder, int position) {
        String imagePath = photosPath.get(position);
        if (!imagePath.equals("add")) {
            holder.ivPhoto.setBackgroundColor(context.getResources().getColor(R.color.color_background));
            Picasso.get().load(imagePath).noPlaceholder().centerCrop().fit()
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setBackgroundColor(context.getResources().getColor(R.color.color_background));
            Picasso.get().load(R.drawable.ic_add_a_photo_black_24dp).placeholder(R.drawable.ic_add_a_photo_black_24dp)
                    .into(holder.ivPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return photosPath.size();
    }

    public interface OnItemClickListener {
        void onDeleteClick(int position);

        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton ibDelete;

        ViewHolder(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_photos_icon);
            ibDelete = v.findViewById(R.id.ib_delete_photos);

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
