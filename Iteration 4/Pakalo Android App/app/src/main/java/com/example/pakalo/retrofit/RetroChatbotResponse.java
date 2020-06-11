package com.example.pakalo.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Nullable;

public class RetroChatbotResponse {
    @SerializedName("recipient_id")
    private String recipient_id;
    @SerializedName("text")
    private String text;
    @SerializedName("buttons")
    @Nullable
    private List<Button> buttons = null;

    public RetroChatbotResponse(String recipient_id, String text){
        this.recipient_id = recipient_id;
        this.text = text;
    }

    public RetroChatbotResponse(String recipient_id, String text, List<Button> buttons){
        this.recipient_id = recipient_id;
        this.text = text;
        this.buttons = buttons;
    }

    public String getRecipient_id() {
        return recipient_id;
    }

    public String getText() {
        return text;
    }

    public void setRecipient_id(String recipient_id) {
        this.recipient_id = recipient_id;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public List<Button> getButtons() {
        return buttons;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    public class Button{
        @Nullable
        @SerializedName("payload")
        private String payload = null;

        @Nullable
        @SerializedName("title")
        private String title = null;

        @Nullable
        public String getRecipeIDPayload() {
            return payload;
        }

        @Nullable
        public String getTitle() {
            return title;
        }
    };
}
