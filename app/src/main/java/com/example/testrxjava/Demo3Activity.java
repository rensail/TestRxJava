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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Demo3Activity extends AppCompatActivity implements View.OnClickListener{
    private  static final  String TAG = "Demo3Activity";

    private Button button_stop,button_start,button_start_no,button_retry,button_nest;
    private boolean isStop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo3);

        button_stop = findViewById(R.id.button_stop);
        button_start = findViewById(R.id.button_start);
        button_start_no = findViewById(R.id.button_start_no);
        button_retry = findViewById(R.id.button_retry);
        button_nest = findViewById(R.id.button_nest);
        button_stop.setOnClickListener(this);
        button_start.setOnClickListener(this);
        button_start_no.setOnClickListener(this);
        button_retry.setOnClickListener(this);
        button_nest.setOnClickListener(this);

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

            case R.id.button_retry:
                testNetWorkRetry();
                break;

            case R.id.button_nest:
                testNetWorkNest();
                break;

            default:
                break;
        }
    }


    /**
     *************************************无条件网络轮询**********************************************************************
     */


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


    /**
     *************************************有条件网络轮询**********************************************************************
     */

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



    /**
     *************************************网络请求出错重连**********************************************************************
     */

    //最大的重连请求次数
    private  static final int MaxRetryCount =10;
    //当前重连请求次数
    private  int CurrentRetryCount =1;
    //当前重连等待时间
    private  int CurrentWaitTime =0;

    public void testNetWorkRetry(){

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://fanyi.youdao.com/")
                                                  .addConverterFactory(GsonConverterFactory.create())
                                                  .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                                  .build();

        PostRequestInterface request = retrofit.create(PostRequestInterface.class);

        Observable<Translation>  observable = request.getcall("网络重新请求");

        observable.retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @NonNull
            @Override
            public ObservableSource<?> apply(@NonNull Observable<Throwable> throwableObservable) throws Exception {

                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @NonNull
                    @Override
                    public ObservableSource<?> apply(@NonNull Throwable throwable) throws Exception {

                        // 输出异常信息
                        Log.d(TAG,  "发生异常 = "+ throwable.toString());

                        //判断当前异常是否属于IO异常
                        if(throwable instanceof IOException){

                            Log.d(TAG,  "属于IO异常，需重试！");

                            //判断当前重试次数是否大于设置次数
                            if(CurrentRetryCount<=MaxRetryCount){
                                Log.d(TAG,"第"+CurrentRetryCount+"次重连");
                                CurrentWaitTime =CurrentRetryCount;
                                CurrentRetryCount = CurrentRetryCount+1;
                                Log.d(TAG,"当前等待时间:"+CurrentWaitTime);
                                return Observable.just(1).delay(CurrentWaitTime,TimeUnit.SECONDS);
                            }
                            else{
                                //重置当前重试次数
                                CurrentRetryCount =1;
                                return Observable.error(new Throwable("当前重试次数超过设置次数["+MaxRetryCount +"]，即不再重试"));
                            }
                        }
                        else {
                                return Observable.error(new Throwable("发生了非网络异常(非I/O异常)"));
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<Translation>() {
              @Override
              public void onSubscribe(Disposable d) {
              }

              @Override
              public void onNext(Translation translation) {
                  Log.d(TAG,"收发数据成功！");
                  translation.show();
              }

              @Override
              public void onError(Throwable e) {
                  // 获取停止重试的信息
                  Log.d(TAG,  e.toString());
              }

              @Override
              public void onComplete() {

              }
          });
    }


    /**
     *************************************网络嵌套回调请求**********************************************************************
     */

    public  void testNetWorkNest(){

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://fanyi.youdao.com/")
                                                  .addConverterFactory(GsonConverterFactory.create())
                                                  .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                                  .build();

        PostRequestInterface  request = retrofit.create(PostRequestInterface.class);

        Observable<Translation> observable_register = request.getcall_register("register");
        Observable<Translation> observable_login = request.getcall_login("login");
        Observable<Translation> observable_middle = request.getcall_middle("middle");

        observable_register.subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .doOnNext(new Consumer<Translation>() {
                               @Override
                               public void accept(@NonNull Translation translation) throws Exception {
                                   Log.d(TAG, "第1次网络请求成功");

                                   // 对第1次网络请求返回的结果进行操作 = 显示翻译结果
                                   translation.show();
                               }
                           })
                           .observeOn(Schedulers.io())
                           .flatMap(new Function<Translation,ObservableSource<Translation>>() {
                               @NonNull
                               @Override
                               public ObservableSource<Translation> apply(@NonNull Translation translation) throws Exception {
                                   return observable_middle;
                               }
                           }).delay(5,TimeUnit.SECONDS)
                             .observeOn(AndroidSchedulers.mainThread())
                             .doOnNext(new Consumer<Translation>() {
                                 @Override
                                 public void accept(@NonNull Translation translation) throws Exception {
                                     Log.d(TAG, "第2次网络请求成功");
                                     // 对第2次网络请求返回的结果进行操作 = 显示翻译结果
                                     translation.show();
                                 }
                             }).observeOn(Schedulers.io())
                               .flatMap(new Function<Translation, ObservableSource<Translation>>() {
                                   @NonNull
                                   @Override
                                   public ObservableSource<Translation> apply(@NonNull Translation translation) throws Exception {
                                       return observable_login;
                                   }
                               }).delay(5,TimeUnit.SECONDS)
                                 .observeOn(AndroidSchedulers.mainThread())
                                 .subscribe(new Consumer<Translation>(){
                                   @Override
                                    public void accept(@NonNull Translation translation) throws Exception {
                                      Log.d(TAG, "第3次网络请求成功");
                                      translation.show();
                                 }
                               },new Consumer<Throwable>(){
                                 @Override
                                 public void accept(@NonNull Throwable throwable) throws Exception {
                                      System.out.println("第3次网络请求失败");
                                 }
                             });


    }

}