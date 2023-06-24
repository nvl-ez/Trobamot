package com.example.trobamot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    //Constantes del juego
    private static final int topSpace = 100; //espacio entre el grid y la parte superiror de la pantalla
    private static final int betweenSpace = 20; //espacio entre letras del grid
    private static final int cantidadLetras = 27;
    private static final int textViewSize = 150;
    private static final String YELLOW = "#fbffb8";
    private static final String WHITE = "#ffffff";
    private static final String RED = "#ff4753";
    private static final String GREEN = "#47ff72";
    private static final String ORANGE = "#ffc43b";

    // Variables del juego
    private int lengthWord = 6;
    private int maxTry = 3;
    //Caracteres con sus posiciones
    private UnsortedArrayMapping<Character,UnsortedLinkedListSet<Integer>> mappingKeyboard = new UnsortedArrayMapping<>(cantidadLetras);
    //posicion del ultimo caracter escrito en el grid de las palabras
    private int x = -1;
    private int y = 0;
    //Palabra sin acentos, palabra con acentos
    private HashMap<String, String> wordsHash = new HashMap<>();
    //Palabra sin acentos, palabra con acentos (Se actualiza con las restricciones)
    private TreeMap<String, String> wordsTree = new TreeMap<>();
    //Caracteres con sus restricciones (-2 no existe, -1 existe y no se sabe posicion, n>=0 pos del caracter en la letra)
    private TreeMap<Character, UnsortedLinkedListSet<Integer>> restrictionTree = new TreeMap<>();
    //Palabra sin acentos
    private String wordKey;
    //Palabra con acentos
    private String wordValue;
    //Palabra introducida
    private String wordLine;
    //cantidad de palabras en el arbol de restricciones
    private int nWords;


    // Variables de construcción de la interfaz
    public static String grayColor = "#D9E1E8";
    private int widthDisplay;
    private int heightDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Object to store display information
        DisplayMetrics metrics = new DisplayMetrics();
        // Get display information
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        widthDisplay = metrics.widthPixels;
        heightDisplay = metrics.heightPixels;
        wordLine = "";

        createInterface();
        readWords();
        updateMapping();
        showNWords();
    }

    private void createInterface() {
        createGrid();
        createKeyboard();
        hideSystemUI();
    }

    private void createGrid() {
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        // Definir les característiques del "pinzell"
        GradientDrawable gd = getUnselected();
        int sideSpace = (widthDisplay-lengthWord*(textViewSize+betweenSpace))/2;

        // Crear los textView del grid
        // Complegidad O(n^2) pero sabiendo que las palabras son de 7 letras como maximo, y
        // realisticamente el numero de intentos no será muy grande

        for(int k=0,i = 0; i<maxTry; i++) {
            for(int j = 0; j<lengthWord; j++,k++) {
                TextView textView = new TextView(this);
                textView.setText("");
                textView.setBackground(gd);
                textView.setId(k); //la columna es: k%lengthWord y la fila es k/maxTry
                textView.setWidth(textViewSize);
                textView.setHeight(textViewSize);
                // Posicionam el TextView
                textView.setX(sideSpace+j*betweenSpace+j*textViewSize);
                textView.setY(topSpace+i*betweenSpace+i*textViewSize);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(30);
                // Afegir el TextView al layout
                constraintLayout.addView(textView);
            }
        }

        //Pintamos la primera caja
        TextView textView = findViewById(getGridId(0, 0));
        textView.setBackground(getSelected());

    }

    //Inicializa el mapping del teclado
    private void initializeKeyboardMapping(){
        //Lenamos las keys de mapping e inicializamos los valores
        for(int i = 'a'; i<='z'; i++){
            mappingKeyboard.put(new Character((char)i), new UnsortedLinkedListSet<Integer>());
        }
        //Caso especial para la ç
        mappingKeyboard.put(new Character('ç'), new UnsortedLinkedListSet<Integer>());
    }

    private void createKeyboard() {
        initializeKeyboardMapping();
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        Iterator it = mappingKeyboard.iterator();
        ConstraintLayout.LayoutParams paramsKeyboard = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        int buttonHeight = 130;
        int buttonWidth = 150;
        int buttonsPerRow = widthDisplay/buttonWidth;
        int spaceBetweenButtons = (widthDisplay-buttonsPerRow*buttonWidth)/(buttonsPerRow+1);
        int rows = cantidadLetras/buttonsPerRow;
        if(cantidadLetras%buttonsPerRow != 0) rows++;
        int topPadding = heightDisplay-buttonHeight*rows -spaceBetweenButtons*rows;
        paramsKeyboard.height = buttonHeight;
        paramsKeyboard.width = buttonWidth;
        Button button;

        //Crea todos los botones del teclado
        for(int i = 0; i<rows; i++){
            for(int j = 0; j<buttonsPerRow && it.hasNext(); j++) {
                button = new Button(this);
                //Obtencion del caracter del boton
                UnsortedArrayMapping.MappingPair pair = (UnsortedArrayMapping.MappingPair) it.next();
                Character c = (Character) pair.getKey();
                button.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                button.setId(9999+c);
                button.setTextSize(25);
                button.setText(c.charValue() + "");
                button.setLayoutParams(paramsKeyboard);
                button.setY( topPadding+i*spaceBetweenButtons+i*buttonHeight);
                button.setX(j*spaceBetweenButtons+j*buttonWidth);
                constraintLayout.addView(button);

                // Afegir la funcionalitat als botons
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        onClickDo(v);
                    }
                });
            }
        }

        //añadimos los botones de borrar y enviar
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        buttonHeight = 130;
        buttonWidth = 300;
        params.height = buttonHeight;
        params.width = buttonWidth;

        Button borrar = new Button(this);
        Button enviar = new Button(this);
        borrar.setTextSize(25);
        borrar.setText("Borrar");
        borrar.setLayoutParams(params);
        borrar.setY( topPadding-spaceBetweenButtons-buttonHeight);
        borrar.setX(widthDisplay/2-buttonWidth-spaceBetweenButtons);
        constraintLayout.addView(borrar);
        enviar.setTextSize(25);
        enviar.setText("Enviar");
        enviar.setLayoutParams(params);
        enviar.setY( topPadding-spaceBetweenButtons-buttonHeight);
        enviar.setX(widthDisplay/2+spaceBetweenButtons);
        constraintLayout.addView(enviar);
        // Afegir la funcionalitat als botons
        enviar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickDo(v);
            }
        });
        borrar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickDo(v);
            }
        });
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE  // no posar amb notch
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void onClickDo(View v) {
        Button button = (Button)v;
        TextView textView;
        boolean correcta = true;

        if(button.getText().toString().compareTo("Borrar") == 0){
            if(x>=0){
                //Borramos el color de la siguiente caja
                if(y*lengthWord+x < lengthWord*maxTry-1) { //tratar el caso de qeu sea la ultima letra
                    textView = findViewById(getGridId(x+1, y));
                    textView.setBackground(getUnselected());
                }

                //Borramos eel contenido de la caja actual
                textView = findViewById(getGridId(x, y));
                textView.setText("");
                x--;
                //Seleccionamos la caja anterior
                textView = findViewById(getGridId(x+1, y));
                textView.setBackground(getSelected());
            }
            if(wordLine.length() >0) {
                wordLine = wordLine.substring(0, wordLine.length()-1);
            }

        } else if(button.getText().toString().compareTo("Enviar") == 0){
            if(y<maxTry-1 && x==lengthWord-1){ //si se ha llenado la fila y no es la ultima
                if(wordsHash.containsKey(wordLine)) {//Verificamos que la palabra exista
                    //Borra el color de la caja de la fila anterior
                    textView = findViewById(getGridId(x, y));
                    textView.setBackground(getUnselected());
                    y++;
                    x = -1;
                    //Marca la primera caja de la siguiente fila
                    textView = findViewById(getGridId(x + 1, y));
                    textView.setBackground(getSelected());

                    correcta = verificateChar();
                    updateRestrictionTree();
                    showNWords();
                    updateKeyborad();
                    if(correcta)changeWindow(correcta, wordKey, wordValue);

                    wordLine = "";
                }
                else{
                    //Mostramos el mensaje de error
                    correcta = false;
                    Context context = getApplicationContext() ;
                    CharSequence text = "Paraula no vàlida!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText (context, text, duration) ;
                    toast.show ();
                }
            }
            else if(y==maxTry-1 && x==lengthWord-1){//si estamos en la ultima fila y se ha llenado la fila
                if(wordsHash.containsKey(wordLine)) {//Verificamos que la palabra exista
                    y++;
                    correcta = verificateChar();
                    updateRestrictionTree();
                    showNWords();
                    updateKeyborad();
                    changeWindow(correcta, wordKey, wordValue);
                } else{
                    //Mostramos el mensaje de error
                    correcta = false;
                    Context context = getApplicationContext() ;
                    CharSequence text = "Paraula no vàlida!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText (context, text, duration) ;
                    toast.show ();
                }
            }
            else if(x<lengthWord-1){//Si no se ha llenado la fila
                correcta = false;
                //Mostramos el mensaje de error
                Context context = getApplicationContext() ;
                CharSequence text = "Paraula incompleta!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText (context, text, duration) ;
                toast.show () ;
            }

        } else{ //Si es una letra
            if(x<lengthWord-1){
                //despintamos la caja anterior si se puede
                if(x>=-1 &&x<lengthWord-2){
                    textView = findViewById(getGridId(x+1, y));
                    textView.setBackground(getUnselected());
                }

                //tratamos el caso de que se haya llegado al final
                x++;

                //pintamos la siguiente caja si se puede
                if(x<lengthWord-1) {
                    textView = findViewById(getGridId(x + 1, y));
                    textView.setBackground(getSelected());
                }
            }


            textView = findViewById(getGridId(x, y));
            textView.setText(button.getText().toString().toUpperCase());
            if(x<lengthWord-1) {
                wordLine += button.getText();
            } else{
                wordLine = wordLine.substring(0,lengthWord-1)+button.getText();
            }
        }

    }

    private int getGridId(int x, int y){
        return (lengthWord*y+x); //Devuelve el id de la caja con las coordenadas especificadas
    }

    private GradientDrawable getSelected(){
        // Definir les característiques del "pinzell"
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(5);
        gd.setStroke(3, Color.parseColor(grayColor));
        gd.setColor(Color.parseColor(YELLOW));
        return gd;
    }

    private GradientDrawable getUnselected(){
// Definir les característiques del "pinzell"
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(5);
        gd.setStroke(3, Color.parseColor(grayColor));
        gd.setColor(Color.parseColor(WHITE));
        return gd;
    }

    private GradientDrawable getGreen(){
        // Definir les característiques del "pinzell"
        GradientDrawable gd = new GradientDrawable();
        //gd.setCornerRadius(5);
        gd.setStroke(3, Color.parseColor(grayColor));
        gd.setColor(Color.parseColor(GREEN));
        return gd;
    }

    private GradientDrawable getOrange(){
        // Definir les característiques del "pinzell"
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(5);
        gd.setStroke(3, Color.parseColor(grayColor));
        gd.setColor(Color.parseColor(ORANGE));
        return gd;
    }

    private GradientDrawable getRed(){
        // Definir les característiques del "pinzell"
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(5);
        gd.setStroke(3, Color.parseColor(grayColor));
        gd.setColor(Color.parseColor(RED));
        return gd;
    }

    public void updateRestrictionTree(){
        TreeMap tmp = new TreeMap();
        nWords = 0;
        Set wordsSet =wordsTree.entrySet();
        Iterator iteratorWords = wordsSet.iterator();


        //iteramos sobre todas las palabras
        while(iteratorWords.hasNext()){
            Set restrictionSet = restrictionTree.entrySet();
            Iterator iteratorRestriction = restrictionSet.iterator();
            Map.Entry wordEntry = (Map.Entry)iteratorWords.next();
            String s = (String)wordEntry.getKey();

            boolean add = true;
            //Evaluamos para cada restriccion
            while(iteratorRestriction.hasNext()){
                Map.Entry restrictionEntry = (Map.Entry)iteratorRestriction.next();
                UnsortedLinkedListSet restrictionList =(UnsortedLinkedListSet)restrictionEntry.getValue();

                if(s.contains(restrictionEntry.getKey()+"")&&!restrictionList.isEmpty()&&restrictionList.contains(-2)){
                    add = false; //no añadiremos la palabra si tiene una letra roja
                    break;
                } else if(!s.contains(restrictionEntry.getKey()+"")&&!restrictionList.isEmpty()&&restrictionList.contains(-1)){
                    add = false; //no añadiremos la palabra si no tiene una letra naranja
                    break;
                } else {
                    //no añadiremos la palabra si no tiene una letra verde en la posicion correcta
                    for(int i = 0; i <lengthWord; i++){
                        if(restrictionList.contains(i)){
                            if(!s.contains((Character)restrictionEntry.getKey()+"")){
                                add = false;
                                break;
                            }
                            if(s.charAt(i)!=(Character)restrictionEntry.getKey()){
                                add = false;
                                break;
                            }
                        }
                    }
                }
            }

            if(add){
                tmp.put(wordEntry.getKey(), wordEntry.getValue());
                nWords++;
            }
        }
        wordsTree = tmp;
    }

    private void readWords(){
        InputStream is = getResources().openRawResource(R.raw.paraules);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line = "";
        nWords = 0;

        while(line!=null){//mientras no hayamos llegado al final
            try {
                line = r.readLine();

                if(line != null && line.length()==lengthWord*2+1){//Buscamos las palabras de la longitud deseada
                    String[] valueAndKey = line.split(";");
                    wordsHash.put(valueAndKey[1], valueAndKey[0]);
                    wordsTree.put(valueAndKey[1], valueAndKey[0]);
                    nWords++;
                }
            } catch (IOException e) {
                System.out.println("Error al leer la palabra");
            }
        }
    }

    //Actualizo el mapping del teclado para qeu contenga las posiciones de los caracteres de una palabra aleatoria
    private void updateMapping(){

        Random rnd = new Random();
        int selectedWord = rnd.nextInt(nWords);
        Set<Map.Entry<String, String>> pairSet= wordsHash.entrySet();
        Iterator iterator = pairSet.iterator();

        //Obtencion de una palabra aleatoria (key y value)
        for(int i = 0; iterator.hasNext() && i<selectedWord-1; i++) iterator.next();
        Map.Entry<String, String> pair = (Map.Entry)iterator.next();
        wordKey = pair.getKey();
        wordValue = pair.getValue();
        System.out.println("Key: "+wordKey);
        System.out.println("Value: "+wordValue);

        //actualizacion del mapping
        for(int i = 0; i<wordKey.length(); i++){
            UnsortedLinkedListSet<Integer> posChar= mappingKeyboard.get(new Character(wordKey.charAt(i)));
            posChar.add(i);
        }
    }

    private boolean verificateChar(){
        boolean correcta = true;
        TextView textView;
        //pintamos la linea anterior con los colores de cada letra dependiendo de si existen, estan en la posicion
        //correcta o si existen pero no estan en la posicion
        //Actualizamos el arbol de restricciones y de palabras
        for(int i = 0; i<lengthWord; i++){
            if(maxTry == 1) {
                textView = findViewById(getGridId(i, y));
            } else{
                textView = findViewById(getGridId(i, y - 1));
            }
            Character character = textView.getText().toString().toLowerCase().charAt(0);
            UnsortedLinkedListSet<Integer> restrictionChars = new UnsortedLinkedListSet<>();

            UnsortedLinkedListSet posChars = mappingKeyboard.get(character);

            if(posChars.isEmpty()){ //si no existe el caracter
                textView.setBackground(getRed());
                restrictionChars.add(-2);
                System.out.println(character + " "+(-2));
                restrictionTree.put(character, restrictionChars);
                correcta = false; //sabemos que la palabra no es la correcta

            } else{ //si existe hay que buscar su pos y ver si coinciden
                if(posChars.contains(i)){
                    textView.setBackground(getGreen());
                    restrictionChars.add(i);
                    System.out.println(character + " "+(i));
                    restrictionTree.put(character, restrictionChars);
                } else{
                    textView.setBackground(getOrange());
                    correcta = false;
                    if(!restrictionTree.containsKey(character)){
                        restrictionChars.add(-1);
                        System.out.println(character + " "+(-1));
                        restrictionTree.put(character, restrictionChars);
                    }
                }
            }

        }
        return correcta;
    }

    @SuppressLint("ResourceType")
    public void showNWords(){
        TextView tv = findViewById(9999);
        if(tv !=null){
            ConstraintLayout constraintLayout = findViewById(R.id.layout);
            tv.setText("Cantidad de palabras posibles: "+nWords);

        }
        else{
            ConstraintLayout constraintLayout = findViewById(R.id.layout);
            TextView textView = new TextView(this);
            textView.setText("Cantidad de palabras posibles: "+nWords);
            // Posicionam el TextView
            textView.setX((widthDisplay-textView.getWidth())/8);
            textView.setY(topSpace+maxTry*betweenSpace+maxTry*textViewSize);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(20);
            textView.setId(9999);
            // Afegir el TextView al layout
            constraintLayout.addView(textView);
        }
    }

    public void updateKeyborad(){
        Set restrictionSet = restrictionTree.entrySet();
        Iterator iterator = restrictionSet.iterator();

        while(iterator.hasNext()){
            Map.Entry restrictionEntry = (Map.Entry)iterator.next();
            Character c = (Character) restrictionEntry.getKey();
            UnsortedLinkedListSet restrictionList = (UnsortedLinkedListSet) restrictionEntry.getValue();

            Button button = findViewById(9999+c);

            if(!restrictionList.isEmpty()&&restrictionList.contains(-2)){
                button.setTextColor(Color.parseColor(RED));
            } else if(!restrictionList.isEmpty()&&restrictionList.contains(-1)){
                button.setTextColor(Color.parseColor(ORANGE));
            } else{
                for(int i = 0; i<lengthWord; i++){
                    if(restrictionList.contains(i)){
                        button.setTextColor(Color.parseColor(GREEN));
                    }
                }
            }

        }
    }

    public void changeWindow(boolean correcta, String key, String value){
        Intent intent = new Intent(this, PantallaFinal.class);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Obtencion del texto del diccionario
                String json = getHTML(key);
                //verificamos el texto
                if(json.compareTo("[]")==0) {//no existe la palabra
                    json = "La paraula no te definició.";
                    intent.putExtra("definicion", json);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        intent.putExtra("definicion", jsonObject.getString("d"));
                        System.out.println(jsonObject.getString("d"));
                    } catch (JSONException e) {
                        System.out.println("ERROR: No se ha podido traducir el JSON");
                    }

                }
                intent.putExtra("palabras", palabrasPosiblesToString());
                intent.putExtra("victoria", correcta);
                intent.putExtra("palabra", value);
                intent.putExtra("restricciones", restrictionsToString());
                startActivity(intent);
            }
        });
        thread.start();
    }

    public String getHTML(String word){
        try {
            URL definition =  new URL("https://www.vilaweb.cat/paraulogic/?diec="+ word);
            BufferedReader in = new BufferedReader(new InputStreamReader(definition.openStream()));

            String line = in.readLine();
            StringBuffer sb = new StringBuffer();
            while(line != null){
                sb.append(line);
                line = in.readLine();
            }
            return sb.toString();
        } catch (MalformedURLException e) {
            System.out.println("ERROR: No existe la URL");
        } catch (IOException e){
            System.out.println("ERROR: No no se ha podido leer el contenido de la pagina");
        }
        return null;
    }

    public String restrictionsToString(){
        Set restricciones = restrictionTree.entrySet();
        Map.Entry restriccion;
        Iterator iterator = restricciones.iterator();
        String string = "RESTRICCIONS: ";
        while(iterator.hasNext()){
            restriccion = (Map.Entry)iterator.next();
            UnsortedLinkedListSet pos = (UnsortedLinkedListSet<Integer>) restriccion.getValue();
            Character letra = (Character)restriccion.getKey();

            //Verificamos si contiene o no la letra
            if(pos.contains(-2)){
                string += "no ha de contenir la "+letra.toString().toUpperCase()+", ";
            } else{
                //Buscamos en que posicion esta la letra
                boolean encontrado = false;
                for(int i = 0; i<lengthWord && !encontrado; i++){
                    if(pos.contains(i)){
                        encontrado = true;
                        string += "ha de contenir la "+letra.toString().toUpperCase()+" a la posició "+(i+1)+", ";
                    }
                }
                if(!encontrado){
                    string += "ha de contenir la "+letra.toString().toUpperCase()+", ";
                }
            }

        }
        return string;
    }

    public String palabrasPosiblesToString(){
        Set palabrasConjunto = wordsTree.entrySet();
        String palabras = "PARAULES POSSIBLES: ";
        Iterator iterator = palabrasConjunto.iterator();

        while(iterator.hasNext()){
            palabras += (String)((Map.Entry)iterator.next()).getValue()+", ";
        }
        return palabras;
    }
}