package com.panda.sakya.base;

import com.panda.sakya.plugin.PlugInfo;
import dalvik.system.DexClassLoader;
/**
 * Created by panda on 16/9/13.
 */
public class SakyaClassLoader extends DexClassLoader {

    private PlugInfo plugInfo;

    public SakyaClassLoader(PlugInfo plugInfo, String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.plugInfo = plugInfo;
    }

}
