package com.example.pakalo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class User {
    private static User user;
    public FirebaseUser firebaseUser;
    public FirebaseFirestore firebaseFirestoreDB;
    private final String TAG = User.class.getName();
    public DocumentReference documentReference;

    private User(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestoreDB = FirebaseFirestore.getInstance();
        documentReference = this.firebaseFirestoreDB.collection("users")
                .document(firebaseUser.getUid());
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

    public void update_recipe_viewed(ArrayList<String> recipe_viewed){
        //update recipe_viewed field
        documentReference.update("recipe_viewed", recipe_viewed)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

}
