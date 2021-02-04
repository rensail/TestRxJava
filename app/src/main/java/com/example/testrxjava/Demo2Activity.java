package com.example.testrxjava;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Demo2Activity extends AppCompatActivity implements View.OnClickListener{
    private  static  final String TAG = "DemoActivity";

    private Button button_cache_data,button_change_thread;
    private TextView textview_demo2_result;
    StringBuilder demo2_sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo2);

        button_cache_data = findViewById(R.id.button_cache_data);
        button_change_thread = findViewById(R.id.button_change_thread);
        textview_demo2_result = findViewById(R.id.textview_demo2_result);
        button_cache_data.setOnClickListener(this);
        button_change_thread.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_cache_data:
                cleanResult();
                testCache();
                break;

            case R.id.button_change_thread:
                cleanResult();
                testThread();
                break;

            default:
                break;
        }
    }

    public void cleanResult(){
        if(textview_demo2_result.getText()!=""){
            textview_demo2_result.setText("");
        }
        demo2_sb.setLength(0);
    }


    /**
     ********************************模拟从缓存，磁盘缓存获取数据，如果都没有的话再进行网络请求获取数据****************************************
     */
    public  void testCache(){

        //模拟缓存中的数据
        String cache = null;
        //模拟磁盘缓存中的数据
        String diskcache = null;

        Observable<String> observable_cache = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                if (cache!=null) {
                    demo2_sb.append("缓存中的数据内容是："+cache+"\n");
                    Log.d(TAG,"缓存中的数据内容是："+cache);
                    e.onNext("从缓存中获取数据");
                }else{
                    e.onComplete();
                }
            }
        });


        Observable<String> observable_discache = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                if(diskcache!=null) {
                    demo2_sb.append("磁盘缓存中的数据内容是："+diskcache+"\n");
                    Log.d(TAG,"磁盘缓存中的数据内容是："+diskcache);
                    e.onNext("从磁盘缓存中获取数据");
                }else{
                    e.onComplete();
                }
            }
        });

        Observable<String>  observable_net = Observable.just("从网络中获取数据");

        Observable.concat(observable_cache,observable_discache,observable_net)
                  .firstElement()
                  .subscribe(new Consumer<String>() {
                      @Override
                      public void accept(@NonNull String s) throws Exception {
                          demo2_sb.append("最终的数据来源：："+s+"\n");
                          Log.d(TAG,"最终的数据来源："+s);
                          textview_demo2_result.setText(demo2_sb.toString());
                      }
                  });
    }


    /**
     ********************************线程切换****************************************
     */

    public void testThread(){

        //新建被观察者
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                 demo2_sb.append("被观察者所在的线程是："+Thread.currentThread().getName()+"\n");
                 Log.d(TAG,"被观察者所在的线程是："+Thread.currentThread().getName());
                 e.onNext(1);
                 e.onComplete();
            }
        });


        //新建一个观察者
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Integer integer) {
                demo2_sb.append("观察者所在的线程是："+Thread.currentThread().getName()+"\n");
                Log.d(TAG,"观察者所在的线程是："+Thread.currentThread().getName());
                demo2_sb.append("观察者响应事件 ["+integer+"]"+"\n");
                Log.d(TAG,"观察者响应事件 ["+integer+"] ");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                textview_demo2_result.setText(demo2_sb.toString());
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                  .observeOn(Schedulers.newThread())
                  .doOnNext(new Consumer<Integer>() {
                      @Override
                      public void accept(@NonNull Integer integer) throws Exception {
                          demo2_sb.append("观察者第一次所在的线程是："+Thread.currentThread().getName()+"\n");
                          Log.d(TAG,"观察者第一次所在的线程是："+Thread.currentThread().getName());
                      }
                  })
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(observer);
    }
}