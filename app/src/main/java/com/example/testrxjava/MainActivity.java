package com.example.testrxjava;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button_demo1,button_demo2,button_demo3,button_demo4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_demo1 =findViewById(R.id.button_demo1);
        button_demo2 =findViewById(R.id.button_demo2);
        button_demo3 =findViewById(R.id.button_demo3);
        button_demo4 =findViewById(R.id.button_demo4);
        button_demo1.setOnClickListener(this);
        button_demo2.setOnClickListener(this);
        button_demo3.setOnClickListener(this);
        button_demo4.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_demo1:
                Intent intent =new Intent(MainActivity.this, Demo1Activity.class);
                startActivity(intent);
                break;

            case R.id.button_demo2:
                Intent intent2 =new Intent(MainActivity.this, Demo2Activity.class);
                startActivity(intent2);
                break;

            case R.id.button_demo3:
                Intent intent3 =new Intent(MainActivity.this, Demo3Activity.class);
                startActivity(intent3);
                break;

            case R.id.button_demo4:
                Intent intent4 =new Intent(MainActivity.this, Demo4Activity.class);
                startActivity(intent4);
                break;

            default:
                break;
        }
    }


}