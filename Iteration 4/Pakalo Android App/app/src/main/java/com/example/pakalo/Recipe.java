package com.example.pakalo;

import java.util.List;
import java.util.Map;

//Recipe class for holding a single recipe
public class Recipe {
    private String name;
    private List<String> ingredients;
    private List<String> instructions;
    private String recipeID;

    public Recipe(String recipeID, String name, List<String> ingredients, List<String> instructions){
        this.recipeID = recipeID;
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String get_recipe_name(){
        return this.name;
    }

    public String get_recipe_ID(){
        return this.recipeID;
    }

    public List<String> get_all_ingredients(){
        return this.ingredients;
    }

    public List<String> get_all_instructions(){
        return this.instructions;
    }

    public String get_ingredient(int index){
        if (index >=0 )
            return this.ingredients.get(index);
        return "There are only " + (ingredients.size() + 1) + " ingredients in this recipe";
    }

    public String get_instruction(int index){
        if (index >=0)
            return this.instructions.get(index);
        return "There are only " + (ingredients.size() + 1) + " instructions in this recipe";
    }

    //Returns all ingredients in numbered form
    public String get_all_ingredients_numbered(){
        String ingredients_str = new String();
        for (int i = 0; i < this.ingredients.size(); ++i){
            ingredients_str += Integer.toString(i+1) + ") " + this.ingredients.get(i) + "\n";
        }
        return ingredients_str;
    }

    //Returns all ingredients in numbered form
    public String get_all_instructions_numbered(){
        String instructions_str = new String();
        for (int i = 0; i < this.instructions.size(); ++i){
            instructions_str += Integer.toString(i+1) + ") " + this.instructions.get(i) + "\n";
        }
        return instructions_str;
    }
}