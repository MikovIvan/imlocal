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

import pl.aprilapps.easyphotopicker.MediaFile;
import ru.imlocal.imlocal.R;

public class RecyclerViewAdapterPhotos extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final int VIEW_TYPE_FILE = 1;
    final int VIEW_TYPE_FILE_PATH = 2;

    private List<MediaFile> imagesFiles;
    private List<String> photoPaths;
    private Context context;
    private RecyclerViewAdapterPhotos.OnItemClickListener mListener;

    public RecyclerViewAdapterPhotos(List<MediaFile> imagesFiles, List<String> photoPaths, Context context) {
        this.imagesFiles = imagesFiles;
        this.context = context;
        this.photoPaths = photoPaths;
    }

    public void setOnItemClickListener(RecyclerViewAdapterPhotos.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_photos, parent, false);
        if (viewType == VIEW_TYPE_FILE) {
            return new ViewHolder1(v);
        }
        if (viewType == VIEW_TYPE_FILE_PATH) {
            return new ViewHolder2(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder1) {
            ((ViewHolder1) holder).populate1(imagesFiles.get(position - photoPaths.size()), position);
        }

        if (holder instanceof ViewHolder2) {
            ((ViewHolder2) holder).populate2(photoPaths.get(position), position);
        }

    }

    @Override
    public int getItemCount() {
        return imagesFiles.size() + photoPaths.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < photoPaths.size()) {
            return VIEW_TYPE_FILE_PATH;
        }
        if (position - photoPaths.size() < imagesFiles.size()) {
            return VIEW_TYPE_FILE;
        }
//        if (photoPaths != null && !photoPaths.isEmpty()) {
//            return 2;
//        }
//        if (imagesFiles != null && !imagesFiles.isEmpty()) {
//            return 1;
//        }
        return -1;
    }

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void add(MediaFile mediaFile) {
        imagesFiles.add(mediaFile);
        notifyItemInserted(imagesFiles.size() - 1);
    }

    public void addImageFile(List<MediaFile> mediaFileList) {
        for (MediaFile mediaFile : mediaFileList) {
            add(mediaFile);
        }
    }

    class ViewHolder1 extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton ibDelete;
        TextView tvAvatar;

        ViewHolder1(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_photos_icon);
            ibDelete = v.findViewById(R.id.ib_delete_photos);
            tvAvatar = v.findViewById(R.id.tv_avatar);

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

        public void populate1(MediaFile mediaFile, int position) {
            ivPhoto.setBackgroundColor(context.getResources().getColor(R.color.color_background));
            Picasso.get().load(mediaFile.getFile()).noPlaceholder().centerCrop().fit()
                    .into(ivPhoto);
            if (position == 0) {
                tvAvatar.setVisibility(View.VISIBLE);
            }
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        ImageButton ibDelete;
        TextView tvAvatar;

        ViewHolder2(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_photos_icon);
            ibDelete = v.findViewById(R.id.ib_delete_photos);
            tvAvatar = v.findViewById(R.id.tv_avatar);

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

        public void populate2(String mediaFile, int position) {
            ivPhoto.setBackgroundColor(context.getResources().getColor(R.color.color_background));
            Picasso.get().load(mediaFile).noPlaceholder().centerCrop().fit()
                    .into(ivPhoto);
            if (position == 0) {
                tvAvatar.setVisibility(View.VISIBLE);
            }
        }
    }
}
