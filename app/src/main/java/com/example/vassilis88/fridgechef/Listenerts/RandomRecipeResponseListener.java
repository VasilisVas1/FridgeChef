package com.example.vassilis88.fridgechef.Listenerts;

import com.example.vassilis88.fridgechef.Models.RandomRecipeApiResponse;

public interface RandomRecipeResponseListener {

    void didFetch(RandomRecipeApiResponse response, String message);
    void didError(String message);
}
