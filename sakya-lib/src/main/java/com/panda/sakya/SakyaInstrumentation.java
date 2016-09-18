package com.panda.sakya;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.panda.sakya.plugin.CreateActivityData;
import com.panda.sakya.plugin.PlugInfo;
import com.panda.sakya.plugin.PluginContext;
import com.panda.sakya.utils.Reflect;
import com.panda.sakya.utils.ReflectException;
import java.lang.reflect.Field;

/**
 * Created by panda on 16/9/13.
 */
public class SakyaInstrumentation extends Instrumentation {

    private PlugInfo currentPlugin;

    public SakyaInstrumentation(PlugInfo info) {
        this.currentPlugin = info;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        CreateActivityData activityData = (CreateActivityData) intent.getSerializableExtra(Constant.FLAG_ACTIVITY_FROM_PLUGIN);
        if (activityData != null) {
            try {
                currentPlugin.ensureApplicationCreated();
            } catch (Exception e) {
                throw new IllegalAccessException("Cannot get plugin Info : " + activityData.pluginPkg);
            }
            if (activityData.activityName != null) {
                className = activityData.activityName;
                cl = currentPlugin.getClassLoader();
            }
        }
        return super.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if (currentPlugin != null) {
            //初始化插件Activity
            Context baseContext = activity.getBaseContext();
            PluginContext pluginContext = new PluginContext(baseContext, currentPlugin);
            try {
                try {
                    //在许多设备上，Activity自身hold资源
                    Reflect.on(activity).set("mResources", pluginContext.getResources());

                } catch (Throwable ignored) {
                }

                Field field = ContextWrapper.class.getDeclaredField("mBase");
                field.setAccessible(true);
                field.set(activity, pluginContext);
                try {
                    Reflect.on(activity).set("mApplication", currentPlugin.getApplication());
                } catch (ReflectException e) {
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            ActivityInfo activityInfo = currentPlugin.findActivityByClassName(activity.getClass().getName());
            if (activityInfo != null) {
                //根据AndroidManifest.xml中的参数设置Theme
                int resTheme = activityInfo.getThemeResource();
                if (resTheme != 0) {
                    changeActivityInfo(activityInfo, activity);
                    activity.setTheme(resTheme);
                }
            }
        }
        super.callActivityOnCreate(activity, icicle);
    }

    private static void changeActivityInfo(ActivityInfo activityInfo, Activity activity) {
        Field field_mActivityInfo;
        try {
            field_mActivityInfo = Activity.class.getDeclaredField("mActivityInfo");
            field_mActivityInfo.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            field_mActivityInfo.set(activity, activityInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
