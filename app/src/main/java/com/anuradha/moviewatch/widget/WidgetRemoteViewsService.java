package com.anuradha.moviewatch.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.anuradha.moviewatch.R;
import com.anuradha.moviewatch.database.MovieContract;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.ExecutionException;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {
//    public final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();
    private static final String[] WIDGET_COLUMNS = {
            MovieContract.MoviesEntry.COLUMN_ID,
            MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
            MovieContract.MoviesEntry.COLUMN_TITLE,
            MovieContract.MoviesEntry.COLUMN_GENRE,
            MovieContract.MoviesEntry.COLUMN_RUNTIME
    };
    // these indices must match the projection
    static final int INDEX_ID = 0;
    static final int INDEX_POSTER_PATH = 1;
    static final int INDEX_TITLE = 2;
    static final int INDEX_GENRE = 3;
    static final int INDEX_RUNTIME = 4;


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                String sort_order = getResources().getStringArray(R.array.sort_values)[7];
                Uri movieUri = MovieContract.MoviesEntry.buildMoviesWithSortorder(sort_order);
                data = getContentResolver().query(movieUri,
                        WIDGET_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_widget_item);
                String title = data.getString(INDEX_TITLE);
                Bitmap poster = null;
                String posterUrl = data.getString(INDEX_POSTER_PATH);
                try {
                    poster = Glide.with(WidgetRemoteViewsService.this)
                            .load(posterUrl)
                            .asBitmap()
                            .error(R.drawable.widget_no_poster)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                } catch (InterruptedException |
                        ExecutionException e
                        )

                {
//                    Log.e(LOG_TAG, "Error retrieving poster from " + posterUrl, e);
                }


                if (poster != null)

                {
                    views.setImageViewBitmap(R.id.widget_icon, poster);
                } else

                {
                    views.setImageViewResource(R.id.widget_icon, R.drawable.widget_no_poster);
                }

                views.setTextViewText(R.id.widget_movie_title, data.getString(INDEX_TITLE));
                views.setTextViewText(R.id.widget_genre, data.getString(INDEX_GENRE));
                views.setTextViewText(R.id.widget_runtime, data.getString(INDEX_RUNTIME));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)

                {
                    setRemoteContentDescription(views, title);
                }

                //                TO BE DONE
                final Intent fillInIntent = new Intent();
//                Bundle extras = new Bundle();
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_movie_title, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_widget_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
