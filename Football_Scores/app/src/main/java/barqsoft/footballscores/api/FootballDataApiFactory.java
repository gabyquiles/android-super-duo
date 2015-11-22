package barqsoft.footballscores.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import barqsoft.footballscores.R;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by gabrielquiles-perez on 11/18/15.
 */
public class FootballDataApiFactory {
    private static FootballDataService mInstance;

    public static FootballDataService getInstance(Context context) {
        String apiKey = context.getString(R.string.api_key);
        String baseUrl = context.getString(R.string.api_base_url);
        String dateFormat = context.getString(R.string.api_date_format);
        if(mInstance == null) {
            mInstance = createApi(baseUrl, apiKey, dateFormat);
        }
        return mInstance;
    }

    private static FootballDataService createApi(String baseUrl, final String apiKey, String dateFormat) {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().addHeader("X-Auth-Token", apiKey).build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(interceptor);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new ItemTypeAdapterFactory());
        builder.setDateFormat(dateFormat);
        Gson gson = builder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        return retrofit.create(FootballDataService.class);
    }
}
