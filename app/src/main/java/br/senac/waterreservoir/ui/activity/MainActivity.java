package br.senac.waterreservoir.ui.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import br.senac.waterreservoir.R;
import br.senac.waterreservoir.model.Reservoir;
import br.senac.waterreservoir.rest.RestClient;
import br.senac.waterreservoir.rest.service.ReservoirService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reservoir spinner
        final Spinner reservoirSpinner = (Spinner) findViewById(R.id.reservoirSpinner);

        // Get reservoirs
        ReservoirService auth = RestClient.getClient(this).create(ReservoirService.class);
        auth.index().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonArray data = response.body().getAsJsonArray("data");

                    // Read json data and populate on array
                    List<Reservoir> reservoirs = new ArrayList<>();
                    for (JsonElement r : data) {
                        JsonObject reservoir = r.getAsJsonObject();
                        reservoirs.add(new Reservoir(
                            reservoir.get("id").toString(),
                            reservoir.getAsJsonObject("attributes").get("name").toString()
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
                // @TODO: fazer algo com o valor seelecionado do spinner
                Toast.makeText(getApplicationContext(), reservoir.getId() + "#" + reservoir.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void share(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        TextView sensorFlow = (TextView) findViewById(R.id.sensorFlow);
        TextView ruler = (TextView) findViewById(R.id.ruler);

        String text = "Vazão: " + sensorFlow.getText().toString() + "l/min Régua: " + ruler.getText().toString() + "l";

        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
