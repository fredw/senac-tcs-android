package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {
    @POST("/users/sign_in")
    Call<JsonObject> signIn(@Body JsonObject body);

    @POST("/users/password")
    Call<JsonObject> passwordRecover(@Body JsonObject body);
}
