package alpha.breathe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NoteActivity extends Activity {

    CustomEditText noteBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.DefaultTheme);
        setContentView(R.layout.note_activity);

        noteBox = findViewById(R.id.id_note_box);
        noteBox.setHorizontallyScrolling(false);
        noteBox.setSingleLine(true);
        noteBox.setLines(5);
        noteBox.setImeOptions(EditorInfo.IME_ACTION_SEND);



    }

    @Override
    protected void onResume() {
        super.onResume();
        noteBox.requestFocus();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(noteBox, InputMethodManager.SHOW_FORCED);

        DatabaseService dbService = new DatabaseService();


        noteBox.setOnEditorActionListener(new CustomEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                switch (actionId) {

                    case EditorInfo.IME_ACTION_SEND:

                        String message = noteBox.getText().toString();
                        noteBox.setText("");

                        if (message.trim().length() > 0){

                            Intent intent= getIntent().setAction("LOCATION");
                            Bundle b = intent.getExtras();

                            int noteCardTop =b.getInt("noteCardTop");
                            String userToken = b.getString("userToken");
                            String lat = b.getString("lat");
                            String lng = b.getString("lng");

                            Date timeStamp = Calendar.getInstance().getTime();

                            dbService.addToQueueDatabase(NoteActivity.this,userToken,lat,lng,timeStamp,message,null);


                            Intent myIntent = new Intent(NoteActivity.this, MainActivity.class);
                            imm.hideSoftInputFromWindow(noteBox.getWindowToken(), 0);

                            NoteActivity.this.startActivity(myIntent);

                        }

                        return true;

                    default:
                        return false;
                }
            }
        });

    }



}
