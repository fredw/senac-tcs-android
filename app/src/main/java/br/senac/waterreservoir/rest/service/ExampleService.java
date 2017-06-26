package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ExampleService {
    @GET("/users/{user}/repos")
    Call<List<JsonObject>> reposForUser(@Path("user") String user);
}
