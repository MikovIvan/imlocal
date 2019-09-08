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
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.utils.Constants;

import static ru.imlocal.imlocal.utils.Utils.newDateFormat;

public class RecyclerViewAdapterEvent extends RecyclerView.Adapter<RecyclerViewAdapterEvent.ViewHolder> {
    private List<Event> dataEvents;
    private Context context;
    private OnItemClickListener mListener;


    public RecyclerViewAdapterEvent(List<Event> dataEvents, Context context) {
        this.dataEvents = dataEvents;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterEvent.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterEvent.ViewHolder holder, int position) {
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
            Picasso.with(context).load("https://www.yiilessons.xyz/img/happeningPhoto/" + event.getEventPhotoList().get(0).getEventPhoto())
                    .into(holder.ivEventImage);
        }
    }

    @Override
    public int getItemCount() {
        return dataEvents.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventImage;
        TextView tvEventPrice;
        TextView tvEventTitle;
        TextView tvEventDate;

        ViewHolder(View v) {
            super(v);
            ivEventImage = v.findViewById(R.id.iv_event_image);
            tvEventPrice = v.findViewById(R.id.tv_event_price);
            tvEventTitle = v.findViewById(R.id.tv_event_title);
            tvEventDate = v.findViewById(R.id.tv_event_date);

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
