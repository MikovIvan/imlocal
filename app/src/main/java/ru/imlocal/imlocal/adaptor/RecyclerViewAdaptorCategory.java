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
import ru.imlocal.imlocal.entity.Category;

public class RecyclerViewAdaptorCategory extends RecyclerView.Adapter<RecyclerViewAdaptorCategory.ViewHolder> {
    private int category_index;
    private List<Category> data;
    private Context context;
    private OnItemCategoryClickListener mListener;
    private int lastSelectedPosition = -1;
    boolean isRepeated;

    public RecyclerViewAdaptorCategory(Context context, String category) {
        switch (category) {
            case "shop":
            case "action":
                category_index = 5;
                data = new ArrayList<>();
                data.add(new Category("Еда"));
                data.add(new Category("Дети"));
                data.add(new Category("Фитнес"));
                data.add(new Category("Красота"));
                data.add(new Category("Покупки"));
                data.add(new Category("Все"));
                this.context = context;
                break;
            case "event":
                category_index = 8;
                data = new ArrayList<>();
                data.add(new Category("Еда"));
                data.add(new Category("Дети"));
                data.add(new Category("Спорт"));
                data.add(new Category("Город"));
                data.add(new Category("Театр"));
                data.add(new Category("Шоу"));
                data.add(new Category("Ярмарка"));
                data.add(new Category("Творчество"));
                data.add(new Category("Все"));
                this.context = context;
                break;
            case "add_action":
                category_index = -1;
                data = new ArrayList<>();
                data.add(new Category("Еда"));
                data.add(new Category("Дети"));
                data.add(new Category("Фитнес"));
                data.add(new Category("Красота"));
                data.add(new Category("Покупки"));
                this.context = context;
                break;
            case "add_event":
                category_index = -1;
                data = new ArrayList<>();
                data.add(new Category("Еда"));
                data.add(new Category("Дети"));
                data.add(new Category("Спорт"));
                data.add(new Category("Город"));
                data.add(new Category("Театр"));
                data.add(new Category("Шоу"));
                data.add(new Category("Ярмарка"));
                data.add(new Category("Творчество"));
                this.context = context;
                break;
            case "add_shop":
                category_index = -1;
                data = new ArrayList<>();
                data.add(new Category("Еда"));
                data.add(new Category("Дети"));
                data.add(new Category("Фитнес"));
                data.add(new Category("Красота"));
                data.add(new Category("Покупки"));
                this.context = context;
                break;
        }
    }

    public void setOnItemClickListener(OnItemCategoryClickListener listener) {
        mListener = listener;
    }

    public void setCategory_index(int category_index) {
        data.get(category_index).setSelected(true);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdaptorCategory.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);

        return new RecyclerViewAdaptorCategory.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdaptorCategory.ViewHolder holder, final int position) {
        Category category = data.get(position);
        holder.tvCategory.setText(category.getName());
        holder.tvCategory.setBackground(category.isSelected() ? context.getResources().getDrawable(R.color.color_main) : context.getResources().getDrawable(android.R.color.white));
        holder.tvCategory.setTextColor(category.isSelected() ? Color.WHITE : context.getResources().getColor(R.color.color_main));

        holder.tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSelectedPosition >= 0) {
                    data.get(lastSelectedPosition).setSelected(false);
                }

                if (lastSelectedPosition == holder.getAdapterPosition() && isRepeated) {
                    category.setSelected(false);
                    isRepeated = false;
                } else {
                    category.setSelected(!category.isSelected());
                    isRepeated = true;
                }

                holder.tvCategory.setBackground(category.isSelected() ? context.getResources().getDrawable(R.color.color_main) : context.getResources().getDrawable(android.R.color.white));
                holder.tvCategory.setTextColor(category.isSelected() ? Color.WHITE : context.getResources().getColor(R.color.color_main));
                lastSelectedPosition = holder.getAdapterPosition();
                if (mListener != null) {
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClickCategory(position);
                    }
                }
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
