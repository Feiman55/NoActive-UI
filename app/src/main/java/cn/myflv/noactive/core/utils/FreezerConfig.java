package cn.myflv.noactive.core.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cn.myflv.noactive.constant.ClassConstants;
import cn.myflv.noactive.constant.MethodConstants;
import de.robv.android.xposed.XposedHelpers;

public class FreezerConfig {

    public final static String ConfigDir = "/data/system/NoActive";
    public final static String LogDir = ConfigDir + "/log";
    public final static String whiteAppConfig = "whiteApp.conf";
    public final static String topAppConfig = "topApp.conf";
    public final static String directAppConfig = "directApp.conf";
    public final static String socketAppConfig = "socketApp.conf";
    public final static String idleAppConfig = "idleApp.conf";
    public final static String blackSystemAppConfig = "blackSystemApp.conf";
    public final static String whiteProcessConfig = "whiteProcess.conf";
    public final static String killProcessConfig = "killProcess.conf";
    public final static String kill19 = "kill.19";
    public final static String kill20 = "kill.20";
    public final static String freezerV1 = "freezer.v1";
    public final static String freezerV2 = "freezer.v2";
    public final static String freezerApi = "freezer.api";
    public final static String API = "Api";
    public final static String V2 = "V2";
    public final static String V1 = "V1";
    public final static String lastLog = "last.log";
    public final static String currentLog = "current.log";
    public final static String Debug = "debug";
    public final static String IntervalUnfreeze = "interval.unfreeze";
    public final static String IntervalUnfreezeDelay = "interval.unfreeze.delay";
    public final static String IntervalFreeze = "interval.freeze";
    public final static String IntervalFreezeDelay = "interval.freeze.delay";
    public final static String BootFreeze = "boot.freeze";
    public final static String BootFreezeDelay = "boot.freeze.delay";
    public final static String SuExcute = "su.excute";
    public final static String[] listenConfig = {whiteAppConfig, whiteProcessConfig,
            killProcessConfig, blackSystemAppConfig, directAppConfig, topAppConfig, socketAppConfig, idleAppConfig};

    public static boolean isScheduledOn() {
        return isConfigOn(IntervalUnfreeze);
    }

    public static boolean isConfigOn(String configName) {
        File config = new File(ConfigDir, configName);
        return config.exists();
    }

    public static int getKillSignal() {
        if (isConfigOn(kill19)) {
            return 19;
        }
        if (isConfigOn(kill20)) {
            return 20;
        }
        return 19;
    }


    public static String getFreezerVersion(ClassLoader classLoader) {
        if (isConfigOn(freezerV2)) {
            return V2;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isConfigOn(freezerApi)) {
                return API;
            }

        }
        if (isConfigOn(freezerV1)) {
            return V1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isAndroidApi(classLoader)) {
                return V2;
            }
        }
        return V1;
    }

    public static boolean isAndroidApi(ClassLoader classLoader) {
        Class<?> CachedAppOptimizer = XposedHelpers.findClass(ClassConstants.CachedAppOptimizer, classLoader);
        return (boolean) XposedHelpers.callStaticMethod(CachedAppOptimizer, MethodConstants.isFreezerSupported);
    }

    public static boolean isXiaoMiV1(ClassLoader classLoader) {
        try {
            return XposedHelpers.findClassIfExists(ClassConstants.GreezeManagerService, classLoader) != null;
        } catch (Throwable ignored) {
        }
        return false;
    }


    public static boolean isUseKill() {
        return isConfigOn(kill19) || isConfigOn(kill20);
    }


    public static void checkAndInit() {
        File configDir = new File(ConfigDir);
        if (!configDir.exists()) {
            boolean mkdir = configDir.mkdir();
            if (!mkdir) {
                Log.xposedLog("NoActive(error) -> Config dir init failed");
                return;
            }
        }
        File logDir = new File(LogDir);
        if (!logDir.exists()) {
            boolean mkdir = logDir.mkdir();
            if (!mkdir) {
                Log.xposedLog("NoActive(error) -> Log dir init failed");
                return;
            }
        }
        for (String configName : listenConfig) {
            File config = new File(configDir, configName);
            if (!config.exists()) {
                createFile(config);
                Log.i("Init " + configName);
            }
        }
    }

    public static void cleanLog() {
        File source = new File(LogDir, currentLog);
        File dest = new File(LogDir, lastLog);
        moveFile(source, dest);
    }

    public static void moveFile(File source, File dest) {
        try {
            boolean delete = dest.delete();
            boolean renameTo = source.renameTo(dest);
        } catch (Exception ignored) {

        }
    }


    public static Set<String> get(String name) {
        Set<String> set = new HashSet<>();
        try {
            File file = new File(ConfigDir, name);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String finalLine = line.trim();
                if ("".equals(finalLine) || line.startsWith("#")) {
                    continue;
                }
                set.add(line.trim());
            }
            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(name + " file not found");
        } catch (IOException ioException) {
            Log.e(name + " file read filed");
        }
        return set;
    }

    public static String getString(String name) {
        Set<String> set = get(name);
        if (set.isEmpty()) {
            return "";
        }
        return set.iterator().next();
    }

    public static String getString(String name, String defaultValue) {
        Set<String> set = get(name);
        if (set.isEmpty()) {
            return defaultValue;
        }
        return set.iterator().next();
    }

    public static void createFile(File file) {
        try {
            boolean newFile = file.createNewFile();
            if (!newFile) {
                throw new IOException();
            }
        } catch (IOException e) {
            Log.e(file.getName() + " file create filed");
        }
    }
}
