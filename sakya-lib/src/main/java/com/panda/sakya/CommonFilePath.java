package com.panda.sakya;

import com.panda.sakya.plugin.PlugInfo;

import java.io.File;

/**
 * Created by panda on 16/9/18.
 */
public class CommonFilePath {

    public static final String PATH_SAKYA = "sakya";

    public static final String PATH_SAKYA_DEX_OPT = "dex_opt";

    public static final String PATH_SAKYA_LIB = "lib";

    public static final String PATH_SAKYA_APK = "apk";

    public static File getPluginLibPath(File nativeLibraryDir, PlugInfo plugInfo) {
        File libPath = new File(nativeLibraryDir, plugInfo.getId() + "-dir/lib/");
        if (!libPath.exists()) {
            libPath.mkdirs();
        }
        return libPath;
    }

    public static File getPluginOptDexPath(File optDexPath, PlugInfo plugInfo) {
        File libPath = new File(optDexPath, plugInfo.getId() + "-dir/dex_opt/");
        if (!libPath.exists()) {
            libPath.mkdirs();
        }
        return libPath;
    }
}
