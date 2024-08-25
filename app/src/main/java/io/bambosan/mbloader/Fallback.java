package io.bambosan.mbloader;

import android.os.Bundle;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Fallback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fallback);
        TextView logOut = findViewById(R.id.logOut);
        String log = getIntent().getStringExtra("LOG_STR");
        logOut.setText(log);
    }
}