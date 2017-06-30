package br.senac.waterreservoir.rest.service;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FlowSensorDataService {
    @GET("/flow_sensors/{flowSensor}/flow_sensors_data_last")
    Call<JsonObject> last(@Path("flowSensor") String flowSensor);
}
