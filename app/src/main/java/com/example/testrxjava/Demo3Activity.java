package com.example.testrxjava;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

public class Demo3Activity extends AppCompatActivity implements View.OnClickListener{
    private  static final  String TAG = "Demo3Activity";

    private Button button_stop,button_start,button_start_no;
    private boolean isStop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo3);

        button_stop = findViewById(R.id.button_stop);
        button_start = findViewById(R.id.button_start);
        button_start_no = findViewById(R.id.button_start_no);
        button_stop.setOnClickListener(this);
        button_start.setOnClickListener(this);
        button_start_no.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_stop:
                isStop = true;
                break;

            case R.id.button_start:
                if(isStop == true){
                    isStop = false;
                    testNetworkPoll_2();
                }
                break;
            case R.id.button_start_no:
                testNetworkPoll_1();
                break;

            default:
                break;
        }
    }

    //无条件轮询
    public void testNetworkPoll_1(){

        Observable.interval(2,20, TimeUnit.SECONDS)
                  .doOnNext(new Consumer<Long>() {
                      @Override
                      public void accept(@NonNull Long aLong) throws Exception {

                          Log.d(TAG,"进行第" + aLong + "次轮询");

                          Retrofit retrofit = new Retrofit.Builder().baseUrl("http://fanyi.youdao.com/")
                                                                    .addConverterFactory(GsonConverterFactory.create())//设置使用Gson解析
                                                                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 支持RxJava
                                                                    .build();

                          PostRequestInterface request = retrofit.create(PostRequestInterface.class);

                          Observable observable = request.getcall("hello world!");

                          observable.subscribeOn(Schedulers.io())//切换至io线程进行订阅
                                    .observeOn(AndroidSchedulers.mainThread())//在主线程中进行观察
                                    .subscribe(new Observer<Translation>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            Log.d(TAG,"网络轮询的Subscribe");
                                        }

                                        @Override
                                        public void onNext(Translation translation) {
                                             translation.show();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.d(TAG,"网络轮询请求失败");
                                        }

                                        @Override
                                        public void onComplete() {
                                             Log.d(TAG,"网络轮询的Complete");
                                        }
                                    });
                      }
                  }).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG,"***事件的Subscribe***");
            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG,"***事件的Error***");
            }

            @Override
            public void onComplete() {
                Log.d(TAG,"***事件的Complete***");
            }
        });
    }



    //有条件轮询
    public void testNetworkPoll_2(){
          Retrofit retrofit = new Retrofit.Builder().baseUrl("http://fanyi.youdao.com/")
                                                    .addConverterFactory(GsonConverterFactory.create())
                                                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                                    .build();

          PostRequestInterface request = retrofit.create(PostRequestInterface.class);

          Observable<Translation> observable = request.getcall("你好，世界！");

          observable.repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
              @NonNull
              @Override
              public ObservableSource<?> apply(@NonNull Observable<Object> objectObservable) throws Exception {


                  return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                      @NonNull
                      @Override
                      public ObservableSource<?> apply(@NonNull Object o) throws Exception {
                          if(isStop==true) {
                              return Observable.error(new Throwable("轮询结束"));//会调用onError
                          }else{
                              return Observable.just(1).delay(2,TimeUnit.SECONDS);
                          }
                      }
                  });


              }
          }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Translation>() {
                @Override
                public void onSubscribe(Disposable d) {
                    Log.d(TAG,"网络轮询Subscribe");
                }

                @Override
                public void onNext(Translation translation) {
                    translation.show();
                }

                @Override
                public void onError(Throwable e) {
                    //获取轮询结束信息
                    Log.d(TAG,e.toString());
                }

                @Override
                public void onComplete() {
                    Log.d(TAG,"网络轮询Complete");
                }
            });
    }

}