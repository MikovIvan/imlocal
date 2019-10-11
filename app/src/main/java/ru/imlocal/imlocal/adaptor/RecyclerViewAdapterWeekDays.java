package ru.imlocal.imlocal.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.imlocal.imlocal.R;
import ru.imlocal.imlocal.entity.Category;

public class RecyclerViewAdapterWeekDays extends RecyclerView.Adapter<RecyclerViewAdapterWeekDays.ViewHolder> {
    private List<Category> data;
    private Context context;
    private RecyclerViewAdapterWeekDays.OnItemCategoryClickListener mListener;

    public RecyclerViewAdapterWeekDays(Context context, List<Category> data) {
        this.data = data;
        this.context = context;
    }

    public void setOnItemClickListener(RecyclerViewAdapterWeekDays.OnItemCategoryClickListener listener) {
        mListener = listener;
    }

    @Override
    public RecyclerViewAdapterWeekDays.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);

        return new RecyclerViewAdapterWeekDays.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterWeekDays.ViewHolder holder, final int position) {
        Category category = data.get(position);
        holder.tvCategory.setText(category.getName());

        holder.tvCategory.setBackgroundColor(category.isSelected() ? context.getResources().getColor(R.color.color_main) : Color.WHITE);
        holder.tvCategory.setTextColor(category.isSelected() ? Color.WHITE : context.getResources().getColor(R.color.color_main));
        holder.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClickCategory(position);
                    }
                }
                category.setSelected(!category.isSelected());
                holder.tvCategory.setBackgroundColor(category.isSelected() ? context.getResources().getColor(R.color.color_main) : Color.WHITE);
                holder.tvCategory.setTextColor(category.isSelected() ? Color.WHITE : context.getResources().getColor(R.color.color_main));
            }
        });

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
