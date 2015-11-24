package barqsoft.footballscores.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;

/**
 * Created by gabrielquiles-perez on 11/24/15.
 */
public abstract class ImageLoader {
    private final String LOG_TAG = ImageLoader.class.getSimpleName();
    protected GenericRequestBuilder mRequestBuilder;

    abstract public void loadIntoImageView(String url, ImageView view);
    abstract public Bitmap getTeamCrestAsBitmap(String url);

    public void clearCache(Context context, View view) {
        Log.w(LOG_TAG, "clearing cache");
        Glide.clear(view);
        Glide.get(context).clearMemory();
    }
}
