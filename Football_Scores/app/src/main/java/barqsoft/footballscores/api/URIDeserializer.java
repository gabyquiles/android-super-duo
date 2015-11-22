package barqsoft.footballscores.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by gabrielquiles-perez on 11/18/15.
 */
public class URIDeserializer implements JsonDeserializer<URI> {
    public URI deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context)
        throws JsonParseException {
        URI uri = null;
        try {
            uri = new URI(json.toString());

        } catch (URISyntaxException e) {

        }
        return uri;
    }
}
