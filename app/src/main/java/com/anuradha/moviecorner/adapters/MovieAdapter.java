package com.anuradha.moviecorner.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anuradha.moviecorner.MainActivityFragment;
import com.anuradha.moviecorner.R;
import com.anuradha.moviecorner.Utility;
import com.anuradha.moviecorner.database.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    //    private static String LOG_TAG = MovieAdapter.class.getSimpleName();
    final private Context mContext;
    private Cursor mCursor;
    private CursorAdapter mCursorAdapter;
    final private MovieAdapterOnClickHandler mClickHandler;

    /**
     * Cache of the children views for a forecast list item.
     */

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView mImageView;
        final TextView mRelaseView;
//                final TextView mRatingView;

        MovieAdapterViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.poster_imgview);
            mRelaseView = (TextView) view.findViewById(R.id.movie_year);
//            mRatingView = (TextView) view.findViewById(R.id.movie_vote);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int movieId = mCursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_ID);
            mClickHandler.onClick(mCursor.getInt(movieId), this);
        }
    }

    public interface MovieAdapterOnClickHandler {
        void onClick(int movieId, MovieAdapterViewHolder vh);
    }

    // Because RecyclerView.Adapter in its current form doesn't natively
    // support cursors, we wrap a CursorAdapter that will do all the job
    // for us.

    public MovieAdapter(Context context, Cursor c, MovieAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        mClickHandler = clickHandler;
        mCursorAdapter = new CursorAdapter(mContext, c, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the view here
                return LayoutInflater.from(context).inflate(R.layout.poster_view, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Binding operations
                ImageView imageView = (ImageView) view.findViewById(R.id.poster_imgview);
                TextView yearView = (TextView) view.findViewById(R.id.movie_year);
//                TextView voteView = (TextView) view.findViewById(R.id.movie_vote);
                String[] releaseDate = (cursor.getString(MainActivityFragment.COLUMN_MOVIE_RELEASEDATE))
                        .split("-");
                //Calls the Picasso API for loading the imageView from the given path
                Picasso.with(context).load(Utility.getActualPosterPath(cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH)))
                        .error(R.drawable.unavailable_poster_black)
                        .into(imageView);
                if ((releaseDate[0] != null) && (releaseDate.length == 3))
                    yearView.setText(Utility.getMonthName(releaseDate[1]) + releaseDate[2] + ", " + releaseDate[0]);

//                String s = String.format(Locale.getDefault(),"%.1f", Float.parseFloat(cursor.getString(MainActivityFragment.COLUMN_MOVIE_VOTE)));
//                voteView.setText(s);
            }
        };
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poster_view, parent, false);
        view.setFocusable(true);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MovieAdapterViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
