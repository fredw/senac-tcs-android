package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RulerDataService {
    @GET("/rulers/{ruler}/rulers_data_last")
    Call<JsonObject> last(@Path("ruler") String ruler);
}
