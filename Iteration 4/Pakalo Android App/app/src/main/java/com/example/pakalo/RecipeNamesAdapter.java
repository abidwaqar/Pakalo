package com.example.pakalo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class RecipeNamesAdapter extends RecyclerView.Adapter<RecipeNamesAdapter.RecipeNameHolder> {
    private List<Pair<String, String>> recipe_name_id_list; //name, id
    private LayoutInflater inflater;
    public static final String RECIPE_ID_MESSAGE = "com.example.pakalo.RECIPE_ID";

    public RecipeNamesAdapter(Context context, List<Pair<String, String>> recipe_name_id_list){
        inflater = LayoutInflater.from(context);
        this.recipe_name_id_list = recipe_name_id_list;
    }

    @NonNull
    @Override
    public RecipeNameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)  {
        View recipe_name_view = inflater.inflate(R.layout.recipe_name_item, parent, false);
        return new RecipeNameHolder(recipe_name_view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeNameHolder holder, int position) {
        holder.recipeName.setText(recipe_name_id_list.get(holder.getAdapterPosition()).first);
        holder.recipeID = recipe_name_id_list.get(holder.getAdapterPosition()).second;
    }

    @Override
    public int getItemCount() {
        return recipe_name_id_list.size();
    }

    public class RecipeNameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView recipeName;
        private String recipeID;
        final RecipeNamesAdapter adapter;
        private final String TAG = RecipeNamesAdapter.class.getName();

        public RecipeNameHolder(@NonNull View itemView, RecipeNamesAdapter adapter) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipe_name);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //intent to the recipe clicked
            Intent recipe_intent = new Intent(v.getContext(), RecipeActivity.class);
            recipe_intent.putExtra(RECIPE_ID_MESSAGE, recipeID);
            v.getContext().startActivity(recipe_intent);
        }
    }
}
