package barqsoft.footballscores.images;

import android.util.Log;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes an SVG internal representation from an {@link InputStream}.
 */
public class SvgDecoder implements ResourceDecoder<InputStream, SVG> {
    private final String LOG_TAG = SvgDecoder.class.getSimpleName();
    public Resource<SVG> decode(InputStream source, int width, int height) throws IOException {
        try {
            SVG svg = SVG.getFromInputStream(source);
            return new SimpleResource<SVG>(svg);
        } catch (SVGParseException ex) {
            Log.v(LOG_TAG, source.toString());
            throw new IOException("Cannot load SVG from stream", ex);
        }
    }

    @Override
    public String getId() {
        return "SvgDecoder.com.bumptech.svgsample.app";
    }
}