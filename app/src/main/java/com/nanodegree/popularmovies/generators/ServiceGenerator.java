package com.nanodegree.popularmovies.generators;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nanodegree.popularmovies.R;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;


/**
 * Created by yogeshmadaan on 03/02/16.
 */
public class ServiceGenerator {
    static String BASE_URL= "https://api.themoviedb.org/3/";

    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, final Context context) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());
//                .client(createOkHttpClient(context))
//                .build();
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
//            @Override public Response intercept(Chain chain) throws IOException {
//                PackageInfo pInfo = null;
//                try {
//                    pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }
//                Request request = chain.request();
//                Request newReq = request.newBuilder()
//                        .addHeader("Accept", "application/json")
//                        .addHeader("versionCode", String.valueOf(pInfo.versionCode))
//                        .addHeader("versionName",String.valueOf(pInfo.versionName))
//                        .build();
//                return chain.proceed(newReq);
//            }
//        }).build();
        OkHttpClient.Builder builder1 = new OkHttpClient.Builder()
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Interceptor.Chain chain) throws IOException {
                                Request original = chain.request();

                                // Request customization: add request headers
                                Request.Builder requestBuilder = original.newBuilder()
                                        .header("Authorization", "abc")
                                        .method(original.method(), original.body());

                                Request request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        })
                ;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder1.addInterceptor(logging);
        OkHttpClient client = builder1.build();
        builder.client(client);
        return builder.build().create(serviceClass);
    }
    private static OkHttpClient createOkHttpClient(final Context context) {
        OkHttpClient client = new OkHttpClient();
        int connTimeout = context.getResources().getInteger(R.integer.http_timeout_limit);
        int readTimeout = context.getResources().getInteger(R.integer.http_timeout_limit);
//        client.setConnectTimeout(connTimeout, TimeUnit.SECONDS);
//        client.setReadTimeout(readTimeout, TimeUnit.SECONDS);
//       client.setRetryOnConnectionFailure(true);
        client.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                PackageInfo pInfo = null;
                try {
                    pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // Customize the request
                Request request = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("versionCode", String.valueOf(pInfo.versionCode))
                        .header("versionName",String.valueOf(pInfo.versionName))
                        .method(original.method(), original.body())
                        .build();

                Response response = chain.proceed(request);

                // Customize or return the response
                return response;
            }
        });
//        client.interceptors().add(new LoggingInterceptor());


        return client;
    }
}
