package br.senac.waterreservoir.ui.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import br.senac.waterreservoir.Application;
import br.senac.waterreservoir.R;
import br.senac.waterreservoir.rest.RestClient;
import br.senac.waterreservoir.rest.service.ReservoirService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(Application.SHARED_PREFERENCE, 0);

        //ReservoirService auth = RestClient.getClient().create(ReservoirService.class);
        //auth.index().enqueue();


        TextView text = (TextView) findViewById(R.id.textView);
        text.setText(settings.getString("token", ""));
    }
}
