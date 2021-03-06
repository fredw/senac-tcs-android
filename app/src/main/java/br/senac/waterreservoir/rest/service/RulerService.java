package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RulerService {
    @GET("/devices/{device}/rulers")
    Call<JsonObject> index(@Path("device") String device);
}
