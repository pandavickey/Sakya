package com.panda.sakya.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ComponentUtils {

    public static ActivityInfo[] getNewAppActivities(Context context, File apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
        return info.activities;
    }

    public static ActivityInfo getActivityInfoInNewApp(Context context, File apkPath, String activityClassName) {
        ActivityInfo[] infos = getNewAppActivities(context, apkPath);
        for (ActivityInfo info : infos) {
            if (info.name.equals(activityClassName)) {
                return info;
            }
        }
        return null;
    }

    public static void registerNewReceivers(Context context,File apkPath) {
        try {
            List<ActivityInfo> addedReceivers = getNewAddedReceivers(context,apkPath);
            for (ActivityInfo addedReceiver : addedReceivers) {
                List<IntentFilter> filters = getReceiverIntentFilter(context, addedReceiver,apkPath);
                for (IntentFilter filter : filters) {
                    BroadcastReceiver receiver = (BroadcastReceiver) ComponentUtils.class.getClassLoader().loadClass(addedReceiver.name).newInstance();
                    context.registerReceiver(receiver, filter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static List<ActivityInfo> getNewAddedReceivers(Context context, File apkPath) {
        ActivityInfo[] newAppReceivers = getNewAppReceivers(context, apkPath);
        ActivityInfo[] appReceivers = getAppReceivers(context);
        List<ActivityInfo> addedReceivers = new ArrayList<>();
        if (newAppReceivers != null && newAppReceivers.length > 0) {
            for (ActivityInfo newAppReceiver : newAppReceivers) {
                boolean isNew = true;
                if (appReceivers != null && appReceivers.length > 0) {
                    for (ActivityInfo appReceiver : appReceivers) {
                        if (newAppReceiver.name.equals(appReceiver.name)) {
                            isNew = false;
                            break;
                        }
                    }
                }
                if (isNew) {
                    addedReceivers.add(newAppReceiver);
                }
            }
        }
        return addedReceivers;
    }

    public static ActivityInfo[] getNewAppReceivers(Context context, File apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath.getAbsolutePath(), PackageManager.GET_RECEIVERS);
        return info.receivers;
    }

    private static List<IntentFilter> getReceiverIntentFilter(Context context, ActivityInfo receiverInfo, File apkPath) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<Object> receivers = getReceivers(context, apkPath);
        Object data = null;
        for (Object receiver : receivers) {
            ActivityInfo info = (ActivityInfo) FieldUtils.readField(receiver, "info");
            if (info.name.equals(receiverInfo.name)) {
                data = receiver;
                break;
            }
        }
        return (List<IntentFilter>) FieldUtils.readField(data, "intents");
    }

    public static ActivityInfo[] getAppReceivers(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS);
            return info.receivers;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Object> receivers;

    private static List<Object> getReceivers(Context context, File apkPath) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        if (receivers != null) {
            return receivers;
        }
        Class sPackageParserClass = Class.forName("android.content.pm.PackageParser");
        Object mPackageParser;
        Object mPackage;
        try {
            mPackageParser = sPackageParserClass.newInstance();
            mPackage = MethodUtils.invokeMethod(mPackageParser, "parsePackage", apkPath, 0);
        } catch (Exception e) {
            e.printStackTrace();

            mPackageParser = sPackageParserClass.getDeclaredConstructor(String.class).newInstance(apkPath.getPath());
            Method m = sPackageParserClass.getDeclaredMethod("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);
            m.setAccessible(true);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            mPackage = m.invoke(mPackageParser, apkPath, apkPath.getPath(), metrics, 0);
        }

        receivers = (List<Object>) FieldUtils.readField(mPackage, "receivers");
        if (receivers == null) {
            receivers = new ArrayList<>();
        }
        return receivers;
    }
}