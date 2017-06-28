package br.senac.waterreservoir.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.io.IOException;

import br.senac.waterreservoir.Application;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * REST client using Retrofit2
 */
public class RestClient {

    private static final String BASE_URL = "http://senac-tcs-api.herokuapp.com/";

    public static Retrofit getClient(final Context context)
    {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create a builder
        Builder builder = new Builder()
                .addInterceptor(loggingInterceptor);

        // Get authorization token from sharedPreferences
        SharedPreferences settings = context.getSharedPreferences(Application.SHARED_PREFERENCE, Context.MODE_PRIVATE);
        final String token = settings.getString("token", "");

        if (!"".equals(token)) {
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("Authorization", token).build();
                    return chain.proceed(request);
                }
            });
        }

        // Build client
        OkHttpClient client = builder.build();

        // Return retrofit instance
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
