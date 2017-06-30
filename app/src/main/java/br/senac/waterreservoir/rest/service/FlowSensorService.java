package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FlowSensorService {
    @GET("/devices/{device}/flow_sensors")
    Call<JsonObject> index(@Path("device") String device);
}
