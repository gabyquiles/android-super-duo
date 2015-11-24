package barqsoft.footballscores.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.R;

/**
 * Created by gabrielquiles-perez on 11/24/15.
 */
public class PNGLoader extends ImageLoader {
    private final String LOG_TAG = PNGLoader.class.getSimpleName();
    private static PNGLoader mLoader;
    private Context mContext;

    private PNGLoader(Context context) {
        mContext = context;
    }

    public static PNGLoader getInstance(Context context) {
        if (mLoader == null) {
            mLoader = new PNGLoader(context);
        }
        return mLoader;
    }

    public void loadIntoImageView(String url, ImageView view) {
        Glide.with(mContext)
                .load(url)
                .error(R.drawable.no_icon)
                .crossFade()
                .into(view);
    }

    public Bitmap getTeamCrestAsBitmap(String url) {
            try {
                FutureTarget target = Glide.with(mContext)
                        .load(url)
                        .error(R.drawable.no_icon)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(40, 40);
                PictureDrawable pictureDrawable = (PictureDrawable) target.get();

                Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),
                        pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawPicture(pictureDrawable.getPicture());
                return bitmap;
            } catch (ExecutionException e) {
                Log.v(LOG_TAG, "ExecutionException");
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.v(LOG_TAG, "InterruptedException");
                e.printStackTrace();
            } catch (Exception e) {
                Log.v(LOG_TAG, "GenericException");
                e.printStackTrace();
            }
        return null;
    }
}
