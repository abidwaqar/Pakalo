package com.example.pakalo;

import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private static User user;
    public FirebaseUser firebaseUser;
    public FirebaseFirestore firebaseFirestoreDB;
    private final String TAG = User.class.getName();
    public DocumentReference documentReference;
    private String userUid;

    private User(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestoreDB = FirebaseFirestore.getInstance();
        documentReference = this.firebaseFirestoreDB.collection("users")
                .document(firebaseUser.getUid());
        userUid = firebaseUser.getUid();
    }

    public static User getInstance(){
        if (user == null){
            user = new User();
        }
        return user;
    }

    public void get_user_collection(OnCompleteListener onCompleteListener){
        documentReference.get().addOnCompleteListener(onCompleteListener);
    }

    /***
     * adds the recipeID (Which is given in parameter) in the viewed recipe list in firestore
     * @param recipe_viewed
     */
    public void add_recipe_in_viewed_list_in_firestore(String recipe_viewed){
        //update recipe_viewed field
        documentReference.update("recipe_viewed", FieldValue.arrayUnion(recipe_viewed));
        //TODO if failed then create recipe viewed
    }

    /***
     * adds the recipeID (Which is given in parameter) in the liked recipe list in firestore
     * @param recipeID_liked
     */
    public void add_recipe_in_liked_list_in_firestore(String recipeID_liked){
        //update recipe_viewed field
        documentReference.update("recipe_liked", FieldValue.arrayUnion(recipeID_liked));
        //TODO if failed then create recipe liked
    }

    /***
     * changed likeImageButton background if the recipe is liked
     * @param recipeID
     */
    public void recipe_is_liked(String recipeID, ImageButton likeImageButton){
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> recipe_liked =  (ArrayList<String>) document.get("recipe_liked");
                        if (recipe_liked != null && recipe_liked.contains(recipeID)){
                            likeImageButton.setBackgroundResource(R.drawable.round_button_listen);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public String getUid(){
        return userUid;
    }

    public void create_field_in_firestore(String field_name){
        Map<String, Object> array_field = new HashMap<>();
        array_field.put(field_name, new ArrayList<>());
        documentReference.set(array_field, SetOptions.merge());
    }

    public void create_user_document(){
        Map<String, Object> array_field = new HashMap<>();
        array_field.put("name", new ArrayList<>());
        documentReference.set(array_field, SetOptions.merge());
    }
}