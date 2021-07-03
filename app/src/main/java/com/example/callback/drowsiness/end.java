package com.example.callback.drowsiness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class end extends AppCompatActivity {
    Button proceed;
    TextView a,b;
    private String key =" ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        proceed = (Button)findViewById(R.id.button2);
        a = (TextView)findViewById(R.id.textView11);
        b = (TextView)findViewById(R.id.textView9);
        proceed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent next = new Intent(end.this,MainActivity.class);
                next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(next);
                end.this.finish();
                return false;
            }
        });
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        a.setText(currentDateTimeString);
        Intent next = getIntent();
        String status = next.getStringExtra(key);
        b.setText(status);
    }
}
