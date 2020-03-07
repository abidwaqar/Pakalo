package com.example.pakalo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final private String TAG = MainActivity.class.getName();
    private List<Pair<String, String>> recipe_name_id_list = new ArrayList<>(); //name, id
    private RecyclerView recipe_names_recycler_view;
    private RecipeNamesAdapter recipe_names_adapter;
    private EditText search;

    //recommended recipes recycler view
    private List<Pair<String, String>> recommended_recipe_name_id_list = new ArrayList<>(); //name, id
    public static RecyclerView recommended_recipes_recycler_view;
    private RecipeNamesAdapter recommended_recipes_adapter;

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

                //search only if more than 3 characters
                if (search.getText().toString().length() >= 3){
                    RecipeDB.getInstance().search(search.getText().toString(), update_list_results);
                }
                else {
                    recipe_name_id_list.clear();
                    recipe_names_recycler_view.getAdapter().notifyDataSetChanged();
                    recipe_names_recycler_view.setAlpha(0);
                    updateHistoryRecipes();
                }
            }
        });


        //recommended recipe for user
        recommended_recipes_recycler_view = findViewById(R.id.recommended_recipes_recycler_view);
        recommended_recipes_adapter = new RecipeNamesAdapter(this, recommended_recipe_name_id_list);
        recommended_recipes_recycler_view.setAdapter(recommended_recipes_adapter);
        recommended_recipes_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        updateHistoryRecipes();
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

    public void openChatbotWithRecipe(View view) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.justai.aimybox.assistant");
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "There is no package available in android", Toast.LENGTH_SHORT).show();
        }
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();

        Intent login_intent = new Intent(this, LoginActivity.class);
        startActivity(login_intent);
        finish();
    }
}
