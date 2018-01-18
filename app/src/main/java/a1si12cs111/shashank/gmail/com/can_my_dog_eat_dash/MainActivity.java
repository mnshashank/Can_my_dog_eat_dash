package a1si12cs111.shashank.gmail.com.can_my_dog_eat_dash;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech textToSpeech;
    private String verdict;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, this);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);


        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });



    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Ask Something..");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String sentence = result.get(0);
                    String[] wordsInTheSentence = sentence.split(" ");
                    int[] wordsIndex = new int[wordsInTheSentence.length];
                    ArrayList<String> stopWords = new ArrayList<String>(Arrays.asList(
                            "can", "my", "dog", "eat", "eating", "have", "feed", "feeding", "I", "to", "Bob", "give", "drink"));

                    for (int i= 0; i < wordsInTheSentence.length; i++) {
                        if (stopWords.contains(wordsInTheSentence[i])) {
                            wordsIndex[i] = 1;
                        }
                    }

                    StringBuilder stringBuilder = new StringBuilder();
                    for(int j=0; j < wordsIndex.length; j++) {
                        if (wordsIndex[j] == 0) {
                            stringBuilder.append(wordsInTheSentence[j]);
                        }
                    }
                    try {
                        checkWhetherDogCanEatOrNot(stringBuilder.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    speakToMe();
                }
                break;
            }

        }
    }

    private void checkWhetherDogCanEatOrNot(String foodName) throws IOException, JSONException {

        String jsonString = new DataUtil().getJsonFromFile(getApplicationContext());

        JSONArray jsonArray = new JSONArray(jsonString);
        Boolean found = false;
        for (int eachObject = 0; eachObject < jsonArray.length(); eachObject ++) {

            JSONObject jsonObject = jsonArray.getJSONObject(eachObject);
            String jsonName = jsonObject.getString("name").replaceAll("\\s+","");
            String jsonAlternateName = jsonObject.getString("alternateName").replaceAll("\\s+","");
            String jsonEatable = jsonObject.getString("eatable");

            if (jsonName.equals(foodName) || jsonAlternateName.equals(foodName)) {
                if (jsonEatable.equals("yes")) {
                    verdict = "sure he can";
                    txtSpeechInput.setText("");
                    txtSpeechInput.setText(jsonObject.getString("description"));
                } else if (jsonEatable.equals("no")) {
                    verdict = "no he cannot";
                    txtSpeechInput.setText("");
                    txtSpeechInput.setText("No he cannot");
                }
                found = true;
            }
        }
        if (found == false) {
            //TODO : when there is np result, think of something to be done, google search view and feedback.
            txtSpeechInput.setText("");
            txtSpeechInput.setText("Could not get the result for now");
            verdict = "sorry i am not sure";
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //Log.e("TTS", "This Language is not supported");
                txtSpeechInput.setText("This Language is not supported");
            } else {
                speakToMe();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakToMe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakToMe21();
        } else {
            speakToMe20();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakToMe21() {
        String utteranceId=this.hashCode() + "";
        textToSpeech.speak(verdict, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
    @SuppressWarnings("deprecation")
    private void speakToMe20() {
        textToSpeech.speak(verdict, TextToSpeech.QUEUE_FLUSH, null);
    }
}
