package com.example.pakalo.chatbot;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pakalo.R;
import com.example.pakalo.RecipeActivity;
import com.example.pakalo.User;
import com.example.pakalo.retrofit.JsonPlaceHolderApi;
import com.example.pakalo.retrofit.RetroChatbotRequest;
import com.example.pakalo.retrofit.RetroChatbotResponse;
import com.example.pakalo.retrofit.RetrofitClientInstance;
import com.example.pakalo.speechToText.MessageDialogFragment;
import com.example.pakalo.speechToText.SpeechService;
import com.example.pakalo.speechToText.VoiceRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatBotActivity extends AppCompatActivity implements MessageDialogFragment.Listener {

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static final String STATE_RESULTS = "results";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    private VoiceRecorder mVoiceRecorder;
    // Resource caches
    private int mColorHearing;
    private int mColorNotHearing;
    // View references
    private TextView mStatus;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };
    private TextView mText;
    private RecyclerView chatbot_RV;
    private ChatBotAdapter chatbot_Ad;
    private List<MessageItemUI> messageList;
    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    send_text_to_rasa_and_set_and_speak_reply(text);
                                    mText.setText(null);
                                    chatbot_Ad.addResult(text, MessageItemUI.MessageType.TYPE_MY_MESSAGE);
                                    chatbot_RV.smoothScrollToPosition(messageList.size() - 1);
                                } else {
                                    mText.setText(text);
                                }
                            }
                        });
                    }
                }
            };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            mStatus.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };
    private Retrofit retrofit;
    private JsonPlaceHolderApi jsonPlaceHolderApi;
    private TextToSpeech textToSpeech;
    private String UserUid = User.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        final Resources resources = getResources();
        final Resources.Theme theme = getTheme();
        mColorHearing = ResourcesCompat.getColor(resources, R.color.colorBox, theme);
        mColorNotHearing = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme);

//        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mStatus = findViewById(R.id.status);
        mText = findViewById(R.id.text);

//        Chatbot chat messagelist, Adapter and recycler view
        messageList = new ArrayList<MessageItemUI>();
        chatbot_RV = findViewById(R.id.chatbot_chat_RV);
        chatbot_Ad = new ChatBotAdapter(this, messageList);
        chatbot_RV.setAdapter(chatbot_Ad);
        chatbot_RV.setLayoutManager(new LinearLayoutManager(this));

//        Rasa Api
        jsonPlaceHolderApi = RetrofitClientInstance.getRetrofitInstance().create(JsonPlaceHolderApi.class);

//        android text to speech api
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if(status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);

                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d("TextToSpeech","On Start");
                        stopVoiceRecorder();
                        mText.setText(null);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d("TextToSpeech","On Done");
                        mText.setText(null);
                        startVoiceRecorder();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.d("TextToSpeech","On Error");
                    }
                });

            }else {
                Log.i("TextToSpeech","Initialization Failed");
            }
        });

        //Send RecipeID to rasa if chatbotactivity is accessed with recipeID as extra data in intent
        Bundle extra = getIntent().getExtras();
        if (extra != null && extra.getString(RecipeActivity.RECIPEID) != null){
            //send to rasa
            String formattedRecipeID = "/recipe_id_from_button{\"recipe_id\":\"" + extra.getString(RecipeActivity.RECIPEID) + "\"}";
            send_text_to_rasa_and_set_and_speak_reply(formattedRecipeID);
            Log.d(this.getClass().toString(), formattedRecipeID);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

//    TODO this
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (chatbot_Ad != null) {
//            outState.putStringArrayList(STATE_RESULTS, chatbot_Ad.getResults());
//        }
//    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_file:
//                mSpeechService.recognizeInputStream(getResources().openRawResource(R.raw.audio));
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }

        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatus.setTextColor(hearingVoice ? mColorHearing : mColorNotHearing);
                if (hearingVoice) {
                    mStatus.setBackgroundResource(R.drawable.round_button_listen);
                } else {
                    mStatus.setBackgroundResource(R.drawable.round_button);
                }
            }
        });
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    //Sends Message to rasa api and sets response to chatbot recycler view
    public void send_text_to_rasa_and_set_and_speak_reply(String message_text) {
        //Send the finalized text to Rasa and show responsoe
        RetroChatbotRequest retroChatbotRequest = new RetroChatbotRequest(UserUid, message_text);
        Call<List<RetroChatbotResponse>> call = jsonPlaceHolderApi.getReplies(retroChatbotRequest);
        call.enqueue(new Callback<List<RetroChatbotResponse>>() {
            @Override
            public void onResponse(Call<List<RetroChatbotResponse>> call, Response<List<RetroChatbotResponse>> response) {
//                error
                if (!response.isSuccessful()) {
                    Log.d(this.getClass().toString(), response.code() + "404");
                    return;
                }

//                SUCCESS
                //First of all stop voice recorder
                stopVoiceRecorder();
                mText.setText(null);

                //Show and speak the response
                List<RetroChatbotResponse> replies = response.body();
                String textToSpeak = "";
                for (RetroChatbotResponse reply : replies) {
//                    Log.d(this.getClass().toString(), reply.getText());
                    chatbot_Ad.addResult(reply.getText(), MessageItemUI.MessageType.TYPE_FRIEND_MESSAGE);
                    chatbot_RV.smoothScrollToPosition(messageList.size() - 1);
//                    textToSpeech(reply.getText());
                    textToSpeak = textToSpeak + "\n" +reply.getText();
                    if (reply.getButtons() != null){
                        for (RetroChatbotResponse.Button button: reply.getButtons()){
                            chatbot_Ad.addResult(button.getTitle(), button.getRecipeIDPayload(), MessageItemUI.MessageType.TYPE_FRIEND_MESSAGE);
                            chatbot_RV.smoothScrollToPosition(messageList.size() - 1);
//                            textToSpeech(button.getTitle());
                            textToSpeak = textToSpeak + "\n" + button.getTitle();
                            Log.d(this.getClass().toString(), button.getTitle() + button.getRecipeIDPayload());
                        }
                    }
                }
                textToSpeech(textToSpeak);
            }

            @Override
            public void onFailure(Call<List<RetroChatbotResponse>> call, Throwable t) {
                Log.d(this.getClass().toString(), t.getMessage());
            }
        });
    }

    private void textToSpeech(String text){
        //Stop the recording before saying something
        stopVoiceRecorder();
        mText.setText(null);

        //speak
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, text);
    }

//    public void isTTSSpeaking(){
//
//        final Handler h =new Handler();
//
//        Runnable r = new Runnable() {
//
//            public void run() {
//
//                if (!textToSpeech.isSpeaking()) {
//                    startVoiceRecorder();
//                }
//                stopVoiceRecorder();
//                h.postDelayed(this, 1000);
//            }
//        };
//
//        h.postDelayed(r, 1000);
//    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(parent);
//            super(inflater.inflate(R.layout.item_result, parent, false));
            text = itemView.findViewById(R.id.text);
        }

    }

    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText(mResults.get(position));
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        void addResult(String result) {
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

    }

}
