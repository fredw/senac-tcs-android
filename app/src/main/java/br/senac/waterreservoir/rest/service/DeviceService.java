package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DeviceService {
    @GET("/reservoirs/{reservoir}/devices")
    Call<JsonObject> index(@Path("reservoir") String reservoir);
}
