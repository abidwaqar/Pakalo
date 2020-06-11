package com.example.pakalo.retrofit;

import com.google.gson.annotations.SerializedName;

public class RetroChatbotRequest {
    @SerializedName("sender")
    private String sender;
    @SerializedName("message")
    private String message;

    public RetroChatbotRequest(String sender, String message){
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
