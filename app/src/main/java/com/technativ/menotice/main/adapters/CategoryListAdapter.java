package com.technativ.menotice.main.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.technativ.menotice.R;
import com.technativ.menotice.main.models.Category;
import com.technativ.menotice.main.models.Notice;

import java.util.ArrayList;
import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.SectionsRowHolder> {

    private List<Category> sectionsData;
    private Context mContext;

    private int mPrefetchItemCount;

    public CategoryListAdapter(Context context, List<Category> sectionsData) {
        this.sectionsData = sectionsData;
        this.mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                mPrefetchItemCount = (int) Math.ceil(recyclerView.getWidth() /
                        recyclerView.getResources().getDimension(R.dimen.notice_width));
            }
        });
    }

    @Override
    public SectionsRowHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_section, viewGroup, false);
        return new SectionsRowHolder(v);
    }

    @Override
    public void onBindViewHolder(SectionsRowHolder holder, int position) {
        holder.bindViews(sectionsData.get(position));
    }

    @Override
    public int getItemCount() {
        return (null != sectionsData ? sectionsData.size() : 0);
    }

    public void setItems(List<Category> items) {
        this.sectionsData = items;
        notifyDataSetChanged();
    }

    class SectionsRowHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        RecyclerView singleItemsList;

        SectionsRowHolder(View view) {
            super(view);

            this.categoryName = (TextView) view.findViewById(R.id.category_name);
            this.singleItemsList = (RecyclerView) view.findViewById(R.id.single_items_list);
        }

        void bindViews(Category categoryItem){
            final String sectionName = categoryItem.getName();
            categoryName.setText(sectionName);

            ArrayList<Notice> singleSectionItems = categoryItem.getNotices();

            singleItemsList.setHasFixedSize(true);
            singleItemsList.setNestedScrollingEnabled(false);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            layoutManager.setInitialPrefetchItemCount(mPrefetchItemCount);
            singleItemsList.setLayoutManager(layoutManager);
            NoticeListAdapter itemListDataAdapter = new NoticeListAdapter(singleSectionItems);
            singleItemsList.setAdapter(itemListDataAdapter);

        }
    }
}