package barqsoft.footballscores.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide.request.FutureTarget;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.R;

/**
 * Created by gabrielquiles-perez on 11/23/15.
 */
public class SVGLoader extends ImageLoader{
    private final String LOG_TAG = SVGLoader.class.getSimpleName();
    private static SVGLoader mLoader;
//    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> mRequestBuilder;
    private ResourceTranscoder<SVG, PictureDrawable> mTranscoder;
    private Encoder<InputStream> mStreamEncoder;
    private FileToStreamDecoder<SVG> mDecoder;
    private SvgSoftwareLayerSetter<Uri> mListener;


    private SVGLoader(Context context) {
        if (mTranscoder == null) {
            mTranscoder = new SvgDrawableTranscoder();
        }
        if (mStreamEncoder == null) {
            mStreamEncoder = new StreamEncoder();
        }
        if (mDecoder == null) {
            mDecoder = new FileToStreamDecoder<SVG>(new SvgDecoder());
        }
        if (mListener == null) {
            mListener = new SvgSoftwareLayerSetter<Uri>();
        }
        mRequestBuilder = Glide
                .with(context)
                .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(mTranscoder, PictureDrawable.class)
                .sourceEncoder(mStreamEncoder)
                .cacheDecoder(mDecoder)
                .decoder(new SvgDecoder())
                .error(R.drawable.no_icon)
                .animate(android.R.anim.fade_in);
    }

    public static SVGLoader getInstance(Context context) {
        if (mLoader == null) {
            mLoader = new SVGLoader(context);
        }
        return mLoader;
    }
    public void loadIntoImageView(String url, ImageView view) {
        //Note: this image is causing a Fatal signal 11 (SIGSEGV), code 1
        if(!url.equals("http://upload.wikimedia.org/wikipedia/de/d/de/Getafe_CF.svg")) {
            mRequestBuilder.listener(mListener)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    // SVG cannot be serialized so it's not worth to cache it
                    .load(Uri.parse(url))
                    .into(view);
        }
    }

    public Bitmap getTeamCrestAsBitmap(String url) {
        if(!url.equals("http://upload.wikimedia.org/wikipedia/de/d/de/Getafe_CF.svg")) {
            try {
                FutureTarget target = mRequestBuilder
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .load(Uri.parse(url)).into(40, 40);
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
        }
        return null;
    }
}
