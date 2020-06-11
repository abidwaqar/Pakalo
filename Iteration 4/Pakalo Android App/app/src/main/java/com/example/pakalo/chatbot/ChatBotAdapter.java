package com.example.pakalo.chatbot;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakalo.R;
import com.example.pakalo.retrofit.JsonPlaceHolderApi;
import com.example.pakalo.retrofit.RetroChatbotRequest;
import com.example.pakalo.retrofit.RetroChatbotResponse;
import com.example.pakalo.retrofit.RetrofitClientInstance;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * This is the adapter for Chatbot messaging view
 */
public class ChatBotAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private LayoutInflater inflater;
    private List<MessageItemUI> messagesList;
    private ChatBotActivity parent;

    public ChatBotAdapter(ChatBotActivity parent, List<MessageItemUI> messagesList){
        this.messagesList = messagesList;
        this.parent = parent;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType){
            case 0:
                view = inflater.inflate(R.layout.my_message_item, parent, false);
                return new MyMessageViewHolder(view);
            case 1:
                view = inflater.inflate(R.layout.friend_message_item, parent, false);
                return new FriendMessageViewHolder(view);
            default:
                throw new IllegalArgumentException("Invalid View Type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.myMessage.setText(messagesList.get(holder.getAdapterPosition()).messageText);
        //if this is recipe ID then make clickable
        if (messagesList.get(holder.getAdapterPosition()).isButton()){
            Log.d(this.getClass().toString(), "Im button. my message is " + messagesList.get(holder.getAdapterPosition()).messageText + " my position is " + holder.getAdapterPosition() + " my payload is " + messagesList.get(holder.getAdapterPosition()).getRecipeIDPayload());
            holder.myMessage.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    holder.myMessage.setBackgroundResource(R.drawable.round_button_listen);
                    Log.d(this.getClass().toString(), "IT IS WORKING" + messagesList.get(holder.getAdapterPosition()).getRecipeIDPayload());
                    parent.send_text_to_rasa_and_set_and_speak_reply(messagesList.get(holder.getAdapterPosition()).getRecipeIDPayload());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position){
        return messagesList.get(position).messageType.ordinal();
    }

    void  addResult(String result, MessageItemUI.MessageType type) {
        messagesList.add(new MessageItemUI(result, type));
        notifyItemInserted(messagesList.size() - 1);
    }

    void  addResult(String result, String recipeIDPayload, MessageItemUI.MessageType type) {
        messagesList.add(new MessageItemUI(result, recipeIDPayload, type));
        notifyItemInserted(messagesList.size() - 1);
    }

    public List<MessageItemUI> getResults() {
        return messagesList;
    }


//    View holder classes

    public class MyMessageViewHolder extends MessageViewHolder {
        public MyMessageViewHolder(@NonNull View itemView){
            super(itemView);

            myMessage = itemView.findViewById(R.id.message);
        }
    }

    public class FriendMessageViewHolder extends MessageViewHolder {
        public FriendMessageViewHolder(@NonNull View itemView){
            super(itemView);

            myMessage = itemView.findViewById(R.id.message);
        }
    }
}
