package com.example.vassilis88.fridgechef;

import android.content.Context;

import com.example.vassilis88.fridgechef.Listenerts.AutocompleteIngredientsResponseListener;
import com.example.vassilis88.fridgechef.Listenerts.InstructionsListener;
import com.example.vassilis88.fridgechef.Listenerts.RecipeByIngredientsResponseListener;
import com.example.vassilis88.fridgechef.Listenerts.RecipeDetailsListener;
import com.example.vassilis88.fridgechef.Models.AutocompleteIngredients;
import com.example.vassilis88.fridgechef.Models.InstructionsResponse;
import com.example.vassilis88.fridgechef.Models.RecipeDetailsResponse;
import com.example.vassilis88.fridgechef.Models.RecipesByIngredientsResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RequestManager {
    Context context;
    Retrofit retrofit=new  Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public RequestManager(Context context) {
        this.context = context;
    }

    public void getRecipeDetails(RecipeDetailsListener listener, int id){
        CallRecipeDetails callRecipeDetails= retrofit.create(CallRecipeDetails.class);
        Call<RecipeDetailsResponse> call = callRecipeDetails.callRecipeDetails(id,BuildConfig.SPOONACULAR_API_KEY);
        call.enqueue(new Callback<RecipeDetailsResponse>() {
            @Override
            public void onResponse(Call<RecipeDetailsResponse> call, Response<RecipeDetailsResponse> response) {
                if (!response.isSuccessful()){
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(),response.message());
            }
            @Override
            public void onFailure(Call<RecipeDetailsResponse> call, Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    public void getInstructions(InstructionsListener listener, int id){
        CallInstructions callInstructions= retrofit.create(CallInstructions.class);
        Call<List<InstructionsResponse>> call = callInstructions.callInstructions(id, BuildConfig.SPOONACULAR_API_KEY);
        call.enqueue(new Callback<List<InstructionsResponse>>() {
            @Override
            public void onResponse(Call<List<InstructionsResponse>> call, Response<List<InstructionsResponse>> response) {
                if (!response.isSuccessful()){
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }
            @Override
            public void onFailure(Call<List<InstructionsResponse>> call, Throwable throwable) {
                listener.didError(throwable.getMessage());

            }
        });
    }
    public void getRecipesByIngredients(RecipeByIngredientsResponseListener listener, String ingredients){
        CallRecipesByIngredients callRecipesByIngredients = retrofit.create(CallRecipesByIngredients.class);
        Call<List<RecipesByIngredientsResponse>> call = callRecipesByIngredients.callRecipeByIngredients(
                ingredients, "20", "1", true, BuildConfig.SPOONACULAR_API_KEY
        );

        call.enqueue(new Callback<List<RecipesByIngredientsResponse>>() {
            @Override
            public void onResponse(Call<List<RecipesByIngredientsResponse>> call, Response<List<RecipesByIngredientsResponse>> response) {
                if (!response.isSuccessful()) {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }
            @Override
            public void onFailure(Call<List<RecipesByIngredientsResponse>> call, Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }


    public void getAutoCompleteIngredients(AutocompleteIngredientsResponseListener listener, String query){
        CallAutoCompleteIngredients callAutoCompleteIngredients = retrofit.create(CallAutoCompleteIngredients.class);
        Call<List<AutocompleteIngredients>> call = callAutoCompleteIngredients.callAutocompleteIngredients(
                query,
                5,
                true,
                BuildConfig.SPOONACULAR_API_KEY
        );
        call.enqueue(new Callback<List<AutocompleteIngredients>>() {
            @Override
            public void onResponse(Call<List<AutocompleteIngredients>> call, Response<List<AutocompleteIngredients>> response) {
                if(!response.isSuccessful()){
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }
            @Override
            public void onFailure(Call<List<AutocompleteIngredients>> call, Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private interface CallRecipeDetails{
        @GET("recipes/{id}/information")
        Call<RecipeDetailsResponse> callRecipeDetails(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }
    private interface CallInstructions{
        @GET("recipes/{id}/analyzedInstructions")
        Call<List<InstructionsResponse>> callInstructions(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }
    private interface CallRecipesByIngredients{
        @GET("recipes/findByIngredients")
        Call<List<RecipesByIngredientsResponse>> callRecipeByIngredients(
                @Query("ingredients") String ingredients,
                @Query("number") String number,
                @Query("ranking") String ranking,
                @Query("ignorePantry") boolean ignorePantry,
                @Query("apiKey") String apiKey
        );
    }
    private interface CallAutoCompleteIngredients{
        @GET("food/ingredients/autocomplete")
        Call<List<AutocompleteIngredients>> callAutocompleteIngredients(
                @Query("query") String query,
                @Query("number") int number,
                @Query("metaInformation") boolean metaInformation,
                @Query("apiKey") String apiKey
        );
    }
}