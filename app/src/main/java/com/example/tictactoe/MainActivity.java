package com.example.tictactoe;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.Group;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button soloButton = findViewById(R.id.button_solo);
        Button multiButton = findViewById(R.id.button_multi);

        soloButton.setOnClickListener(v -> {
            Group difficulty = findViewById(R.id.group_difficulty);
            difficulty.setVisibility(View.VISIBLE);

            Button normal = findViewById(R.id.button_difficulty1);
            Button hard = findViewById(R.id.button_difficulty2);
            normal.setOnClickListener(view ->
                    startActivity(new Intent(MainActivity.this, GameActivity.class)
                            .putExtra("solo", true)
                            .putExtra("difficulty", 3))
            );
            hard.setOnClickListener(view ->
                    startActivity(new Intent(MainActivity.this, GameActivity.class)
                            .putExtra("solo", true)
                            .putExtra("difficulty", 9))
            );
        });

        multiButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GameActivity.class).putExtra("solo", false))
        );
    }

    @Override
    public void onStart() {
        super.onStart();

        Group difficulty = findViewById(R.id.group_difficulty);
        difficulty.setVisibility(View.INVISIBLE);
    }
}