package com.linhaicai.weather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by linhaicai on 2016/5/4.
 */
public class AutoUpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i=new Intent(context,AutoUpdateReceiver.class);
        context.startService(i);
    }
}
