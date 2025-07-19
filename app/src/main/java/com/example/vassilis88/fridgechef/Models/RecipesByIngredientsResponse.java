package com.example.vassilis88.fridgechef.Models;

import java.util.ArrayList;

public class RecipesByIngredientsResponse {
    public int id;
    public String image;
    public int missedIngredientCount;
    public ArrayList<MissedIngredient> missedIngredients;
    public String title;
}
