package com.qmai.android.new_instance;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.qmai.android.newinstance_api.ImplLoader;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImplLoader.init();
        Shape shape = ImplLoader.getIntance("/shape");
        TextView tv = findViewById(R.id.show);
        tv.setText((shape == null) + "");

    }
}
