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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.technativ.menotice.R;
import com.technativ.menotice.main.activities.PublisherDetailsActivity;
import com.technativ.menotice.main.custom.CircleTransform;
import com.technativ.menotice.main.models.Publisher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PublisherListAdapter extends RecyclerView.Adapter<PublisherListAdapter.ItemRowHolder> {

    private Context context;
    private List<Publisher> itemsList;

    public PublisherListAdapter(Context context, ArrayList<Publisher> itemsList) {
        this.context = context;
        this.itemsList = itemsList;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_follows, viewGroup, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemRowHolder holder, int i) {
        holder.bindViews(itemsList.get(i));
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public void setItems(List<Publisher> items) {
        this.itemsList = items;
        notifyDataSetChanged();
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {

        ImageView followsLogo;
        TextView followsName;
        TextView timeAgo;
        ImageView actionImage;

        ItemRowHolder(View view) {
            super(view);

            this.followsLogo = (ImageView) view.findViewById(R.id.follows_logo);
            this.followsName = (TextView) view.findViewById(R.id.follows_name);
            this.timeAgo = (TextView) view.findViewById(R.id.time_ago);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), PublisherDetailsActivity.class);
                    intent.putExtra(PublisherDetailsActivity.EXTRA_PUBLISHER_ID, itemsList.get(getAdapterPosition()).getPublisherId());
                    intent.putExtra(PublisherDetailsActivity.EXTRA_PUBLISHER_ABOUT, itemsList.get(getAdapterPosition()).getAbout());
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

        void bindViews(Publisher follows) {
            followsName.setText(follows.getName());
            timeAgo.setText(follows.getTime());

            try {
                InputStream is = context.getAssets().open(follows.getLogo());
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();

                Glide.with(context).load(buffer)
                        .thumbnail(0.5f)
                        .crossFade()
                        .transform(new CircleTransform(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(followsLogo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}