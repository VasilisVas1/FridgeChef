package com.example.vassilis88.fridgechef.Listenerts;

import com.example.vassilis88.fridgechef.Models.RecipesByIngredientsResponse;

import java.util.List;

public interface RecipeByIngredientsResponseListener {

    void didFetch(List<RecipesByIngredientsResponse> response, String message);

    void didError(String message);
}
