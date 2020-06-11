package com.example.pakalo.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Singleton class that initializes and returns retrofit instance
public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://23e2249874f6.ngrok.io";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}