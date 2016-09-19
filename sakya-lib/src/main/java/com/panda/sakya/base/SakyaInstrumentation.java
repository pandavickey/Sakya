package com.panda.sakya.base;

import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.view.ContextThemeWrapper;

import com.panda.sakya.Constant;
import com.panda.sakya.Sakya;
import com.panda.sakya.plugin.CreateActivityData;
import com.panda.sakya.plugin.DynamicActivity;
import com.panda.sakya.plugin.PlugInfo;
import com.panda.sakya.plugin.PluginContext;
import com.panda.sakya.utils.Reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by panda on 16/9/13.
 */
public class SakyaInstrumentation extends DelegateInstrumentation implements IInstrumentation {

    private PlugInfo currentPlugin;

    public SakyaInstrumentation(Instrumentation mBase) {
        super(mBase);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        CreateActivityData activityData = (CreateActivityData) intent.getSerializableExtra(Constant.FLAG_ACTIVITY_FROM_PLUGIN);
        if (activityData != null) {
            try {
                currentPlugin = Sakya.getSingleton().getPluginByPackageName(activityData.pluginPkg);
                currentPlugin.ensureApplicationCreated();
            } catch (Exception e) {
                throw new IllegalAccessException("Cannot get plugin Info : " + activityData.pluginPkg);
            }
            if (activityData.activityName != null) {
                className = activityData.activityName;
                cl = currentPlugin.getClassLoader();
            }
            Activity activity = super.newActivity(cl, className, intent);
            Reflect.on(activity).set("mResources", currentPlugin.getResources());
            Reflect.on(activity).set("mApplication", currentPlugin.getApplication());
            return activity;
        }
        return super.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        lookupActivityInPlugin(activity);
        if (currentPlugin != null) {
            //初始化插件Activity
            Context baseContext = activity.getBaseContext();
            PluginContext pluginContext = new PluginContext(baseContext, currentPlugin);
            try {
                Field field = ContextWrapper.class.getDeclaredField("mBase");
                field.setAccessible(true);
                field.set(activity, pluginContext);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            ActivityInfo activityInfo = currentPlugin.findActivityByClassName(activity.getClass().getName());
            if (activityInfo != null) {
                //根据AndroidManifest.xml中的参数设置Theme
                int resTheme = activityInfo.getThemeResource();
                if (resTheme != 0) {
                    try {
                        Field mTheme = ContextThemeWrapper.class.getDeclaredField("mTheme");
                        mTheme.setAccessible(true);
                        changeActivityInfo(activityInfo, activity);
                        activity.setTheme(resTheme);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        super.callActivityOnCreate(activity, icicle);
    }

    private void lookupActivityInPlugin(Activity activity) {
        ClassLoader classLoader = activity.getClass().getClassLoader();
        if (classLoader instanceof SakyaClassLoader) {
            currentPlugin = ((SakyaClassLoader) classLoader).getPlugInfo();
        } else {
            currentPlugin = null;
        }
    }

    private static void changeActivityInfo(ActivityInfo activityInfo, Activity activity) {
        Field field_mActivityInfo;
        try {
            field_mActivityInfo = Activity.class.getDeclaredField("mActivityInfo");
            field_mActivityInfo.setAccessible(true);
            field_mActivityInfo.set(activity, activityInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
        try {
            Method method = mBase.getClass().getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class);
            replaceIntentTargetIfNeed(who,intent);
            return (ActivityResult) method.invoke(mBase, who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode) {
        try {
            Method method = mBase.getClass().getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class);
            replaceIntentTargetIfNeed(who,intent);
            return (ActivityResult) method.invoke(mBase, who, contextThread, token, target, intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment target, Intent intent, int requestCode) {
        try {
            Method method = mBase.getClass().getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, Fragment.class, Intent.class, int.class);
            replaceIntentTargetIfNeed(who,intent);
            return (ActivityResult) method.invoke(mBase, who, contextThread, token, target, intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment target, Intent intent, int requestCode, Bundle options) {
        try {
            Method method = mBase.getClass().getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, Fragment.class, Intent.class, int.class, Bundle.class);
            replaceIntentTargetIfNeed(who,intent);
            return (ActivityResult) method.invoke(mBase, who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options, UserHandle user) {
        try {
            Method method = mBase.getClass().getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class, UserHandle.class);
            replaceIntentTargetIfNeed(who,intent);
            return (ActivityResult) method.invoke(mBase, who, contextThread, token, target, intent, requestCode, options, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target, Intent intent, int requestCode, Bundle options) {
        try {
            Method method = mBase.getClass().getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, String.class, Intent.class, int.class, Bundle.class);
            replaceIntentTargetIfNeed(who,intent);
            return (ActivityResult) method.invoke(mBase, who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void replaceIntentTargetIfNeed(Context from, Intent intent)
    {
        if (!intent.hasExtra(Constant.FLAG_ACTIVITY_FROM_PLUGIN) && currentPlugin != null)
        {
            ComponentName componentName = intent.getComponent();
            if (componentName != null)
            {
                String pkgName = componentName.getPackageName();
                String activityName = componentName.getClassName();
                if (pkgName != null)
                {
                    CreateActivityData createActivityData = new CreateActivityData(activityName, currentPlugin.getPackageName());
                    ActivityInfo activityInfo = currentPlugin.findActivityByClassName(activityName);
                    if (activityInfo != null) {
                        intent.setClass(from, DynamicActivity.class);
                        intent.putExtra(Constant.FLAG_ACTIVITY_FROM_PLUGIN, createActivityData);
                        intent.setExtrasClassLoader(currentPlugin.getClassLoader());
                    }
                }
            }
        }
    }
}
