package com.anuradha.moviewatch.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anuradha.moviewatch.R;
import com.anuradha.moviewatch.async.Trailer;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrailerRecyclerAdapter extends RecyclerView.Adapter<TrailerRecyclerAdapter.TrailerRecyclerAdapterViewHolder> {
    final private Context mContext;
    private List<Trailer> mTrailers;

    class TrailerRecyclerAdapterViewHolder extends RecyclerView.ViewHolder {
        final ImageView mImageView;
        final TextView mDescriptionView;

        TrailerRecyclerAdapterViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.trailer_link_btn);
            mDescriptionView = (TextView) view.findViewById(R.id.trailer_name);
        }
    }

    public TrailerRecyclerAdapter(Context context, List<Trailer> trailers) {
        this.mContext = context;
        this.mTrailers = trailers;
    }

    @Override
    public TrailerRecyclerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_view, parent, false);
        view.setFocusable(true);
        return new TrailerRecyclerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerRecyclerAdapterViewHolder holder, int position) {
        String yt_thumbnail_url;
        yt_thumbnail_url = "http://img.youtube.com/vi/" + mTrailers.get(position).getKey() + "/0.jpg";
        Picasso.with(mContext).load(yt_thumbnail_url)
                .error(R.drawable.play_button)
                .into(holder.mImageView);

        (holder.mDescriptionView).setText(mTrailers.get(position).getName());
        final int adapter_position = holder.getAdapterPosition();

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + mTrailers.get(adapter_position).getKey())));
            }
        });
        holder.mDescriptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + mTrailers.get(adapter_position).getKey())));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mTrailers) return 0;
        return mTrailers.size();
    }
}
