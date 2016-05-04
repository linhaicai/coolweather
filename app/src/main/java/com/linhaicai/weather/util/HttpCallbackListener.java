package com.linhaicai.weather.util;

/**
 * Created by linhaicai on 2016/5/2.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
