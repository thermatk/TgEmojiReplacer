package com.thermatk.android.xf.telegram;

import android.content.res.XModuleResources;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.thermatk.android.xf.telegram.SettingsActivity.defChoice;
import static com.thermatk.android.xf.telegram.SettingsActivity.prefDef;
import static com.thermatk.android.xf.telegram.SettingsActivity.prefKey;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Xposed implements IXposedHookZygoteInit,IXposedHookLoadPackage {
    static String MODULE_PATH;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, prefDef);
        prefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!"org.telegram.messenger".equals(loadPackageParam.packageName)) {
            return;
        }
        findAndHookMethod("android.content.res.AssetManager", loadPackageParam.classLoader,
                "open", String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String path = (String)param.args[0];
                            if (path.contains("emoji/v10_emoji2.0x")) {
                                XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID, prefDef);

                                XposedBridge.log("TGEMOJI loading replacement "+path + prefs.getInt(prefKey,defChoice));

                                String folder = "";
                                boolean notDef = true;
                                switch (prefs.getInt(prefKey,defChoice)) {
                                    case 0:
                                        notDef = false;
                                        break;
                                    case 1:
                                        folder = "GoogleNoto/";
                                        break;
                                    case 2:
                                        folder = "EmojiOne/";
                                        break;
                                    case 3:
                                        folder = "Twemoji/";
                                        break;
                                }
                                if(notDef) {
                                    param.setResult(XModuleResources.createInstance(MODULE_PATH, null).getAssets().open(folder + path.split("x_")[1]));
                                }
                                return;
                            }
                    }
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });
    }
}
