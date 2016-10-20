package com.thermatk.android.xf.telegram;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;

public class Xposed implements IXposedHookLoadPackage {

    public int VERSIONCODE;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if ((!"org.telegram.messenger".equals(loadPackageParam.packageName)) && (!"org.telegram.plus".equals(loadPackageParam.packageName))) {
            return;
        }

        Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
        Context systemContext = (Context) callMethod(activityThread, "getSystemContext");
        VERSIONCODE = systemContext.getPackageManager().getPackageInfo(loadPackageParam.packageName, 0).versionCode;

        final Class<?> EmojiClass = findClass("org.telegram.messenger.Emoji", loadPackageParam.classLoader);

        XC_MethodReplacement replaceEmoji_methodReplacement = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                if ((("org.telegram.messenger").equals(loadPackageParam.packageName) && (VERSIONCODE > 8371)) || (("org.telegram.plus").equals(loadPackageParam.packageName) && (VERSIONCODE > 8503))) {
                    return methodHookParam.args[0];
                } else {
                    CharSequence cs = ((CharSequence) methodHookParam.args[0]);
                    if (cs == null || cs.length() == 0) {
                        return cs;
                    }
                    Spannable s;
                    boolean createNew = (boolean) methodHookParam.args[3];
                    if (!createNew && cs instanceof Spannable) {
                        s = (Spannable) cs;
                    } else {
                        s = Spannable.Factory.getInstance().newSpannable(cs.toString());
                    }
                    try {
                        return s;
                    } catch (Exception e) {
                        return cs;
                    }
                }
            }
        };

        if ((("org.telegram.messenger").equals(loadPackageParam.packageName) && (VERSIONCODE > 8371)) || (("org.telegram.plus").equals(loadPackageParam.packageName) && (VERSIONCODE > 8503))) {
            findAndHookMethod(EmojiClass,
                    "replaceEmoji", CharSequence.class, Paint.FontMetricsInt.class, int.class, boolean.class, int[].class,
                    replaceEmoji_methodReplacement);
        } else {
            findAndHookMethod(EmojiClass,
                    "replaceEmoji", CharSequence.class, Paint.FontMetricsInt.class, int.class, boolean.class,
                    replaceEmoji_methodReplacement);
        }

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
    }
}
