package com.example.venkateshwar.tod;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class ListActivity extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        StringBuffer s1 = new StringBuffer();
        textView=(TextView)findViewById(R.id.learningList);
//        getActionBar().setHomeButtonEnabled(true);
        if (textView != null) {
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setLongClickable(true);
            textView.setTextIsSelectable(true);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
        }
        int i = 0;
        for (Learning str : MainActivity.arrayList) {
            s1.append(i + 1 + ". " + str.toString() + "\n");
            i++;
        }
        textView.setText(s1);
    }
}
