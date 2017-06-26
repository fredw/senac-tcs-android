package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ReservoirService {
    @GET("/reservoirs")
    Call<JsonObject> signIn();
}
