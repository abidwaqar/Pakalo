package com.example.pakalo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.CompletionHandler;
import com.example.pakalo.chatbot.ChatBotActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final private String TAG = MainActivity.class.getName();
    private List<Pair<String, String>> recipe_name_id_list = new ArrayList<>(); //name, id
    private RecyclerView recipe_names_recycler_view;
    private RecipeNamesAdapter recipe_names_adapter;
    private EditText search;
    private Timer timer;

    //recommended recipes recycler view
    private List<Pair<String, String>> recommended_recipe_name_id_list = new ArrayList<>(); //name, id
    public static RecyclerView recommended_recipes_recycler_view;
    private RecipeNamesAdapter recommended_recipes_adapter;

    //Liked recipes recycler view
    private List<Pair<String, String>> liked_recipe_name_id_list = new ArrayList<>(); //name, id
    public static RecyclerView liked_recipes_recycler_view;
    private RecipeNamesAdapter liked_recipes_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = findViewById(R.id.search);

        //connect recycler view to adapter
        recipe_names_recycler_view = findViewById(R.id.recipe_names_recycler_view);
        recipe_names_adapter = new RecipeNamesAdapter(this, recipe_name_id_list);
        recipe_names_recycler_view.setAdapter(recipe_names_adapter);
        recipe_names_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recipe_names_recycler_view.setAlpha(0);

        //create firestore fields if not created
        create_firebase_fields();

        //Update recipe list as user types
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update adapter items
                CompletionHandler update_list_results = new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                        Log.d(TAG, jsonObject.toString());
                        recipe_name_id_list.clear();
                        recipe_name_id_list.addAll(RecipeDB.getInstance().getRecipesFromJson(jsonObject));
                        Log.d(TAG, recipe_name_id_list.toString());
                        recipe_names_recycler_view.getAdapter().notifyDataSetChanged();
                        if (recipe_name_id_list.size() > 0){
                            recipe_names_recycler_view.setAlpha(1);
                        }
                        else{
                            recipe_names_recycler_view.setAlpha(0);
                        }
                    }
                };

                //callback for delay search
                TimerTask update_recipe_name_callback = new TimerTask() {
                    @Override
                    public void run() {
                        if (search.getText().toString().length() != 0)
                            RecipeDB.getInstance().search(search.getText().toString(), update_list_results);
                        else {
                            recipe_name_id_list.clear();

                            //The thing that updates UI should be in this method
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recipe_names_recycler_view.getAdapter().notifyDataSetChanged();
                                    recipe_names_recycler_view.setAlpha(0);
                                }
                            });
                            updateHistoryRecipes();
                            updateLikedRecipes();
                        }
                    }
                };

                //if length not 0, send search request after 300 ms delay to algolia after every keystroke
                timer = new Timer();
                timer.schedule(update_recipe_name_callback, 300); // 300ms delay before the timer executes the „run“ method from TimerTask

                //search only if more than 3 characters
//                if (search.getText().toString().length() == 0){
//                    recipe_name_id_list.clear();
//                    recipe_names_recycler_view.getAdapter().notifyDataSetChanged();
//                    recipe_names_recycler_view.setAlpha(0);
//                    updateHistoryRecipes();
//                }
            }
        });


        //recommended recipe for user
        recommended_recipes_recycler_view = findViewById(R.id.recommended_recipes_recycler_view);
        recommended_recipes_adapter = new RecipeNamesAdapter(this, recommended_recipe_name_id_list);
        recommended_recipes_recycler_view.setAdapter(recommended_recipes_adapter);
        recommended_recipes_recycler_view.setLayoutManager(new LinearLayoutManager(this));


        //liked recipe for user
        liked_recipes_recycler_view = findViewById(R.id.liked_recipes_recycler_view);
        liked_recipes_adapter = new RecipeNamesAdapter(this, liked_recipe_name_id_list);
        liked_recipes_recycler_view.setAdapter(liked_recipes_adapter);
        liked_recipes_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        //adds recipe to history and liked recipes
        updateHistoryRecipes();
        updateLikedRecipes();
    }


    public void updateHistoryRecipes(){
        //display recipes
        //adds history recipes to list
        OnCompleteListener addRecipeNamesToRecommenderList = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Pair pair = Pair.create(document.get("title").toString(), document.getId());
                        if (!recommended_recipe_name_id_list.contains(pair))
                        {
                            recommended_recipe_name_id_list.add(0, pair);
                            recommended_recipes_recycler_view.getAdapter().notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        //gets user history recipes form firebase
        OnCompleteListener onCompleteListener = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> recipe_viewed =  (ArrayList<String>) document.get("recipe_viewed");
                        for (int i =0 ; i< recipe_viewed.size(); ++i)
                        {
                            RecipeDB.getInstance().getRecipe(recipe_viewed.get(i), addRecipeNamesToRecommenderList);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        //get user table
        User.getInstance().get_user_collection(onCompleteListener);
    }

    public void updateLikedRecipes(){
        //display recipes
        //adds liked recipes to list
        OnCompleteListener addRecipeNamesToLikedList = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Pair pair = Pair.create(document.get("title").toString(), document.getId());
                        if (!liked_recipe_name_id_list.contains(pair))
                        {
                            liked_recipe_name_id_list.add(0, pair);
                            liked_recipes_recycler_view.getAdapter().notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        //gets user liked recipes form firebase
        OnCompleteListener onCompleteListener = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> recipe_liked =  (ArrayList<String>) document.get("recipe_liked");
                        for (int i =0 ; i< recipe_liked.size(); ++i)
                        {
                            RecipeDB.getInstance().getRecipe(recipe_liked.get(i), addRecipeNamesToLikedList);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        //get user table
        User.getInstance().get_user_collection(onCompleteListener);
    }

    public void create_firebase_fields(){
        OnCompleteListener create_firebase_fields_listener = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
//                        ArrayList<String> recipe_liked =  (ArrayList<String>) document.get("recipe_liked");
                        String field_name = "recipe_liked";
                        if (document.get(field_name) == null) {
                            //create field
                            User.getInstance().create_field_in_firestore(field_name);
                        }
                        field_name = "recipe_viewed";
                        if (document.get(field_name) == null) {
                            //create field
                            User.getInstance().create_field_in_firestore(field_name);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                        User.getInstance().create_user_document();
                        create_firebase_fields();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        User.getInstance().get_user_collection(create_firebase_fields_listener);
    }

    public void openChatbotWithRecipe(View view) {
        //Explicit intent to chatbot
        Intent launchChatbot = new Intent(this, ChatBotActivity.class);
        startActivity(launchChatbot);
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();

        Intent login_intent = new Intent(this, LoginActivity.class);
        startActivity(login_intent);
        finish();
    }
}
