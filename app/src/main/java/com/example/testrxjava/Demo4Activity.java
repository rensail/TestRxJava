package com.example.testrxjava;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class Demo4Activity extends AppCompatActivity implements View.OnClickListener{

    private  static final  String TAG = "Demo4Activity";
    private  Button button_merge_local,button_merge_net_local;

    private  String local_data1 = "本地数据1";
    private  String local_data2 = "本地数据2";
    private  String local_combine_data = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo4);

        button_merge_local = findViewById(R.id.button_merge_local);
        button_merge_net_local = findViewById(R.id.button_merge_net_local);
        button_merge_local.setOnClickListener(this);
        button_merge_net_local.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.button_merge_local:
                mergeLocalData();
                break;

            case R.id.button_merge_net_local:
                mergeNetWorkAndLocal();
                break;

            default:
                break;
        }
    }

    /**
     *************************************合并本地数据**********************************************************************
     */

    public void  mergeLocalData(){



        Observable<String> observable1 = Observable.just(local_data1);
        Observable<String> observable2 = Observable.just(local_data2);

        Observable.merge(observable1,observable2)
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Observer<String>() {
                      @Override
                      public void onSubscribe(Disposable d) {

                      }

                      @Override
                      public void onNext(String s) {
                          Log.d(TAG,"数据："+s);
                          local_combine_data = local_combine_data+s;
                      }

                      @Override
                      public void onError(Throwable e) {
                      }

                      @Override
                      public void onComplete() {
                          Log.d(TAG,"合并后的数据是："+local_combine_data);
                      }
                  });
    }


    /**
     *************************************合并网络和本地数据*********************************************************************
     */

    public void  mergeNetWorkAndLocal(){
        Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl("http://fanyi.youdao.com/")
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                        .build();

        PostRequestInterface request = retrofit.create(PostRequestInterface.class);

        Observable<Translation> observable_net = request.getcall("来自网络的数据");
        Observable observable_local1 = Observable.just(local_data1+local_data2);

        Observable.zip(observable_net, observable_local1, new BiFunction<Translation, String, String>() {
            @NonNull
            @Override
            public String apply(@NonNull Translation translation, @NonNull String s) throws Exception {
                return "网络数据["+translation.getTranslateResult().get(0).get(0).getTgt()+"] 本地数据["+s+"]";
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<String>() {
              @Override
              public void onSubscribe(Disposable d) {

              }

              @Override
              public void onNext(String s) {
                  Log.d(TAG,"合并后的数据："+s);
              }

              @Override
              public void onError(Throwable e) {
                  Log.d(TAG,"网络请求失败！");
                  Log.d(TAG,e.getMessage());
              }

              @Override
              public void onComplete() {

              }
          });
    }

}