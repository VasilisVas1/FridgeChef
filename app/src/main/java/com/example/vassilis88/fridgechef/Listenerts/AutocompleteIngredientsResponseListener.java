package com.example.vassilis88.fridgechef.Listenerts;

import com.example.vassilis88.fridgechef.Models.AutocompleteIngredients;

import java.util.List;

public interface AutocompleteIngredientsResponseListener {
    void didFetch(List<AutocompleteIngredients> response, String message);

    void didError(String message);
}
