package com.example.vassilis88.fridgechef.Listenerts;

import com.example.vassilis88.fridgechef.Models.RecipeDetailsResponse;

public interface RecipeDetailsListener {
    void didFetch(RecipeDetailsResponse response, String message);
    void didError(String message);
}
