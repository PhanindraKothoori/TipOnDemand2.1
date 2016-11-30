package com.example.venkateshwar.tod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView= (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<Learning>(getApplicationContext(),R.layout.simple_learning,
                MainActivity.arrayList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle("Edit this");
                final EditText editText = new EditText(getApplicationContext());
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                editText.setTextColor(Color.argb(255, 0, 0, 0));
                editText.setSingleLine(false);
                editText.setHorizontalScrollBarEnabled(false);
                editText.setText(MainActivity.arrayList.get(position).getData());
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        String s = editText.getText().toString().trim();
                        if (s == null || s.isEmpty()) {
                            Snackbar.make(findViewById(android.R.id.content), "You cannot make it empty.", Snackbar.LENGTH_LONG).show();
                        } else {
                            MainActivity.arrayList.get(position).setData(s);
                            MainActivity.mLearnings.child(Integer.toString(position)).setValue(MainActivity.arrayList.get(position));
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // 3. Get the AlertDialog from create()
                builder.show();

            }
        });

    }
}
