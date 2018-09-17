package com.technativ.menotice.main.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.technativ.menotice.R;
import com.technativ.menotice.main.activities.NoticeDetailsActivity;
import com.technativ.menotice.main.models.Category;
import com.technativ.menotice.main.models.Notice;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.SingleItemsRowHolder> {

    private List<Notice> itemsList;

    public NoticeListAdapter(ArrayList<Notice> itemsList) {
        this.itemsList = itemsList;
    }

    @Override
    public SingleItemsRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_single, viewGroup, false);
        return new SingleItemsRowHolder(v, viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(SingleItemsRowHolder holder, int i) {
        holder.bindViews(itemsList.get(i));
    }

    public void setItems(List<Notice> items) {
        this.itemsList = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    class SingleItemsRowHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvPublisher, tvDatePublished;
        ImageView itemImage;

        private final Context context;

        SingleItemsRowHolder(View view, Context context) {
            super(view);

            this.tvTitle = (TextView) view.findViewById(R.id.notice_title);
            this.tvPublisher = (TextView) view.findViewById(R.id.tvPublisher);
            this.tvDatePublished = (TextView) view.findViewById(R.id.tvDatePublished);
            this.itemImage = (ImageView) view.findViewById(R.id.notice_logo);
            this.context = context;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int noticeId = itemsList.get(getAdapterPosition()).getNoticeId();

                    Intent intent = new Intent(v.getContext(), NoticeDetailsActivity.class);
                    intent.putExtra(NoticeDetailsActivity.EXTRA_NOTICE_ID, noticeId);
                    v.getContext().startActivity(intent);

                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v
                                .findViewById(R.id.row_content)
                                .getBackground()
                                .setHotspot(event.getX(), event.getY());
                        return(false);
                    }
                });
            }

        }

        void bindViews(Notice notice){
            tvTitle.setText(notice.getTitle());
            tvPublisher.setText(String.format("By %s", notice.getPublisher()));
            tvDatePublished.setText(notice.getDatePublished());

            try {
                InputStream is = context.getAssets().open(notice.getImage());
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();

                Glide.with(context).load(buffer)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(itemImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}