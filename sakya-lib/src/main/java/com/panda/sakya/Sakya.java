package com.panda.sakya;

import android.app.Application;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.panda.sakya.base.SakyaClassLoader;
import com.panda.sakya.base.SakyaInstrumentation;
import com.panda.sakya.plugin.CreateActivityData;
import com.panda.sakya.plugin.DynamicActivity;
import com.panda.sakya.plugin.PlugInfo;
import com.panda.sakya.plugin.PluginContext;
import com.panda.sakya.plugin.PluginManifestUtil;
import com.panda.sakya.utils.FileUtils;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.panda.sakya.compat.ActivityThreadCompat.instance;
import static com.panda.sakya.utils.FieldUtils.readField;
import static com.panda.sakya.utils.FieldUtils.writeField;
import static com.panda.sakya.utils.MethodUtils.getDeclaredMethod;

/**
 * Created by panda on 16/9/13.
 */
public class Sakya {

    private static Sakya SINGLETON;
    private Context context;
    private File apkDir, optimizedDir, nativeLibraryDir;

    private final Map<String, PlugInfo> pluginPkgToInfoMap = new ConcurrentHashMap<String, PlugInfo>();


    private Sakya(Context context) {
        this.context = context;
        File directory = new File(context.getFilesDir(), CommonFilePath.PATH_SAKYA);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        apkDir = new File(directory, CommonFilePath.PATH_SAKYA_APK);
        if (!apkDir.exists()) {
            apkDir.mkdirs();
        }
        optimizedDir = new File(directory, CommonFilePath.PATH_SAKYA_DEX_OPT);
        if (!optimizedDir.exists()) {
            optimizedDir.mkdirs();
        }
        nativeLibraryDir = new File(directory, CommonFilePath.PATH_SAKYA_LIB);
        if (!nativeLibraryDir.exists()) {
            nativeLibraryDir.mkdirs();
        }
    }

    public static Sakya getSingleton() {
        if (SINGLETON == null) {
            throw new IllegalStateException("Please init the at application oncreate first!");
        }
        return SINGLETON;
    }

    public static void init(Context context) {
        if (SINGLETON == null) {
            SINGLETON = new Sakya(context);
        }
    }

    public void loadApk(File apkPath) throws Exception {
        if (apkPath.isFile()) {
            File privateFile = new File(apkDir, apkPath.getName());
            FileUtils.copyFile(apkPath, privateFile);

            AssetManager am = AssetManager.class.newInstance();
            Method addAssetPath = getDeclaredMethod(AssetManager.class, "addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            addAssetPath.invoke(am, privateFile.getAbsolutePath());

            Resources hotRes = context.getResources();
            Resources res = new Resources(am, hotRes.getDisplayMetrics(),
                    hotRes.getConfiguration());

            PlugInfo info = new PlugInfo();
            info.setId(apkPath.getName());
            info.setFilePath(privateFile.getAbsolutePath());
            info.setAssetManager(am);
            info.setResources(res);
            PluginManifestUtil.setManifestInfo(context, privateFile.getAbsolutePath(), info);

            SakyaClassLoader pluginClassLoader = new SakyaClassLoader(info, privateFile.getAbsolutePath(), CommonFilePath.getPluginOptDexPath(optimizedDir, info).getAbsolutePath()
                    , getPluginLibPath(info).getAbsolutePath(), ClassLoader.getSystemClassLoader().getParent());
            info.setClassLoader(pluginClassLoader);

            Application app = makeApplication(info);
            attachBaseContext(info, app);
            info.setApplication(app);

            Instrumentation oldInstrumentation = (Instrumentation) readField(instance(), "mInstrumentation", true);
            SakyaInstrumentation instrumentation = new SakyaInstrumentation(oldInstrumentation);
            writeField(instance(), "mInstrumentation", instrumentation, true);

            pluginPkgToInfoMap.put(info.getPackageName(), info);

            startActivity(context, info, info.getMainActivity().activityInfo);
        }
    }

    public PlugInfo getPluginByPackageName(String packageName){
        return pluginPkgToInfoMap.get(packageName);
    }
    public void startActivity(Context from, PlugInfo plugInfo, ActivityInfo activityInfo) {
        if (activityInfo == null) {
            throw new ActivityNotFoundException("Cannot find ActivityInfo from plugin, could you declare this Activity in plugin?");
        }
        Intent intent = new Intent();
        CreateActivityData createActivityData = new CreateActivityData(activityInfo.name, plugInfo.getPackageName());
        intent.setClass(from, DynamicActivity.class);
        intent.putExtra(Constant.FLAG_ACTIVITY_FROM_PLUGIN, createActivityData);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
    }

    private Application makeApplication(PlugInfo plugInfo) {
        ApplicationInfo appInfo = plugInfo.getPackageInfo().applicationInfo;
        String appClassName = appInfo.className;
        if (appClassName == null) {
            appClassName = Application.class.getName();
        }
        try {
            return (Application) plugInfo.getClassLoader().loadClass(appClassName).newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Unable to create Application for "
                    + plugInfo.getPackageName() + ": "
                    + e.getMessage());
        }
    }

    private void attachBaseContext(PlugInfo info, Application app) {
        try {
            Field mBase = ContextWrapper.class.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(app, new PluginContext(context.getApplicationContext(), info));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public File getPluginLibPath(PlugInfo info) {
        return CommonFilePath.getPluginLibPath(nativeLibraryDir, info);
    }

}
