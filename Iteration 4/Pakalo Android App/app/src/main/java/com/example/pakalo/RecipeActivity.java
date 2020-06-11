package com.example.pakalo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pakalo.chatbot.ChatBotActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipeActivity extends AppCompatActivity {
    private final String TAG = RecipeActivity.class.getName();
    private Recipe recipe;
    private TextView recipeName;
    private TextView recipeIngredients;
    private TextView recipeInstructions;
    private DocumentReference recipe_reference;
    private ImageButton likeImageButton;
    static public String RECIPEID = "RECIPEID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Intent recipe_intent = getIntent();
        String recipeID = recipe_intent.getStringExtra(RecipeNamesAdapter.RECIPE_ID_MESSAGE);

        recipeName = findViewById(R.id.recipe_name_ar);
        recipeIngredients = findViewById(R.id.ingredients_list_ar);
        recipeInstructions = findViewById(R.id.instructions_list_ar);
        likeImageButton = findViewById(R.id.like_button);

        OnCompleteListener dataToRecipe = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        recipe = RecipeDB.getInstance().getRecipeFromFirestoreMapData(document.getData(), recipeID);
                        recipeName.setText(recipe.get_recipe_name());
                        recipeIngredients.setText(recipe.get_all_ingredients_numbered());
                        recipeInstructions.setText(recipe.get_all_instructions_numbered());

                        //checks if recipe is liked and changes the background respectively
                        User.getInstance().recipe_is_liked(recipe.get_recipe_ID(), likeImageButton);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        recipe_reference = RecipeDB.getInstance().getRecipe(recipeID, dataToRecipe);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width), (int)(height * 0.7));

        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = height - (int)(height * 0.7);

        getWindow().setAttributes(params);
    }

    public void openChatbotWithRecipe(View view) {
        String txt = this.recipe.get_recipe_name();

        //add recipe viewed to user recipe viewed field in firestore
        User.getInstance().add_recipe_in_viewed_list_in_firestore(recipe.get_recipe_ID());

        //Open chatbot activity
        Intent launchChatbot = new Intent(this, ChatBotActivity.class);
        launchChatbot.putExtra(RECIPEID, this.recipe.get_recipe_ID());
        startActivity(launchChatbot);
    }

    public void likeRecipe(View view) {
        //add recipeID to user->liked recipe collection in firestore
        User.getInstance().add_recipe_in_liked_list_in_firestore(this.recipe.get_recipe_ID());

        //checks if recipe is liked and changes the background respectively
        User.getInstance().recipe_is_liked(recipe.get_recipe_ID(), likeImageButton);
    }
}
