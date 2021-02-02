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

import android.os.Bundle;
import android.util.Log;

public class Demo2Activity extends AppCompatActivity {
    private  static  final String TAG = "DemoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo2);
        testCache();
        //testThread();
    }

    /**
     * 模拟从缓存，磁盘缓存获取数据，如果都没有的话再进行网络请求获取数据
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
                    Log.d(TAG,"数据的内容是："+cache);
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
                    Log.d(TAG,"数据的内容是："+diskcache);
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
                          Log.d(TAG,"最终的数据来源："+s);
                      }
                  });
    }


    public void testThread(){

        //新建被观察者
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                 Log.d(TAG,"被观察者所在的线程是："+Thread.currentThread().getName());
                 e.onNext(1);
                 e.onComplete();
            }
        });


        //新建一个观察者
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG,"开始通过Subscribe连接");
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG,"观察者所在的线程是："+Thread.currentThread().getName());
                Log.d(TAG,"观察者响应事件 ["+integer+"] ");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG,"对Complete事件作出响应");
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                  .observeOn(AndroidSchedulers.mainThread())
                  .doOnNext(new Consumer<Integer>() {
                      @Override
                      public void accept(@NonNull Integer integer) throws Exception {
                          Log.d(TAG,"观察者第一次所在的线程是："+Thread.currentThread().getName());
                      }
                  })
                  .observeOn(Schedulers.newThread())
                  .subscribe(observer);


    }
}