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
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Intent recipe_intent = getIntent();
        String recipeID = recipe_intent.getStringExtra(RecipeNamesAdapter.RECIPE_ID_MESSAGE);

        recipeName = findViewById(R.id.recipe_name_ar);
        recipeIngredients = findViewById(R.id.ingredients_list_ar);
        recipeInstructions = findViewById(R.id.instructions_list_ar);

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
        String mimeType = "text/plain";
//        ShareCompat.IntentBuilder
//                .from(this)
//                .setType(mimeType)
//                .setChooserTitle("Share this text with: ")
//                .setText(txt)
//                .startChooser();


        //add recipe viewed to user recipe viewed field
        OnCompleteListener onCompleteListener = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> recipe_viewed =  (ArrayList<String>) document.get("recipe_viewed");
                        if (!recipe_viewed.contains(recipe.get_recipe_ID())){
                            recipe_viewed.add(recipe.get_recipe_ID());
                            User.getInstance().update_recipe_viewed(recipe_viewed);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        };

        User.getInstance().get_user_collection(onCompleteListener);

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.justai.aimybox.assistant");
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "There is no package available in android", Toast.LENGTH_SHORT).show();
        }
    }
}
