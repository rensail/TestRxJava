package com.example.testrxjava;


import android.util.Log;
import java.util.List;

/**
 * 有道翻译接收json数据的实例类
 */
public class Translation {
    private String type;
    private int errorCode;
    private int elapsedTime;
    private List<List<translateResultBean>> translateResult;

    public void setType(String type) {
        this.type = type;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setTranslateResult(List<List<translateResultBean>> translateResult) {
        this.translateResult = translateResult;
    }

    public String getType() {
        return type;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public List<List<translateResultBean>> getTranslateResult() {
        return translateResult;
    }


    public class translateResultBean {
        public String src;
        public String tgt;

        public void setSrc(String src) {
            this.src = src;
        }

        public void setTgt(String tgt) {
            this.tgt = tgt;
        }

        public String getSrc() {
            return src;
        }

        public String getTgt() {
            return tgt;
        }
    }

    //显示返回的结果
    public void show(){
        System.out.println(getTranslateResult().get(0).get(0).getTgt());
    }
}
