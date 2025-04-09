package com.example.vassilis88.fridgechef.Listenerts;

import com.example.vassilis88.fridgechef.Models.SimilarRecipeResponse;

import java.util.List;

public interface SimilarRecipesListener {
    void didFetch(List<SimilarRecipeResponse> response, String message);
    void didError(String message);
}
