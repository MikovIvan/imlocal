package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Event;

import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.EVENT_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat;

public class RecyclerViewAdapterEventsBusiness extends RecyclerView.Adapter<RecyclerViewAdapterEventsBusiness.ViewHolder> {
    private List<Event> dataEvents;
    private Context context;
    private RecyclerViewAdapterEventsBusiness.OnItemClickListener mListener;

    public RecyclerViewAdapterEventsBusiness(Context context) {
        this.dataEvents = new ArrayList<>();
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterEventsBusiness.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event_business, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterEventsBusiness.ViewHolder holder, int position) {
        Event event = dataEvents.get(position);

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
        return dataEvents.size();
    }

    public interface OnItemClickListener {
        void onEventClick(int position);
    }

    public void setData(List<Event> events) {
        dataEvents.clear();
        dataEvents.addAll(events);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventImage;
        TextView tvEventTitle;
        TextView tvEventDate;

        ViewHolder(View v) {
            super(v);
            ivEventImage = v.findViewById(R.id.iv_event_image_business);
            tvEventTitle = v.findViewById(R.id.tv_event_title_business);
            tvEventDate = v.findViewById(R.id.tv_event_date_business);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onEventClick(position);
                        }
                    }
                }
            });
        }
    }
}
