package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.threeten.bp.LocalDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Event;
import ru.imlocal.imlocal.ui.FragmentListEvents;
import ru.imlocal.imlocal.utils.Constants;
import ru.imlocal.imlocal.utils.PaginationAdapterCallback;

import static ru.imlocal.imlocal.utils.Constants.BASE_IMAGE_URL;
import static ru.imlocal.imlocal.utils.Constants.EVENT_IMAGE_DIRECTION;
import static ru.imlocal.imlocal.utils.Constants.FORMATTER2;
import static ru.imlocal.imlocal.utils.Constants.SIMPLE_DATE_FORMAT;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat;
import static ru.imlocal.imlocal.utils.Utils.newDateFormat2;

public class PaginationAdapterEvents extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;
    private String errorMsg;

    private List<Event> dataEvents;
    private Context context;
    private PaginationAdapterEvents.OnItemClickListener mListener;

    public PaginationAdapterEvents(List<Event> dataEvents, Context context, FragmentListEvents fragmentListEvents) {
        this.context = context;
        this.mCallback = fragmentListEvents;
        this.dataEvents = dataEvents;
    }

    public List<Event> getEvents() {
        return dataEvents;
    }

    public void setEvents(List<Event> dataEvents) {
        this.dataEvents = dataEvents;
    }

    public void setOnItemClickListener(PaginationAdapterEvents.OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.list_item_event, parent, false);
                viewHolder = new EventVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Event event = dataEvents.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                EventVH eventVH = (EventVH) holder;
                if (event != null) {
                    if (event.getPrice() == 0) {
                        eventVH.tvEventPrice.setText(R.string.event_price_free);
                    } else {
                        eventVH.tvEventPrice.setText(event.getPrice() + Constants.KEY_RUB);
                    }
                    if (event.getEnd() != null && !event.getEnd().substring(0, 11).equals(event.getBegin().substring(0, 11))) {
                        StringBuilder sb = new StringBuilder(newDateFormat(event.getBegin()));
                        sb.append(" - ").append(newDateFormat2(event.getEnd()));
                        eventVH.tvEventDate.setText(sb);
                    } else {
                        eventVH.tvEventDate.setText(newDateFormat(event.getBegin()));
                    }
                    eventVH.tvEventTitle.setText(event.getTitle());
                    if (event.getEventPhotoList().isEmpty()) {
                        eventVH.ivEventImage.setImageResource(R.drawable.testimg);
                    } else {
                        Picasso.get().load(BASE_IMAGE_URL + EVENT_IMAGE_DIRECTION + event.getEventPhotoList().get(0).getEventPhoto())
                                .placeholder(R.drawable.placeholder)
                                .into(eventVH.ivEventImage);
                    }
                }
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataEvents == null ? 0 : dataEvents.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == dataEvents.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    public void filter(List<Event> copy, int category, LocalDate localDateStart, LocalDate localDateEnd, LocalDate localDateSingle, boolean isShowFree) {
        List<Event> filterList = new ArrayList<>();
        dataEvents.clear();
        dataEvents.addAll(copy);
        if (localDateEnd != null) {
            if (isShowFree && category != 0) {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))
                            && event.getPrice() == 0 && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else if (isShowFree) {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))
                            && event.getPrice() == 0) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else if (category != 0) {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))
                            && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isAfter(localDateStart.minusDays(1)) &&
                            LocalDate.parse(event.getBegin(), FORMATTER2).isBefore(localDateEnd.plusDays(1))) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            }
        } else if (localDateStart != null) {
            if (isShowFree && category != 0) {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)
                            && event.getPrice() == 0 && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else if (isShowFree) {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)
                            && event.getPrice() == 0) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else if (category != 0) {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)
                            && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else {
                for (Event event : dataEvents) {
                    if (LocalDate.parse(event.getBegin(), FORMATTER2).isEqual(localDateSingle)) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            }
        } else {
            if (isShowFree && category != 0) {
                for (Event event : dataEvents) {
                    if (event.getPrice() == 0 && event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else if (isShowFree) {
                for (Event event : dataEvents) {
                    if (event.getPrice() == 0) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else if (category != 0) {
                for (Event event : dataEvents) {
                    if (event.getEventTypeId() == category) {
                        filterList.add(event);
                    }
                }
                dataEvents.clear();
                addAll(filterList);
            } else {
                dataEvents.clear();
                addAll(copy);
            }
        }
        notifyDataSetChanged();
        sortByDate();
    }

    public void sortByDate() {
        Collections.sort(dataEvents, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                try {
                    return SIMPLE_DATE_FORMAT.parse(e2.getBegin()).compareTo(SIMPLE_DATE_FORMAT.parse(e1.getBegin()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    public void add(Event event) {
        dataEvents.add(event);
        notifyItemInserted(dataEvents.size() - 1);
    }

    public void addAll(List<Event> dataEvents) {
        for (Event event : dataEvents) {
            add(event);
        }
    }

    public void remove(Event event) {
        int position = dataEvents.indexOf(event);
        if (position > -1) {
            dataEvents.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
//        add(new Shop());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = dataEvents.size() - 1;
        Event event = getItem(position);

        if (event != null) {
            dataEvents.remove(position);
            notifyItemRemoved(position);
        }
    }

    private Event getItem(int position) {
        return dataEvents.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(dataEvents.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public interface OnItemClickListener {
        void onItemEventClick(int position);
    }

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

    class EventVH extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventPrice;
        TextView tvEventTitle;
        TextView tvEventDate;

        EventVH(View v) {
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
                            mListener.onItemEventClick(position);
                        }
                    }
                }
            });
        }
    }
}
