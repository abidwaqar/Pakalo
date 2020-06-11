package com.example.pakalo;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.algolia.search.saas.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RecipeDB is a singleton class, Used to get object name and id from a substring of text.
 */
public class RecipeDB {
    private static RecipeDB recipeDB;

    //algolia
    private Client algolia_client;
    private Index recipes_index;

    //firestore
    private FirebaseFirestore db;
    private CollectionReference recipes;

    private RecipeDB() {
        //algolia auth
		//insert here
        algolia_client = new Client('YourApplicationID', 'YourAdminAPIKey');
        recipes_index = algolia_client.getIndex("recipes");

        //firestore
        db = FirebaseFirestore.getInstance();
        recipes = db.collection("recipes");
    }

    public static RecipeDB getInstance(){
        if (recipeDB == null){
            recipeDB = new RecipeDB();
        }
        return recipeDB;
    }

    //Search for a recipe with string or substring
    public void search(String text, CompletionHandler update_list_results) {
        recipes_index.searchAsync(new Query(text), update_list_results);
    }

    public List<Pair<String, String>> getRecipesFromJson(JSONObject jsonObject)
    {
//        List<String> recipe_list = new ArrayList<>();
        List<Pair<String, String>> recipe_name_id_list = new ArrayList<>();

        try {
            //exception on getJsonArray
            JSONArray hits = jsonObject.getJSONArray("hits");
            for (int i = 0; i< hits.length(); ++i) {
                recipe_name_id_list.add(Pair.create(hits.getJSONObject(i).getString("recipeTitle"),
                        hits.getJSONObject(i).getString("objectID")));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        return recipe_name_id_list;
    }

    /**
     * Parameters: Recipe ID
     * Returns: Recipe Class with all ingredients and instructions
     */
    public DocumentReference getRecipe(String recipeID, OnCompleteListener onCompleteListener){
        DocumentReference docRef = recipes.document(recipeID);
        docRef.get().addOnCompleteListener(onCompleteListener);
        return docRef;
    }

    public Recipe getRecipeFromFirestoreMapData(Map<String, Object> recipeData, String recipeID){
        return new Recipe((String) recipeID,  (String) recipeData.get("title"), (List<String>) recipeData.get("ingredients"),
                (List<String>) recipeData.get("instructions"));
    }
}
