package com.example.testrxjava;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.SafeObserver;
import io.reactivex.schedulers.Schedulers;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private  static final String TAG = "MainActivity";
    Integer i =10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //testconcat();
        //testmerge();
        //testmergeDelayError();
        //testzip();
        testcombineLastest();
    }

    //TODO ------------------------基础的操作符-------------------------------------------------------


    public void testrxjava(){
        /**
         * 链式写法
         */
        //被观察者
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).subscribe(new Observer<Integer>() {//订阅观察者(观察者通过匿名类实现)
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "接收到了事件"+ integer  );
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        });

        /**
         * 非链式写法
         */
        //被观察者
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        });

        //观察者
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "接收到了事件"+ integer  );
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        };
        //订阅
        observable.subscribe(observer);
     }


    //TODO ------------------------变化操作符-------------------------------------------------------

    /**
     * map变化操作符
     */
    public  void testmap(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).map(new Function<Integer, String>() {
            @NonNull
            @Override
            public String apply(@NonNull Integer integer) throws Exception {
                return "使用 Map变换操作符 将事件" + integer +"的参数从 整型"+integer + " 变换成 字符串类型" + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.d(TAG,s);
            }
        });
    }

    /**
     * flatmap变化操作符，用于拆分事件
     */
    public void testflatmap(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @NonNull
            @Override
            public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                final List<String> list = new ArrayList<String>();
                for(int i =0;i<4;i++){
                    list.add("我是事件 ["+integer+"] 拆分后的子事件"+i);
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.d(TAG,s);
            }
        });
    }

    /**
     * concatmap变化操作符，用于拆分事件
     * 与flatmap类似，只是flatmap拆分后是无序，concat是有序的
     */
    public  void testconcatmap(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @NonNull
            @Override
            public ObservableSource<String> apply(@NonNull Integer integer) throws Exception {
                final  List<String> list = new ArrayList<>();
                for(int i=0; i<5; i++){
                    list.add("我是事件 ["+integer+"] 拆分后的子事件"+i);
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.d(TAG,s);
            }
        });
    }

    /**
     * buffer变化操作符,用于缓存事件
     */
    public  void testbuffer(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onNext(5);
            }
        }).buffer(3,1)
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        Log.d(TAG," 缓存区里的事件数量 = " +  integers.size());
                        for(Integer value:integers){
                            Log.d(TAG,"事件 ["+value+" ]");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应" );
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }
                });
    }


    //TODO ------------------------组合操作符-------------------------------------------------------

    /**
     * concat合并被观察者数量<=4,concatarray合并被观察者数量>4
     */
    public  void testconcat(){
         Observable.concat(Observable.just(1,2,3),
                           Observable.just(4,5,6),
                           Observable.just(7,8,9),
                           Observable.just(10,11,12))
                   .subscribe(new Observer<Integer>() {
                       @Override
                       public void onSubscribe(Disposable d) {
                           Log.d(TAG, "开始采用subscribe连接");
                       }

                       @Override
                       public void onNext(Integer integer) {
                           Log.d(TAG, "接收到了事件"+ integer  );
                       }

                       @Override
                       public void onError(Throwable e) {
                           Log.d(TAG, "对Error事件作出响应");
                       }

                       @Override
                       public void onComplete() {
                           Log.d(TAG, "对Complete事件作出响应");
                       }
                   });
    }


    /**
     * merge合并被观察者数量<=4,mergearray合并被观察者数量>4
     * merge和concat的不同在于，merge合并后按时间并行执行
     */
    public void testmerge(){
        Observable.merge(Observable.intervalRange(0,5,1,1,TimeUnit.SECONDS),
                         Observable.intervalRange(5,5,1,1,TimeUnit.SECONDS))
                  .subscribe(new Observer<Long>() {
                      @Override
                      public void onSubscribe(Disposable d) {
                          Log.d(TAG, "开始采用subscribe连接");
                      }

                      @Override
                      public void onNext(Long value) {
                          Log.d(TAG, "接收到了事件"+ value  );
                      }

                      @Override
                      public void onError(Throwable e) {
                          Log.d(TAG, "对Error事件作出响应");
                      }

                      @Override
                      public void onComplete() {
                          Log.d(TAG, "对Complete事件作出响应");
                      }
                  });
    }

    /**
     * concatDelayError和mergeDelayError，当希望onError在其他观察者Observable发送结束后触发。
     */
    public void testmergeDelayError(){
        Log.d(TAG,"------------------------使用merge时------------------------------");
        Observable.merge(Observable.create(new ObservableOnSubscribe<Long>() {
                    @Override
                    public void subscribe(ObservableEmitter<Long> e) throws Exception {
                        e.onNext(1l);
                        e.onNext(2l);
                        e.onNext(3l);
                        e.onNext(4l);
                        e.onNext(5l);
                        e.onError(new NullPointerException());
                    }
                }),
                Observable.intervalRange(6,5,1,1,TimeUnit.SECONDS))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }

                    @Override
                    public void onNext(Long value) {
                        Log.d(TAG, "接收到了事件"+ value  );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }
                });


        Log.d(TAG,"------------------------使用mergeDelayError时------------------------------");
        Observable.mergeDelayError(Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                e.onNext(1L);
                e.onNext(2L);
                e.onNext(3L);
                e.onNext(4L);
                e.onNext(5L);
                e.onError(new NullPointerException());
            }
        }),
            Observable.intervalRange(6,5,1,1,TimeUnit.SECONDS)
        ).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(Long value) {
                Log.d(TAG, "接收到了事件"+ value  );
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        });
    }


    //TODO ------------------------合并操作符-------------------------------------------------------

    /**
     * zip,合并多个被观察者（Observable）发送的事件，生成一个新的事件序列（即组合过后的事件序列），并最终发送.
     */
    public void testzip(){
        Observable observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "被观察者1发送了事件1");
                e.onNext(1);
                Thread.sleep(1000);
                Log.d(TAG, "被观察者1发送了事件2");
                e.onNext(2);
                Thread.sleep(1000);
                Log.d(TAG, "被观察者1发送了事件3");
                e.onNext(3);
                Thread.sleep(1000);
                //如果被观察者1调用了onComplete方法，会报错
            }
        }).subscribeOn(Schedulers.io());

        Observable observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.d(TAG, "被观察者2发送了事件A");
                e.onNext("A");
                Thread.sleep(1000);
                Log.d(TAG, "被观察者2发送了事件B");
                e.onNext("B");
                Thread.sleep(1000);
                Log.d(TAG, "被观察者2发送了事件C");
                e.onNext("C");
                Thread.sleep(1000);
                Log.d(TAG, "被观察者2发送了事件D");
                e.onNext("D");
                Thread.sleep(1000);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @NonNull
            @Override
            public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                return integer+s;
            }
        }).subscribe(new Observer<String>(){
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, "最终接收到的事件 =  " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        });
    }


    public void testcombineLastest(){
        Observable.combineLatest(Observable.intervalRange(0,3,1,1,TimeUnit.SECONDS),
                                 Observable.intervalRange(3,3,1,1,TimeUnit.SECONDS),
                                 new BiFunction<Long,Long,Long>(){
                                     @NonNull
                                     @Override
                                     public Long apply(@NonNull Long aLong, @NonNull Long aLong2) throws Exception {
                                         Log.d(TAG, "合并的数据是： "+ aLong + " "+ aLong2);
                                         return aLong+aLong2;
                                     }
                                 }).subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                Log.d(TAG, "合并的结果是： "+aLong);
            }
        });
    }
}