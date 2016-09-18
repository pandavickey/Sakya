package com.panda.sakya;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.panda.sakya.utils.ComponentUtils;
import com.panda.sakya.utils.FieldUtils;

import java.io.File;

/**
 * Created by panda on 16/9/18.
 */
public class SakyaCallback implements Handler.Callback {

    public static final int LAUNCH_ACTIVITY = 100;
    private Handler.Callback mCallback = null;
    private Context context;
    private File apkFile;

    public SakyaCallback(Context context, Handler.Callback callback, File apkFile) {
        mCallback = callback;
        this.context = context;
        this.apkFile = apkFile;
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == LAUNCH_ACTIVITY) {
            return handleLaunchActivity(msg);
        }
        if (mCallback != null) {
            return mCallback.handleMessage(msg);
        }
        return false;
    }

    private boolean handleLaunchActivity(Message msg) {
        try {
            Intent stubIntent = (Intent) FieldUtils.readField(msg.obj, "intent");
            Intent targetIntent = stubIntent.getParcelableExtra(Constant.EXTRA_TARGET_INTENT);
            if (targetIntent != null) {
                ComponentName targetComponentName = targetIntent.resolveActivity(context.getPackageManager());
                ActivityInfo targetActivityInfo = ComponentUtils.getActivityInfoInNewApp(context, apkFile, targetComponentName.getClassName());
                if (targetActivityInfo != null) {

                    targetIntent.putExtra(Constant.EXTRA_TARGET_INFO, targetActivityInfo);
                    FieldUtils.writeDeclaredField(msg.obj, "intent", targetIntent);
                    FieldUtils.writeDeclaredField(msg.obj, "activityInfo", targetActivityInfo);
                } else {
                }
            } else {
            }
        } catch (Exception e) {
        }

        if (mCallback != null) {
            return mCallback.handleMessage(msg);
        }
        return false;
    }

}