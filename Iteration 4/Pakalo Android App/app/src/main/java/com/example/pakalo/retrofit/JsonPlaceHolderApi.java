package com.example.pakalo.retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface JsonPlaceHolderApi {
    @POST("/webhooks/rest/webhook")
    Call<List<RetroChatbotResponse>> getReplies(@Body RetroChatbotRequest retroChatbotRequest);
}
