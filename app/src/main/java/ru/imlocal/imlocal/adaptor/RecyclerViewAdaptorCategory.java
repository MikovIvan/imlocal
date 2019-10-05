package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.imlocal.imlocal.R;

public class RecyclerViewAdaptorCategory extends RecyclerView.Adapter<RecyclerViewAdaptorCategory.ViewHolder> {
    private int category_index;
    private List<String> data;
    private Context context;
    private OnItemCategoryClickListener mListener;

    public RecyclerViewAdaptorCategory(Context context, String category) {
        switch (category) {
            case "shop":
                category_index = 5;
                data = new ArrayList<>();
                data.add("Еда");
                data.add("Дети");
                data.add("Фитнес");
                data.add("Красота");
                data.add("Покупки");
                data.add("Все");
                this.context = context;
                break;
            case "action":
                category_index = 5;
                data = new ArrayList<>();
                data.add("Еда");
                data.add("Дети");
                data.add("Фитнес");
                data.add("Красота");
                data.add("Покупки");
                data.add("Все");
                this.context = context;
                break;
            case "event":
                category_index = 8;
                data = new ArrayList<>();
                data.add("Еда");
                data.add("Дети");
                data.add("Спорт");
                data.add("Город");
                data.add("Театр");
                data.add("Шоу");
                data.add("Ярмарка");
                data.add("Творчество");
                data.add("Все");
                this.context = context;
                break;
            case "add_action":
                category_index = -1;
                data = new ArrayList<>();
                data.add("Еда");
                data.add("Дети");
                data.add("Фитнес");
                data.add("Красота");
                data.add("Покупки");
                this.context = context;
                break;
            case "add_event":
                category_index = -1;
                data = new ArrayList<>();
                data.add("Еда");
                data.add("Дети");
                data.add("Спорт");
                data.add("Город");
                data.add("Театр");
                data.add("Шоу");
                data.add("Ярмарка");
                data.add("Творчество");
                this.context = context;
                break;
        }



    }

    public void setOnItemClickListener(OnItemCategoryClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdaptorCategory.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);

        return new RecyclerViewAdaptorCategory.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdaptorCategory.ViewHolder holder, final int position) {
        holder.tvCategory.setText(data.get(position));

        holder.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category_index = position;
                notifyDataSetChanged();
                if (mListener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClickCategory(position);
                    }
                }
            }
        });

        if (category_index == position) {
            holder.tvCategory.setBackground(context.getResources().getDrawable(R.color.color_main));
            holder.tvCategory.setTextColor(Color.WHITE);
        } else {
            holder.tvCategory.setBackgroundColor(Color.WHITE);
            holder.tvCategory.setTextColor(context.getResources().getColor(R.color.color_main));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemCategoryClickListener {
        void onItemClickCategory(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;

        ViewHolder(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.btn_category);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClickCategory(position);
                        }
                    }
                }
            });
        }
    }
}
