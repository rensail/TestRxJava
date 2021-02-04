package com.example.testrxjava;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogShow {

    private  static  final String TAG = "LogShow";
    private Process exec;
    private int mPId;
    private String mPID;
    private boolean mRunning = true;
    String cmds = "";
    private BufferedReader mReader = null;
    StringBuilder Sb = new StringBuilder("this is log");

    //显示日志的方法
    public void show (Handler handler,int pid) {
        try {
            mPID = String.valueOf(pid);
            cmds = "logcat  *:e *:d | grep \"(" + mPID + ")\"";
            exec = Runtime.getRuntime().exec(cmds);
            mReader = new BufferedReader(new InputStreamReader(exec.getInputStream()), 1024);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String line="";
                    try {
                        while (mRunning && (line = mReader.readLine())!=null) {
                            Sb.append(line);
                            if (!mRunning) {
                                break;
                            }
                            if (line.length()==0) {
                                continue;
                            }
                            final Message msg = Message.obtain();
                            msg.what = 2;
                            msg.obj = line+ "\n";
                            handler.sendMessage(msg);
                            Thread.sleep(100);
                        }
                    }catch (Exception e){
                        Log.e(TAG, e.toString());
                    }
                    finally {
                        Log.e(TAG, "finally");
                        Log.e(TAG, "" + mRunning);
                        if (line == null) {
                            Log.e(TAG, "line is null");
                        }
                        if (exec != null) {
                            exec.destroy();
                        }
                        if (mReader != null) {
                            try {
                                mReader.close();
                                mReader = null;
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //回收显示日志的资源
    public void recycle(){
        mRunning = false;
        if (exec != null) {
            exec.destroy();
        }
        if (mReader != null) {
            try {
                mReader.close();
                mReader = null;
            } catch (IOException e) {
            }
        }
    }
}
