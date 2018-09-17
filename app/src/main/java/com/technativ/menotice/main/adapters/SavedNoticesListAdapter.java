package com.technativ.menotice.main.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import com.technativ.menotice.main.fragments.SavedNoticesFragment;

import java.io.IOException;
import java.io.InputStream;

public class SavedNoticesListAdapter extends RecyclerView.Adapter<SavedNoticesListAdapter.ItemRowHolder> {

    private Context context;
    private Cursor itemsCursor;

    public SavedNoticesListAdapter(Context context, Cursor itemsCursor) {
        this.context = context;
        this.itemsCursor = itemsCursor;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_saved_notices, viewGroup, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemRowHolder holder, int i) {
        itemsCursor.moveToPosition(i);
        holder.bindViews(itemsCursor);
    }

    @Override
    public int getItemCount() {
        return (null != itemsCursor ? itemsCursor.getCount() : 0);
    }

    public void setItems(Cursor items) {
        this.itemsCursor = items;
        notifyDataSetChanged();
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {

        ImageView savedNoticeImage;
        TextView savedNoticeTitle;
        TextView savedNoticePublisher;
        ImageView actionImage;

        ItemRowHolder(View view) {
            super(view);

            this.savedNoticeImage = (ImageView) view.findViewById(R.id.saved_notice_image);
            this.savedNoticeTitle = (TextView) view.findViewById(R.id.saved_notice_title);
            this.savedNoticePublisher = (TextView) view.findViewById(R.id.saved_notice_publisher);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), PublisherDetailsActivity.class);
                    itemsCursor.moveToPosition(getAdapterPosition());
                    intent.putExtra(PublisherDetailsActivity.EXTRA_PUBLISHER_ID, itemsCursor.getString(
                            itemsCursor.getColumnIndex(SavedNoticesFragment.NOTICE_ID)));

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

        void bindViews(Cursor cursor) {
            savedNoticeTitle.setText(cursor.getString(
                    cursor.getColumnIndex(SavedNoticesFragment.TITLE)));
            savedNoticePublisher.setText(cursor.getString(
                    cursor.getColumnIndex(SavedNoticesFragment.PUBLISHER)));

            try {
                InputStream is = context.getAssets().open(cursor.getString(
                        cursor.getColumnIndex(SavedNoticesFragment.IMAGE)));
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();

                Glide.with(context).load(buffer)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(savedNoticeImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}