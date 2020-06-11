package com.example.pakalo.chatbot;

import javax.annotation.Nullable;

/**
 * This class is used to tell that the message belongs to which person My or Friend
 */
public class MessageItemUI {
    public String messageText;
    public MessageType messageType;
    @Nullable
    public String recipeIDPayload = null;
    private boolean isButton;

    //My message or friend's
    public static enum MessageType{
        TYPE_MY_MESSAGE, TYPE_FRIEND_MESSAGE
    }

    public MessageItemUI(String text, MessageType messageType){
        this.messageText = text;
        this.messageType = messageType;
        isButton = false;
    }

    public MessageItemUI(String text, String recipeIDPayload, MessageType messageType){
        this.messageText = text;
        this.messageType = messageType;
        this.recipeIDPayload = recipeIDPayload;
        this.isButton = true;
    }

    /**
     * if this is recipe ID then return True
     * @return
     */
    public boolean isButton(){
        return isButton;
    }

    @Nullable
    public String getRecipeIDPayload(){
        return recipeIDPayload;
    }
}
