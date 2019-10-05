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
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.utils.Constants;

import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.EVENT_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat;

public class RecyclerViewAdapterFavEvents extends RecyclerView.Adapter<RecyclerViewAdapterFavEvents.ViewHolder> {
    private List<Event> dataEvents;
    private Context context;
    private OnItemClickListener mListener;
    public boolean full_show;

    public RecyclerViewAdapterFavEvents(List<Event> dataEvents, Context context) {
        this.dataEvents = dataEvents;
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
    public RecyclerViewAdapterFavEvents.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterFavEvents.ViewHolder holder, int position) {
        Event event = dataEvents.get(position);

        if (event.getPrice() == 0) {
            holder.tvEventPrice.setText(R.string.event_price_free);
        } else {
            holder.tvEventPrice.setText(event.getPrice() + Constants.KEY_RUB);
        }
        holder.tvEventDate.setText(newDateFormat(event.getBegin()));
        holder.tvEventTitle.setText(event.getTitle());
        if (event.getEventPhotoList().isEmpty()) {
            holder.ivEventImage.setImageResource(R.drawable.testimg);
        } else {
            Picasso.get().load(BASE_IMAGE_URL + EVENT_IMAGE_DIRECTION + event.getEventPhotoList().get(0).getEventPhoto())
                    .into(holder.ivEventImage);
        }
    }

    @Override
    public int getItemCount() {
        if (full_show) return dataEvents.size(); else return Math.min(dataEvents.size(), 2);
    }

    public interface OnItemClickListener {
        void onItemEventClick(int position);

        void onItemAddToFavorites(int position, ImageButton imageButton);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventImage;
        TextView tvEventPrice;
        TextView tvEventTitle;
        TextView tvEventDate;
        ImageButton ibAddToFavorites;

        ViewHolder(View v) {
            super(v);
            ivEventImage = v.findViewById(R.id.iv_event_image);
            tvEventPrice = v.findViewById(R.id.tv_event_price);
            tvEventTitle = v.findViewById(R.id.tv_event_title);
            tvEventDate = v.findViewById(R.id.tv_event_date);
            ibAddToFavorites = v.findViewById(R.id.ib_add_to_favorites);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemEventClick(position);
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
