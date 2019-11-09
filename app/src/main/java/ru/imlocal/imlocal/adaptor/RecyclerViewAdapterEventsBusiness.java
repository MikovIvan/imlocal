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
//    public RecyclerViewAdapterEventsBusiness(List<Event> dataEvents, Context context) {
//        this.dataEvents = dataEvents;
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
        void onEditEventClick(int position);

        void onDeleteEventClick(int position);
    }

    public void addNewEvent(Event event) {
        dataEvents.add(0, event);
        notifyItemInserted(0);
    }

    public void setData(List<Event> events) {
//        final int currentCount = dataEvents.size();
//        synchronized (dataEvents){
//            dataEvents.addAll(events);
//        }
//        if (Looper.getMainLooper() == Looper.myLooper()) {
//            notifyItemRangeInserted(currentCount, events.size());
//        } else {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    notifyItemRangeInserted(currentCount, events.size());
//                }
//            });
//        }
        dataEvents.clear();
        dataEvents.addAll(events);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventImage;
        TextView tvEventTitle;
        TextView tvEventDate;
        ImageButton ibEdit;
        ImageButton ibDelete;

        ViewHolder(View v) {
            super(v);
            ivEventImage = v.findViewById(R.id.iv_event_image_business);
            tvEventTitle = v.findViewById(R.id.tv_event_title_business);
            tvEventDate = v.findViewById(R.id.tv_event_date_business);

            ibEdit = v.findViewById(R.id.ib_edit_business);
            ibDelete = v.findViewById(R.id.ib_delete_business);


            ibEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onEditEventClick(position);
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
                            mListener.onDeleteEventClick(position);
                        }
                    }
                }
            });
        }
    }
}
