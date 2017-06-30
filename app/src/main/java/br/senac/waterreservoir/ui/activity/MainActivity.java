package br.senac.waterreservoir.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.senac.waterreservoir.R;
import br.senac.waterreservoir.model.Device;
import br.senac.waterreservoir.model.Reservoir;
import br.senac.waterreservoir.rest.RestClient;
import br.senac.waterreservoir.rest.service.DeviceService;
import br.senac.waterreservoir.rest.service.FlowSensorDataService;
import br.senac.waterreservoir.rest.service.FlowSensorService;
import br.senac.waterreservoir.rest.service.ReservoirService;
import br.senac.waterreservoir.rest.service.RulerDataService;
import br.senac.waterreservoir.rest.service.RulerService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private View loadingView;
    private View mainContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingView = findViewById(R.id.loading);
        mainContentView = findViewById(R.id.main_content);

        // Reservoir spinner
        final Spinner reservoirSpinner = (Spinner) findViewById(R.id.reservoirSpinner);
        // Reservoir spinner
        final Spinner deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);

        // Get reservoirs
        ReservoirService reservoirService = RestClient.getClient(this).create(ReservoirService.class);
        reservoirService.index().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonArray data = response.body().getAsJsonArray("data");

                    // Read json data and populate on array
                    List<Reservoir> reservoirs = new ArrayList<>();
                    for (JsonElement r : data) {
                        JsonObject reservoir = r.getAsJsonObject();
                        reservoirs.add(new Reservoir(
                            reservoir.get("id").getAsString(),
                            reservoir.getAsJsonObject("attributes").get("name").getAsString()
                        ));
                    }

                    // Populate spinner with the array adapter
                    ArrayAdapter<Reservoir> spinnerArrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, reservoirs);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    reservoirSpinner.setAdapter(spinnerArrayAdapter);
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Reservoir: on select an item
        reservoirSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Reservoir reservoir = (Reservoir) parent.getSelectedItem();

                // Get devices
                DeviceService deviceService = RestClient.getClient(getBaseContext()).create(DeviceService.class);
                deviceService.index(reservoir.getId()).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            JsonArray data = response.body().getAsJsonArray("data");

                            // Read json data and populate on array
                            List<Device> devices = new ArrayList<>();
                            for (JsonElement r : data) {
                                JsonObject device = r.getAsJsonObject();
                                devices.add(new Device(
                                    device.get("id").getAsString(),
                                    device.getAsJsonObject("attributes").get("name").getAsString()
                                ));
                            }

                            // Populate spinner with the array adapter
                            ArrayAdapter<Device> spinnerArrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, devices);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            deviceSpinner.setAdapter(spinnerArrayAdapter);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Device: on select an item
        deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Device device = (Device) parent.getSelectedItem();

                // Get flow sensors
                FlowSensorService flowSensorService = RestClient.getClient(getBaseContext()).create(FlowSensorService.class);
                flowSensorService.index(device.getId()).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            JsonArray data = response.body().getAsJsonArray("data");
                            if (data.size() == 0) {
                                setFlowSensorConsumption(null);
                                return;
                            }
                            // @TODO: the best approach is to handle all flow sensors and not only the first...
                            // Get first data flow sensor
                            JsonObject flowSensor = data.get(0).getAsJsonObject();
                            String flowSensorId = flowSensor.get("id").getAsString();

                            // Get last flow sensor data
                            FlowSensorDataService flowSensorDataService = RestClient.getClient(getBaseContext()).create(FlowSensorDataService.class);
                            flowSensorDataService.last(flowSensorId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful()) {
                                        JsonObject data = response.body().getAsJsonObject("data");
                                        if (data == null) {
                                            setFlowSensorConsumption(null);
                                            return;
                                        }
                                        setFlowSensorConsumption(data.get("consumption_per_minute").getAsDouble());
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                // Get rulers
                RulerService rulerService = RestClient.getClient(getBaseContext()).create(RulerService.class);
                rulerService.index(device.getId()).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            JsonArray data = response.body().getAsJsonArray("data");
                            if (data.size() == 0) {
                                setLevelSensorVolume(null);
                                return;
                            }
                            // @TODO: the best approach is to handle all rulers and not only the first...
                            // Get first data ruler
                            JsonObject ruler = data.get(0).getAsJsonObject();
                            String rulerId = ruler.get("id").getAsString();

                            // Get last ruler data
                            RulerDataService rulerDataService = RestClient.getClient(getBaseContext()).create(RulerDataService.class);
                            rulerDataService.last(rulerId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                                    if (response.isSuccessful()) {
                                        JsonObject data = response.body().getAsJsonObject("data");
                                        if (data == null) {
                                            setLevelSensorVolume(null);
                                            return;
                                        }

                                        // Read all level sensors to get last turned on level
                                        JsonArray levelSensors = data.getAsJsonArray("level_sensor_data");
                                        boolean switchedOn;
                                        int i = 0;
                                        Double volume = .0;
                                        do {
                                            JsonObject levelSensor = levelSensors.get(i).getAsJsonObject();
                                            switchedOn = levelSensor.get("switched_on").getAsBoolean();
                                            if (switchedOn) {
                                                volume = levelSensor.getAsJsonObject("level_sensor").get("volume").getAsDouble();
                                            }
                                            i++;
                                        } while (switchedOn);

                                        setLevelSensorVolume(volume);
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setFlowSensorConsumption(Double consumption) {
        TextView dataTextView = (TextView) findViewById(R.id.flowSensorData);
        TextView unitTextView = (TextView) findViewById(R.id.flowSensorUnit);
        if (consumption == null) {
            dataTextView.setText("-");
            unitTextView.setVisibility(View.INVISIBLE);
        } else {
            dataTextView.setText(new DecimalFormat("#.##").format(consumption));
            unitTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setLevelSensorVolume(Double volume) {
        TextView dataTextView = (TextView) findViewById(R.id.levelSensorData);
        TextView unitTextView = (TextView) findViewById(R.id.levelSensorUnit);
        if (volume == null) {
            dataTextView.setText("-");
            unitTextView.setVisibility(View.INVISIBLE);
        } else {
            dataTextView.setText(new DecimalFormat("#.##").format(volume));
            unitTextView.setVisibility(View.VISIBLE);
        }
    }

    public void showPopupFlowSensor(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.flow_sensor_actions, popup.getMenu());
        popup.show();
    }

    public void showPopupLevelSensor(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.level_sensor_actions, popup.getMenu());
        popup.show();
    }

    public void share(MenuItem item) {
        String text = "";
        TextView dataTextView;
        switch (item.getItemId()) {
            case R.id.action_flow_sensor:
                dataTextView = (TextView) findViewById(R.id.flowSensorData);
                text = "Flow rate: " + dataTextView.getText().toString() + " " + getResources().getString(R.string.liter_per_minute);
                break;
            case R.id.action_level_sensor:
                dataTextView = (TextView) findViewById(R.id.levelSensorData);
                text = "Level: " + dataTextView.getText().toString() + " " + getResources().getString(R.string.meter_cubic);
                break;
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    /*
     * Shows the progress UI and hides main information
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showLoading(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mainContentView.setVisibility(show ? View.GONE : View.VISIBLE);
        mainContentView
                .animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    mainContentView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingView
                .animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
