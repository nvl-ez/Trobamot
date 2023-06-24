package com.example.trobamot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.TextView;

import com.google.android.material.resources.TextAppearance;

public class PantallaFinal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Obtenemos dimensiones de la pantalla
        // Object to store display information
        DisplayMetrics metrics = new DisplayMetrics();
        // Get display information
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthDisplay = metrics.widthPixels;
        int heightDisplay = metrics.heightPixels;

        //Obtenemos el layout
        setContentView(R.layout.activity_pantall_final);
        ConstraintLayout constraintLayout = findViewById(R.id.resultado);
        Intent intent = getIntent();

        //Añadir texto de victoria o derrota
        TextView estadoPartida = new TextView(this);
        constraintLayout.addView(estadoPartida);
        if(intent.getBooleanExtra("victoria",true)){
            estadoPartida.setText("Enhorabona!");
        } else{
            estadoPartida.setText("Oh oh oh oh...");
        }
        estadoPartida.setY(50);
        estadoPartida.setWidth(widthDisplay);
        estadoPartida.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        estadoPartida.setTextSize(35);

        //Añadir palabra
        TextView word = new TextView(this);
        constraintLayout.addView(word);
        word.setText(intent.getStringExtra("palabra"));
        word.setY(175);
        word.setWidth(widthDisplay);
        word.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        word.setTextSize(25);



        //Añadir definicion
        TextView definicion = new TextView(this);
        constraintLayout.addView(definicion);
        definicion.setText(Html.fromHtml(intent.getStringExtra("definicion")).toString());
        definicion.setY(350);
        definicion.setX(10);

        if(!intent.getBooleanExtra("victoria",true)) {
            //Añadir restricciones
            TextView restricciones = new TextView(this);
            constraintLayout.addView(restricciones);
            restricciones.setText(Html.fromHtml(intent.getStringExtra("restricciones")).toString());
            restricciones.setWidth(widthDisplay - 10);
            restricciones.setY(definicion.getY() + (definicion.getText().length() / 100) * 20 + 100);
            restricciones.setX(10);

            //Añadir palabras posibles
            TextView palabras = new TextView(this);
            constraintLayout.addView(palabras);
            palabras.setText(Html.fromHtml(intent.getStringExtra("palabras")).toString());
            palabras.setWidth(widthDisplay - 10);
            palabras.setY(restricciones.getY() + (restricciones.getText().length() / 100) * 20 + 200);
            palabras.setX(10);
        }

    }
}