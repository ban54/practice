package com.ban54.simplebutterknife;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import viewbind.ViewBind;
import viewbind.annotation.Bind;
import viewbind.annotation.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.button1)
    Button mBtn1;
    @Bind(R.id.button2)
    Button mBtn2;
    @Bind(R.id.button3)
    Button mBtn3;
    @Bind(R.id.text1)
    TextView mTextView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewBind.bind(this);
        mBtn1.setText("I am button1");
        mBtn2.setText("I am button2");
        mBtn3.setText("I am button3");
        mTextView1.setBackgroundColor(Color.BLUE);
        mTextView1.setTextColor(Color.WHITE);
    }

    @OnClick({R.id.button1, R.id.button2, R.id.button3})
    @Override
    public void onClick(View view) {
        int color = Color.GREEN;
        switch (view.getId()) {
            case R.id.button1:
                color = Color.RED;
                break;
            case R.id.button2:
                color = Color.BLUE;
                break;
            case R.id.button3:
                color = Color.YELLOW;
                break;
        }
        mTextView1.setBackgroundColor(color);
    }
}
