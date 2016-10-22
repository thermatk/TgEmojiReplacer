package com.thermatk.android.xf.telegram;

import android.content.res.XModuleResources;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Xposed implements IXposedHookZygoteInit,IXposedHookLoadPackage {
    static String MODULE_PATH;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
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
                                XposedBridge.log("TGEMOJI loading replacement "+path);

                                param.setResult(XModuleResources.createInstance(MODULE_PATH, null).getAssets().open("GoogleNoto/"+path.split("x_")[1]));
                                return;
                            }
                    }
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    }
                });
        // CODE FROM V.1.1
        /*
        final Class<?> EmojiClass = findClass("org.telegram.messenger.Emoji", loadPackageParam.classLoader);


        XC_MethodReplacement replaceEmoji_methodReplacement = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return methodHookParam.args[0];
            }
        };
        findAndHookMethod(EmojiClass,
                "replaceEmoji", CharSequence.class, Paint.FontMetricsInt.class, int.class, boolean.class, int[].class,
                replaceEmoji_methodReplacement);

        try {
            final Class<?> EmojiGridAdapter = findClass("org.telegram.ui.Components.EmojiView.EmojiGridAdapter", loadPackageParam.classLoader);

            findAndHookMethod(EmojiGridAdapter, "getView", int.class, View.class, ViewGroup.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                            Object o = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                            if (o instanceof ImageView) {
                                ImageView imageView = (ImageView) o;
                                String convert = (String) imageView.getTag();
                                try {

                                    int bigImgSize = getStaticIntField(EmojiClass, "bigImgSize");
                                    imageView.setImageDrawable(TextDrawable.builder()
                                            .beginConfig()
                                            .textColor(Color.BLACK)
                                            .fontSize(bigImgSize)
                                            .endConfig()
                                            .buildRect(convert, Color.TRANSPARENT));
                                } catch (Exception ignored) {
                                    imageView.setImageDrawable(TextDrawable.builder()
                                            .beginConfig()
                                            .textColor(Color.BLACK)
                                            .endConfig()
                                            .buildRect(convert, Color.TRANSPARENT));

                                }

                            }
                            return o;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }
}
